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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLPropertiesConfiguration;

/**
 * Class that manages the actual configuration application-wide.
 * 
 * @author rzimmer
 */
public class WiLConfiguration extends XMLPropertiesConfiguration {

    /**
     * Property key for avoidProxy.
     */
    public static final String AVOID_PROXY_PROPERTY_KEY = "avoidProxy";

    /**
     * Property key for start page.
     */
    public static final String BASE_URL_PROPERTY_KEY = "baseUrl";

    /**
     * Property key for browser to use.
     */
    public static final String BROWSER_PROPERTY_KEY = "browser";

    /**
     * Property key for browserSideLogEnabled.
     */
    public static final String BROWSER_SIDE_LOG_ENABLED_PROPERTY_KEY = "browserSideLogEnabled";

    /**
     * Property key for cacheMailConnections
     */
    public static final String CACHE_MAIL_CONNECTION_PROPERTY_KEY = "cacheMailConnections";

    /**
     * The property key for the configuration directory.
     */
    public static final String CONFIG_DIR_PROPERTY_KEY = "config.dir";

    /**
     * The property key for the flag for creating a timestamp directory.
     */
    public static final String CREATE_TIMESTAMP_DIR_PROPERTY_KEY = "createTimestampDir";

    /**
     * Property key for the list of DB connections.
     */
    public static final String DB_CONNECTIONS_PROPERTY_KEY = "dbConnections";

    /**
     * Property key for the DB password.
     */
    public static final String DB_PASSWORD_PROPERTY_KEY = "password";

    /**
     * Name of the DB properties file.
     */
    public static final String DB_PROPERTY_FILE = "db.xml";

    /**
     * Property key for the DB user.
     */
    public static final String DB_USER_PROPERTY_KEY = "user";

    /**
     * Property key for the debug mode.
     */
    public static final String DEBUG_MODE_PROPERTY_KEY = "debugMode";

    /**
     * Property key for debugURL.
     */
    public static final String DEBUG_URL_PROPERTY_KEY = "debugURL";

    /**
     * Property key for dontInjectRegex.
     */
    public static final String DONT_INJECT_REGEX_PROPERTY_KEY = "dontInjectRegex";

    /**
     * Property key for dontTouchLogging.
     */
    public static final String DONT_TOUCH_LOGGING_PROPERTY_KEY = "dontTouchLogging";

    /**
     * Property key for ensureCleanSession.
     */
    public static final String ENSURE_CLEAN_SESSION_PROPERTY_KEY = "ensureCleanSession";

    /**
     * Property key for firefoxProfileTemplate.
     */
    public static final String FIREFOX_PROFILE_TEMPLATE_PROPERTY_KEY = "firefoxProfileTemplate";

    /**
     * Property key defining the folder of a mail account.
     */
    public static final String FOLDER_PROPERTY_KEY = "folder";

    /**
     * Property key for forcedBrowserMode.
     */
    public static final String FORCED_BROWSER_MODE_PROPERTY_KEY = "forcedBrowserMode";

    /**
     * Property key for honorSystemProxy.
     */
    public static final String HONOR_SYSTEM_PROXY_PROPERTY_KEY = "honorSystemProxy";

    /**
     * Property key for htmlSuite.
     */
    public static final String HTML_SUITE_PROPERTY_KEY = "htmlSuite";

    /**
     * Property key for interactive.
     */
    public static final String INTERACTIVE_PROPERTY_KEY = "interactive";

    /**
     * Property key for the JDBC driver.
     */
    public static final String JDBC_DRIVER_PROPERTY_KEY = "jdbcDriver";

    /**
     * Property key for the JDBC URL.
     */
    public static final String JDBC_URL_PROPERTY_KEY = "jdbcUrl";

    /**
     * Property key for jettyThreads.
     */
    public static final String JETTY_THREADS_PROPERTY_KEY = "jettyThreads";

    /**
     * Property key for the left window offset (for taking screenshots).
     */
    public static final String LEFT_WINDOW_OFFSET_PROPERTY_KEY = "leftWindowOffset";

    /**
     * Property key for the directory where all log files will be written to.
     */
    public static final String LOG_DIR_PROPERTY_KEY = "logDir";

    /**
     * Property key for the echo logging flag.
     */
    public static final String LOG_ECHO_PROPERTY_KEY = "logEcho";

    /**
     * Property key for logOutFileName.
     */
    public static final String LOG_OUT_FILE_NAME_PROPERTY_KEY = "logOutFileName";

    /**
     * Property file which saves this Mailer's properties.
     */
    public static final String MAILER_PROPERTY_FILE = "mailer.xml";

    /**
     * Property key for available mail connections.
     */
    public static final String MAIL_CONNECTIONS = "mailConnections";

    /**
     * Property key for the milliseconds to wait for pages to load.
     */
    public static final String MS_WAIT_FOR_PAGE_LOAD_PROPERTY_KEY = "milliSecondsToWaitForPageToLoad";

    /**
     * The property key for the flag for creating a timestamp directory.
     */
    public static final String NOTIFY_BY_MAIL_PROPERTY_KEY = "notifyByMail";

