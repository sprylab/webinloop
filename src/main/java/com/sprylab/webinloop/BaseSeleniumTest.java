/*******************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Copyright 2009-2011 by sprylab technologies GmbH
 * 
 * WebInLoop - a program for testing web applications
 * 
 * This file is part of WebInLoop.
 * 
 * WebInLoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 * 
 * WebInLoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with WebInLoop.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>
 * for a copy of the LGPLv3 License.
 ******************************************************************************/

package com.sprylab.webinloop;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.sprylab.webinloop.reporter.Reporter;
import com.sprylab.webinloop.reporter.ReporterTest;
import com.sprylab.webinloop.util.db.Database;
import com.sprylab.webinloop.util.mailer.Mailer;
import com.thoughtworks.selenium.SeleneseTestBase;
import com.thoughtworks.selenium.SeleniumException;

/**
 * <p>
 * Provides base functionality for Selenium tests in combination with TestNG for
 * automatic testing of web app.
 * </p>
 * <p>
 * Functionality includes starting / stopping of Selenium server and assert
 * methods.
 * </p>
 * <p>
 * Selenium is controlled via wrapper object that is capable of creating reports
 * of every test in form of screenshots (see {@link ScreenshottingSelenium} and
 * {@link Reporter}).
 * </p>
 */
public class BaseSeleniumTest extends SeleneseTestBase {

    /**
     * Logger instance.
     */
    protected static Log log = LogFactory.getLog(BaseSeleniumTest.class);

    /**
     * The reporter instance.
     */
    protected static ReporterTest reporter = null;

    /**
     * Asserts an condition throwing a {@link AssertionError} with a default
     * error message.
     * 
     * @param condition
     *            condition to comply
     */
    public static void assertTrue(boolean condition) {
        SeleneseTestBase.assertTrue("Assertion failed", condition);
    }

    /**
     * Marks a test as failed. It takes a screenshot.
     * 
     * @param cause
     *            the cause of the failure
     */
    public static void fail(Throwable cause) {
        cause.printStackTrace();
        reporter.recordStep("FAIL: " + cause.getMessage());
        reporter.testFailed();
    }

    /**
     * Calls {@link BaseSeleniumTest#seleniumEquals(String, String)} and thus
     * supports multiple lines for regex expressions.
     * 
     * @param expected
     *            expected object
     * @param actual
     *            actual object
     * @return true if equal or matches
     * @see SeleneseTestBase#seleniumEquals(Object, Object)
     */
    public static boolean seleniumEquals(Object expected, Object actual) {
        if (expected instanceof String && (actual instanceof String || actual instanceof Number)) {
            // actual may be number because of selenium.getMouseSpeed()
            return BaseSeleniumTest.seleniumEquals(expected.toString(), actual.toString());
        }
        return expected.equals(actual);
    }

    /**
     * This version supports matching regex expressions in strings with multiple
     * lines.
     * 
     * @param expectedPattern
     *            Selenium regex pattern
     * @param actual
     *            actual string
     * @return true if equal or matches
     * @see SeleneseTestBase#seleniumEquals(String, String)
     */
    public static boolean seleniumEquals(String expectedPattern, String actual) {
        boolean result = false;

        String[] lines = null;
        if (expectedPattern.startsWith("exact:") || expectedPattern.equals("")) {
            // should match exactly, so fill lines array with untreated string
            lines = new String[] {actual };
        } else {
            // no exact match, so split string as it may contain multiple lines
            lines = StringUtils.split(actual, "\n\r");
        }

        // check for each line, if it's matching
        for (int i = 0; i < lines.length; i++) {
            result |= SeleneseTestBase.seleniumEquals(expectedPattern, lines[i]);
        }
        return result;
    }

    /**
     * Returns true, if a array (represented as an comma separated string)
     * equals an (real) string array.
     * 
     * Please note: The expected value may contain escaped <b>,</b> (<b>\,</b>)
     * which are not treated as a value separator.
     * 
     * @param expected
     *            comma separated string of values
     * @param actualValues
     *            array with all values
     * @return true if equal or matches
     * @see SeleneseTestBase#seleniumEquals(Object, Object)
     */
    public static boolean seleniumEquals(String expected, String[] actualValues) {
        boolean result = false;

        final String prefix = getExpressionPrefix(expected);
        final String expectedWithoutPrefix = expected.replaceFirst(prefix, "");

        // split expected string and assign to array
        // split matches , but not \,
        String[] expectedValues = expectedWithoutPrefix.split("(?<!\\\\),", actualValues.length);

        if (actualValues.length == expectedValues.length) {
            // we do a AND comparison, so set result to the neutral true
            result = true;
            // equal number of actual and expected values
            for (int i = 0; i < expectedValues.length; i++) {
                // clean expected value has \, where the actual value has only ,
                String expectedCleaned = prefix + cleanExpectedStringForArray(expectedValues[i]);
                result &= BaseSeleniumTest.seleniumEquals(expectedCleaned, actualValues[i]);
            }
        } else if (expectedValues.length == 1) {
            // we do a OR comparison, so set result to the neutral false
            result = false;
            // clean expected value has \, where the actual value has only ,
            String expectedCleaned = prefix + cleanExpectedStringForArray(expectedValues[0]);
            for (int i = 0; i < actualValues.length; i++) {
                result |= BaseSeleniumTest.seleniumEquals(expectedCleaned, actualValues[i]);
            }
        }

        return result;
    }

    /**
     * Cleans an expected expected value string for comparison with an array.
     * This function replaces all <code>\\\\,</code> with <code>,</code> and all
     * <code>\\\\</code> with <code>\\</code>.
     * 
     * @param string
     *            the string to clean
     * @return cleaned version of string
     */
    protected static String cleanExpectedStringForArray(String string) {
        String result = string.replaceAll("\\\\,", ",");
        return StringUtils.replace(result, "\\\\", "\\");
    }

    /**
     * Returns the prefix of a Selenium value string. The supported prefixes
     * are:
     * 
     * <ul>
     * <li>regexp</li>
     * <li>regex</li>
     * <li>regexpi</li>
     * <li>regexi</li>
     * <li>glob</li>
     * <li>exact</li>
     * </ul>
     * 
     * @param string
     *            the value string to analyze
     * @return the prefix of string
     */
    protected static String getExpressionPrefix(String string) {
        if (string.startsWith("regexp:") || string.startsWith("regex:") || string.startsWith("regexpi:")
                || string.startsWith("regexi:") || string.startsWith("glob:") || string.startsWith("exact:")) {
            return string.substring(0, string.indexOf(":") + 1);
        }

        return "";
    };

    /**
     * Flag indicating that Selenium should expect an error on the next command
     * execution.
     */
    protected boolean assertErrorOnNext = false;

    /**
     * The message error message that is expected.
     */
    protected String assertErrorOnNextMessage = "";

    /**
     * Flag indicating that Selenium should expect a failure on the next command
     * execution.
     */
    protected boolean assertFailureOnNext = false;

    /**
     * The message indicating the reason for the failure.
     */
    protected String assertFailureOnNextMessage = "";

    /**
     * The Selenium object that receives all the commands.
     */
    protected ScreenshottingSelenium selenium = null;

    /**
     * The Selenium server.
     */
    protected SeleniumServer seleniumServer = null;

    /**
     * All Selenium defined variables needed for the test case.
     */
    protected HashMap<String, Object> testCaseVariables = new HashMap<String, Object>();