    /**
     * Property key for the package name.
     */
    public static final String PACKAGE_PROTERTY_KEY = "package";

    /**
     * Property key for portDriversShouldContact.
     */
    public static final String PORT_DRIVERS_SHOULD_CONTACT_PROPERTY_KEY = "portDriversShouldContact";

    /**
     * Property key for profilesLocation.
     */
    public static final String PROFILES_LOCATION_PROPERTY_KEY = "profilesLocation";

    /**
     * Name of the properties directory in JAR.
     */
    public static final String PROPERTIES_DIRECTORY = "properties";

    /**
     * Property key for proxyInjectionModeArg.
     */
    public static final String PROXY_INJECTION_MODE_ARG_PROPERTY_KEY = "proxyInjectionModeArg";

    /**
     * Property key defining which mail account should me used for mailing
     * report.
     */
    public static final String REPORT_MAIL_ACCOUNT_PROPERTY_KEY = "reportMailAccount";

    /**
     * Property key defining the format of the report mail attachment.
     */
    public static final String REPORT_MAIL_ATTACHMENT_PROPERTY_KEY = "reportMailAttachment";

    /**
     * Property key defining the sender of the report mail.
     */
    public static final String REPORT_MAIL_FROM_PROPERTY_KEY = "reportMailFrom";

    /**
     * Property key defining which one will get a mailed report.
     */
    public static final String REPORT_MAIL_TO_PROPERTY_KEY = "reportMailTo";

    /**
     * Property key for timeoutInSeconds.
     */
    public static final String RETRY_TIMEOUT_IN_SECONDS_PROPERTY_KEY = "timeoutInSeconds";

    /**
     * Property key for reuseBrowserSessions.
     */
    public static final String REUSE_BROWSER_SESSION_PROPERTY_KEY = "reuseBrowserSessions";

    /**
     * Property key for the screen shooting delay in MS.
     */
    public static final String SCREENSHOT_DELAY_MS_PROPERTY_KEY = "screenhotDelayMs";

    /**
     * Property key for the directory where screenshot report will be stored.
     */
    public static final String SCREENSHOT_DIR_PROPERTY_KEY = "screenshotDir";

    /**
     * Name of the Selenium configuration file.
     */
    public static final String SELENIUM_PROPERTY_FILE = "selenium.xml";

    /**
     * Property key for server host.
     */
    public static final String SELENIUM_SERVER_HOST_PROPERTY_KEY = "seleniumServerHost";

    /**
     * Property key for server port.
     */
    public static final String SELENIUM_SERVER_PORT_PROPERTY_KEY = "seleniumServerPort";

    /**
     * Name of the Selenium server configuration file.
     */
    public static final String SELENIUM_SERVER_PROPERTY_FILE = "seleniumServer.xml";

    /**
     * Property key for selfTestDir.
     */
    public static final String SELF_TEST_DIR_PROPERTY_KEY = "selfTestDir";

    /**
     * Property key for selfTest.
     */
    public static final String SELF_TEST_PROPERTY_KEY = "selfTest";

    /**
     * Property key for singleWindow.
     */
    public static final String SINGLE_WINDOW_PROPERTY_KEY = "singleWindow";

    /**
     * Property key for taking entire page screenshots.
     */
    public static final String TAKE_ENTIRE_PAGE_SCREENSHOTS_PROPERTY_KEY = "takeEntirePageScreenshots";

    /**
     * Property key for the take screenshots flag.
     */
    public static final String TAKE_SCREENSHOTS_PROPERTY_KEY = "takeScreenshots";

    /**
     * Property key for the directory where TestNG report will be stored.
     */
    public static final String TESTNG_DIR_PROPERTY_KEY = "testNgOutputDir";

    /**
     * Name of the configuration file for creating TestNG classes.
     */
    public static final String TESTNG_PROPERTY_FILE = "testngClass.xml";

    /**
     * The property key for the TestNG test suite name.
     */
    public static final String TESTSUITE_NAME_PROPERTY_KEY = "testsuiteName";

    /**
     * The property key for the translator output directory.
     */
    public static final String TRANSLATOR_OUTPUT_DIR_PROPERTY_KEY = "translatorOutputDir";

    /**
     * Property key for trustAllSSLCertificates.
     */
    public static final String TRUST_ALL_SSL_CERTIFICATES_PROPERTY_KEY = "trustAllSSLCertificates";

    /**
     * Property key for the upper window offset (for taking screenshots).
     */
    public static final String UPPER_WINDOW_OFFSET_PROPERTY_KEY = "upperWindowOffset";

    /**
     * Property key defining the url of a mail account.
     */
    public static final String URL_PROPERTY_KEY = "url";

    /**
     * Property key for userExtensions.
     */
    public static final String USER_EXTENSIONS_PROPERTY_KEY = "userExtensions";

    /**
     * Property key for userJSInjection.
     */
    public static final String USER_JS_INJECTION_PROPERTY_KEY = "userJSInjection";