    /**
     * Default constructor.
     */
    public BaseSeleniumTest() {
        Set<Entry<Object, Object>> entrySet = System.getProperties().entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            this.testCaseVariables.put(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thoughtworks.selenium.SeleneseTestBase#checkForVerificationErrors()
     */
    @Override
    public void checkForVerificationErrors() {
        String verifyError = this.verificationErrors.toString();
        if (!verifyError.isEmpty()) {
            // there is a verification error
            clearVerificationErrors();

            if (assertFailureOnNext) {
                throw new SeleniumException(assertFailureOnNextMessage);
            }

            reporter.recordStep("ERROR: Verification failed");
            reporter.setVerificationFailed();
        }
    }

    /**
     * If the test has failed, an exception is thrown. This is the case for
     * tests that only have had verification errors.
     * 
     * @throws Exception
     *             is thrown when there were verification errors
     */
    protected void checkForFailure() throws Exception {
        if (reporter.isFailed()) {
            throw new Exception("There were verification errors.");
        }
    }

    /**
     * Clears state of assertErrorOnNext: No error is expected when executing
     * the next command.
     */
    protected void clearAssertErrorOnNext() {
        assertErrorOnNext = false;
        assertErrorOnNextMessage = "";
    }

    /**
     * Clears state of assertFailureOnNext: No failure occurs when executing the
     * next (verification) command returns false.
     */
    protected void clearAssertFailureOnNext() {
        assertFailureOnNext = false;
        assertFailureOnNextMessage = "";
    }

    /**
     * Connect to the Selenium server and sets up reporter for screenshot
     * taking.
     * 
     * @throws Exception
     *             if error occurs during connection to Selenium server.
     */
    protected void connectToSeleniumServer() throws Exception {
        String serverHost =
                WiLConfiguration.getInstance().getString(WiLConfiguration.SELENIUM_SERVER_HOST_PROPERTY_KEY);
        int serverPort = WiLConfiguration.getInstance().getInt(WiLConfiguration.SELENIUM_SERVER_PORT_PROPERTY_KEY);
        String browser = WiLConfiguration.getInstance().getString(WiLConfiguration.BROWSER_PROPERTY_KEY);
        String baseUrl = correctBaseUrl(getBaseUrl());

        reporter = new ReporterTest(this.getClass().getSimpleName(), baseUrl);
        this.selenium = new ScreenshottingSelenium(serverHost, serverPort, browser, baseUrl, reporter);
    }

    /**
     * If the baseURL denotes a file, it will convert this file into a valid URI
     * to use for baseURL. Else it will return the baseURL unchanged.
     * 
     * @param baseUrl
     *            the baseURL
     * @return the corrected baseURL
     */
    protected String correctBaseUrl(String baseUrl) {
        File baseUrlFile = new File(baseUrl);
        if (baseUrlFile.exists()) {
            baseUrlFile = baseUrlFile.getAbsoluteFile();

            return StringUtils.replaceOnce(baseUrlFile.toURI().toString(), "file:", "file://");
        }

        return baseUrl;
    }

    /**
     * Returns the baseURL as defined via system properties. It is likely that
     * this method will be overridden by the test case.
     * 
     * @return the baseURL
     */
    protected String getBaseUrl() {
        return WiLConfiguration.getInstance().getString(WiLConfiguration.BASE_URL_PROPERTY_KEY);
    }

    /**
     * Retrieves the test variable specified by <code>expression</code>.
     * <code>Expression</code> may be a string that optional specifies an escape
     * mode delimited by a , . If no escape mode is passed, than the test
     * variable will be returned as it is.
     * 
     * Available escape modes:
     * <ul>
     * <li>csv</li>
     * <li>html</li>
     * <li>java</li>
     * <li>javascript</li>
     * <li>sql</li>
     * <li>xml</li>
     * </ul>
     * 
     * @param expression
     *            name of the test variable (optional with escape mode delimited
     *            by a ,)
     * @return (escaped) test variable
     */
    protected String getTestVariableValue(String expression) {
        // handle special cases for "nbsp" and "space"
        if (expression.equals("nbsp")) {
            // string containing only nbsp
            final char nbspAscii = (char) 160;
            return new String(new char[] {nbspAscii });
        }

        if (expression.equals("space")) {
            // string containing only space
            final char spaceAscii = (char) 20;
            return new String(new char[] {spaceAscii });
        }

        int separatorIdx = expression.indexOf(",");
        String variable;
        if (separatorIdx >= 0) {
            variable = expression.substring(0, separatorIdx);
        } else {
            variable = expression;
        }

        String result = "";
        boolean uppercase = false;

        if (!this.testCaseVariables.containsKey(variable) && this.testCaseVariables.containsKey(variable.toLowerCase())) {
            // there is a lower case variable key, so the user requested a
            // upper case result
            variable = variable.toLowerCase();
            uppercase = true;
        }

        Object value = this.testCaseVariables.get(variable);

        if (value instanceof Object[]) {
            result = StringUtils.join((Object[]) value, ",");
        } else {
            result = value.toString();
        }

        if (separatorIdx >= 0) {
            String escapeToken = expression.substring(separatorIdx + 1);
            String escapeTokenL = escapeToken.toLowerCase();
            if (escapeTokenL.startsWith("regex:")) {
                escapeToken = escapeToken.substring("regex:".length());
                Matcher m = Pattern.compile(escapeToken).matcher(result);
                if (m.find()) {
                    result = m.group(1);
                } else {
                    result = "";
                }
            } else if (escapeTokenL.equals("csv")) {
                result = StringEscapeUtils.escapeCsv(result);
            } else if (escapeTokenL.equals("html")) {
                result = StringEscapeUtils.escapeHtml(result);
            } else if (escapeTokenL.equals("java")) {
                result = StringEscapeUtils.escapeJava(result);
            } else if (escapeTokenL.equals("javascript")) {
                result = StringEscapeUtils.escapeJavaScript(result);
            } else if (escapeTokenL.equals("sql")) {
                result = StringEscapeUtils.escapeSql(result);
            } else if (escapeTokenL.equals("xml")) {
                result = StringEscapeUtils.escapeXml(result);
            }
        }

        if (uppercase) {
            result = result.toUpperCase();
        }

        return result;
    }

    /**
     * Asks the reporter to generate the summary.
     */
    protected void makeSummary() {
        Reporter.getInstance().makeSummary();
    }

    /**
     * When executing the next command, a error is expected specified by the
     * message.
     * 
     * @param message
     *            error message that is expected
     */
    protected void setAssertErrorOnNext(String message) {
        if (message != null) {
            this.assertErrorOnNextMessage = message;
            this.assertErrorOnNext = true;
        }
    }

    /**
     * When executing the next (verification) command fails, the whole test
     * fails.
     * 
     * @param message
     *            error message that is expected
     */
    protected void setAssertFailureOnNext(String message) {
        if (message != null) {
            this.assertFailureOnNextMessage = message;
            this.assertFailureOnNext = true;
        }
    }

    /**
     * Stops the selenium server and generates the summary.
     */
    @AfterSuite(alwaysRun = true)
    protected void shutdown() {
        this.stopSeleniumServer();
        this.makeSummary();
    }

    /**
     * Start reporting for a test class that is run.
     * 
     * @throws Exception
     *             an error occurred while initializing a connection to the
     *             Selenium server
     */
    @BeforeClass(alwaysRun = true)
    protected void startRecording() throws Exception {
        log.info("Running test " + getClass().getSimpleName() + ".");
        connectToSeleniumServer();
        this.selenium.startRecordingTest();
        this.selenium.start();
    }

    /**
     * Starts the Selenium server configuring it using the options from the
     * properties.
     */
    protected void startSeleniumServer() {
        try {
            this.seleniumServer = new SeleniumServer(initServerConfiguration());
            this.seleniumServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the server with the values from the seleniumServer.xml
     * property file, if values are set.
     * 
     * @return a Selenium {@link RemoteControlConfiguration}
     */
    private RemoteControlConfiguration initServerConfiguration() {
        RemoteControlConfiguration rcConfiguration = new RemoteControlConfiguration();

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.AVOID_PROXY_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setAvoidProxy(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.AVOID_PROXY_PROPERTY_KEY));
        }
        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.BROWSER_SIDE_LOG_ENABLED_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setBrowserSideLogEnabled(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.BROWSER_SIDE_LOG_ENABLED_PROPERTY_KEY));
        }

        // originally from selenium.xml
        rcConfiguration.setDebugMode(WiLConfiguration.getInstance()
                .getBoolean(WiLConfiguration.DEBUG_MODE_PROPERTY_KEY));

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.DEBUG_URL_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setDebugURL(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.DEBUG_URL_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.DONT_INJECT_REGEX_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setDontInjectRegex(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.DONT_INJECT_REGEX_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.DONT_TOUCH_LOGGING_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setDontTouchLogging(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.DONT_TOUCH_LOGGING_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.ENSURE_CLEAN_SESSION_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setEnsureCleanSession(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.ENSURE_CLEAN_SESSION_PROPERTY_KEY));
        }

        File firefoxProfileTemplate =
                new File(WiLConfiguration.getInstance().getString(
                        WiLConfiguration.FIREFOX_PROFILE_TEMPLATE_PROPERTY_KEY));
        if (firefoxProfileTemplate.exists() && firefoxProfileTemplate.isDirectory()) {
            rcConfiguration.setFirefoxProfileTemplate(firefoxProfileTemplate.getAbsoluteFile());
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.FORCED_BROWSER_MODE_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setForcedBrowserMode(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.FORCED_BROWSER_MODE_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.HONOR_SYSTEM_PROXY_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setHonorSystemProxy(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.HONOR_SYSTEM_PROXY_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.HTML_SUITE_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setHTMLSuite(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.HTML_SUITE_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.INTERACTIVE_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setInteractive(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.INTERACTIVE_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.JETTY_THREADS_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setJettyThreads(WiLConfiguration.getInstance().getInt(
                    WiLConfiguration.JETTY_THREADS_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.LOG_OUT_FILE_NAME_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setLogOutFileName(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.LOG_OUT_FILE_NAME_PROPERTY_KEY));
        }

        rcConfiguration.setPort(WiLConfiguration.getInstance().getInt(
                WiLConfiguration.SELENIUM_SERVER_PORT_PROPERTY_KEY));

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.PORT_DRIVERS_SHOULD_CONTACT_PROPERTY_KEY)
                .isEmpty()) {
            rcConfiguration.setPortDriversShouldContact(WiLConfiguration.getInstance().getInt(
                    WiLConfiguration.PORT_DRIVERS_SHOULD_CONTACT_PROPERTY_KEY));
        }

        File profilesLocation =
                new File(WiLConfiguration.getInstance().getString(WiLConfiguration.PROFILES_LOCATION_PROPERTY_KEY));
        if (profilesLocation.exists() && profilesLocation.isDirectory()) {
            rcConfiguration.setProfilesLocation(profilesLocation.getAbsoluteFile());
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.PROXY_INJECTION_MODE_ARG_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setProxyInjectionModeArg(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.PROXY_INJECTION_MODE_ARG_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.RETRY_TIMEOUT_IN_SECONDS_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setRetryTimeoutInSeconds(WiLConfiguration.getInstance().getInt(
                    WiLConfiguration.RETRY_TIMEOUT_IN_SECONDS_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.REUSE_BROWSER_SESSION_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setReuseBrowserSessions(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.REUSE_BROWSER_SESSION_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.SELF_TEST_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setSelfTest(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.SELF_TEST_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.SELF_TEST_DIR_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setSelfTestDir(new File(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.SELF_TEST_DIR_PROPERTY_KEY)).getAbsoluteFile());
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.SINGLE_WINDOW_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setSingleWindow(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.SINGLE_WINDOW_PROPERTY_KEY));
        }

        int waitforPLTimeout =
                WiLConfiguration.getInstance().getInt(WiLConfiguration.MS_WAIT_FOR_PAGE_LOAD_PROPERTY_KEY);
        rcConfiguration.setTimeoutInSeconds(waitforPLTimeout / 1000);

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.TRUST_ALL_SSL_CERTIFICATES_PROPERTY_KEY)
                .isEmpty()) {
            rcConfiguration.setTrustAllSSLCertificates(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.TRUST_ALL_SSL_CERTIFICATES_PROPERTY_KEY));
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.USER_EXTENSIONS_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setUserExtensions(new File(WiLConfiguration.getInstance().getString(
                    WiLConfiguration.USER_EXTENSIONS_PROPERTY_KEY)).getAbsoluteFile());
        }

        if (!WiLConfiguration.getInstance().getString(WiLConfiguration.USER_JS_INJECTION_PROPERTY_KEY).isEmpty()) {
            rcConfiguration.setUserJSInjection(WiLConfiguration.getInstance().getBoolean(
                    WiLConfiguration.USER_JS_INJECTION_PROPERTY_KEY));
        }
        return rcConfiguration;
    }

    /**
     * Prepares the test environment.
     * 
     * @throws IOException
     *             thrown when the properties files could not be read
     */
    @BeforeSuite(alwaysRun = true)
    protected void startup() throws IOException {
        startSeleniumServer();
    }

    /**
     * Stop reporting for a test class that was run.
     */
    @AfterClass(alwaysRun = true)
    protected void stopRecording() {
        if (this.selenium != null) {
            this.selenium.stopRecordingTest();
            this.selenium.stop();
        }

        Mailer.closeAllConnections();
        Database.getInstance().closeDatabaseConnections();
    }

    /**
     * Stops the Selenium server.
     */
    protected void stopSeleniumServer() {
        this.seleniumServer.stop();
    }
}