    /**
     * Property key for the window height.
     */
    public static final String WINDOW_HEIGHT_PROPERTY_KEY = "windowHeight";

    /**
     * Property key for the window width.
     */
    public static final String WINDOW_WIDTH_PROPERTY_KEY = "windowWidth";

    /**
     * Property key for the chrome driver executable path.
     */
    public static final String CHROME_DRIVER_EXE_PROPERTY_KEY = "webdriver.chrome.driver";

    /**
     * The actual singleton instance.
     */
    private static final WiLConfiguration INSTANCE = new WiLConfiguration();

    /**
     * Name of the configuration file for the screenshots.
     */
    private static final String SCREENSHOT_PROPERTY_FILE = "screenshot.xml";

    /**
     * Gets the Configuration instance.
     * 
     * @return the configuration instance
     */
    public static WiLConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * The directory, where to look for configuration files.
     */
    private File configDir = new File(System.getProperty("user.dir"));

    /**
     * Flag indication if in debugMode or not.
     */
    private boolean debugMode = false;

    /**
     * Protected constructor.
     */
    protected WiLConfiguration() {
        initConfiguration();
    }

    /**
     * Reads options from the property files and the system properties. System
     * properties have priority.
     */
    public void initConfiguration() {
        try {
            // remove old properties
            this.clear();

            InputStream testNgResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + TESTNG_PROPERTY_FILE);
            this.load(testNgResourceStream);

            File testngXml = new File(configDir, TESTNG_PROPERTY_FILE);
            if (testngXml.exists()) {
                // user has specified it's own testng.xml - use it
                this.load(new FileInputStream(testngXml), true);
            }

            InputStream seleniumServerResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + SELENIUM_SERVER_PROPERTY_FILE);
            this.load(seleniumServerResourceStream);

            File seleniumServerXml = new File(configDir, SELENIUM_SERVER_PROPERTY_FILE);
            if (seleniumServerXml.exists()) {
                // user has specified it's own seleniumServer.xml - use it
                this.load(new FileInputStream(seleniumServerXml), true);
            }

            InputStream seleniumResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + SELENIUM_PROPERTY_FILE);
            this.load(seleniumResourceStream);

            File seleniumXml = new File(configDir, SELENIUM_PROPERTY_FILE);
            if (seleniumXml.exists()) {
                // user has specified it's own selenium.xml - use it
                this.load(new FileInputStream(seleniumXml), true);
            }

            InputStream screenshotResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + SCREENSHOT_PROPERTY_FILE);
            this.load(screenshotResourceStream);

            File screenshotXml = new File(configDir, SCREENSHOT_PROPERTY_FILE);
            if (screenshotXml.exists()) {
                // user has specified a screenshot.xml - use it
                this.load(new FileInputStream(screenshotXml), true);
            }

            InputStream dbResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + DB_PROPERTY_FILE);
            this.load(dbResourceStream);

            File dbXml = new File(configDir, DB_PROPERTY_FILE);
            if (dbXml.exists()) {
                // user has specified a db.xml - use it
                this.load(new FileInputStream(dbXml), true);
            }

            InputStream mailerResourceStream =
                    getClass().getResourceAsStream("/" + PROPERTIES_DIRECTORY + "/" + MAILER_PROPERTY_FILE);
            this.load(mailerResourceStream);

            File mailerXml = new File(configDir, MAILER_PROPERTY_FILE);
            if (mailerXml.exists()) {
                // user has specified a mailer.xml - use it
                this.load(new FileInputStream(mailerXml), true);
            }

            this.setProperty(CONFIG_DIR_PROPERTY_KEY, configDir.getAbsolutePath());
            this.setProperty(DEBUG_MODE_PROPERTY_KEY, String.valueOf(debugMode));

            // overwrite with system properties
            ConfigurationUtils.copy(new SystemConfiguration(), this);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param configDir
     *            the configDir to set
     */
    public void setConfigDir(File configDir) {
        if (configDir != null && configDir.isDirectory()) {
            this.configDir = configDir;
        } else {
            this.configDir = new File(System.getProperty("user.dir"));
        }
        try {
            initConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param debugMode
     *            the debugMode to set
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Converts this configuration to a property map.
     * 
     * @return this configuration as a property map
     */
    public Properties toProperties() {
        return ConfigurationConverter.getProperties(this);
    }

    /**
     * Loads properties from an Java XML property file specified by an
     * {@link FileInputStream}.
     * 
     * @param in
     *            the file input stream pointing to a XML property file
     * @param overwrite
     *            flag indicating if old properties are overwritten bye the new
     *            ones of the same name or if the new ones should be appended to
     *            a list
     * @throws ConfigurationException
     *             if an error occurs during the load operation
     */
    private void load(FileInputStream in, boolean overwrite) throws ConfigurationException {
        if (overwrite) {
            // overwrite old values
            XMLPropertiesConfiguration temp = new XMLPropertiesConfiguration();
            temp.load(in);

            ConfigurationUtils.copy(temp, this);
        } else {
            // append new values to existing keys
            super.load(in);
        }
    }
}
