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
import java.net.URISyntaxException;

import javax.mail.MessagingException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.sprylab.webinloop.reporter.ReporterEntry;
import com.sprylab.webinloop.reporter.ReporterRecordLaterEntry;
import com.sprylab.webinloop.reporter.ReporterTest;
import com.sprylab.webinloop.util.db.Database;
import com.sprylab.webinloop.util.mailer.Mailer;
import com.sprylab.webinloop.util.mailer.Mailer.MailboxOperation;
import com.sprylab.webinloop.util.shellexecutor.ShellExecutionResult;
import com.sprylab.webinloop.util.shellexecutor.ShellExecutor;
import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;

/**
 * A implementation {@link DefaultSelenium} with support for new commands and
 * taking screenshots.
 * 
 * @author rzimmer
 * 
 */
public class ScreenshottingSelenium extends DefaultSelenium {

    /**
     * Represents a the screenshot moment <i>after</i> the command execution.
     */
    private static final String AFTER = "After";

    /**
     * Represents a the screenshot moment <i>before</i> the command execution.
     */
    private static final String BEFORE = "Before";

    /**
     * Indicates, if the entire page should be saved to an image file using the
     * built in browser screenshot mechanism (Firefox only) or not. If false,
     * screenshots will be made using AWT.
     */
    private boolean entirePageScreenshots = false;

    /**
     * Indicates if the echo commands should be logged to a file.
     */
    private boolean logEcho = false;

    /**
     * Helper object to request a later recording (next step=
     */
    private ReporterRecordLaterEntry recordLater = new ReporterRecordLaterEntry();

    /**
     * Reporter that records test run.
     */
    private ReporterTest reporter = null;

    /**
     * Stores current timeout in ms (default: 30000).
     */
    private String timeout = "30000";

    /**
     * Default constructor.
     * 
     * @param processor
     *            CommandProccessor
     * @param reporter
     *            Reporter to use for building report.
     */
    public ScreenshottingSelenium(CommandProcessor processor, ReporterTest reporter) {
        super(processor);
        this.reporter = reporter;

        try {
            initProperties();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connect to Selenium server using passed values and reporter.
     * 
     * @param serverHost
     *            host of the Selenium Server
     * @param serverPort
     *            port of the Selenium Server
     * @param browserStartCommand
     *            browser to start in Selenium syntax
     * @param browserURL
     *            the baseURL
     * @param reporter
     *            the reporter to collect the test results
     */
    public ScreenshottingSelenium(String serverHost, int serverPort, String browserStartCommand, String browserURL,
            ReporterTest reporter) {
        super(serverHost, serverPort, browserStartCommand, browserURL);
        this.reporter = reporter;

        try {
            initProperties();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thoughtworks.selenium.DefaultSelenium#captureEntirePageScreenshot
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void captureEntirePageScreenshot(String filename, String kwargs) {
        super.captureEntirePageScreenshot(filename, kwargs);

        // notify reporter of screenshot
        File screenshotFile = new File(filename);
        this.reporter.recordStep("Captured screenshot", screenshotFile);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thoughtworks.selenium.DefaultSelenium#click(java.lang.String)
     */
    @Override
    public void click(String locator) {
        final String command = "click";

        if (hasScreenhot(command, locator, BEFORE)) {
            recordStep("Before click on " + locator);
        }

        super.click(locator);

        if (hasScreenhot(command, locator, AFTER)) {
            recordStep("Clicked " + locator);
        }
    }

    /**
     * Prints out text on the console and to an file, if the logEcho
     * configuration variable is true.
     * 
     * @param text
     *            the text to output
     */
    public void echo(String text) {
        System.out.println(text);
        if (logEcho) {
            this.reporter.logEcho(text);
        }
    }

    /**
     * Executes a shell command.
     * 
     * @param script
     *            script or command to run
     * @throws Exception
     *             if script or command cannot be executed
     */
    public void execShell(String script) throws Exception {
        getShell(script);
    }

    /**
     * Executes a SQL statement.
     * 
     * @param sql
     *            the SQL statement to execute
     * @throws Exception
     *             if the data base operation to perform has failed
     */
    public void execSQL(String sql) throws Exception {
        // extract db connection from sql string
        String[] temp = splitSQLString(sql);

        recordStep("Executing statement: " + sql);

        int rows = Database.getInstance().execUpdate(temp[0], temp[1]);

        recordStep("Updated " + rows + " rows by statement: " + sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thoughtworks.selenium.DefaultSelenium#getAlert()
     */
    @Override
    public String getAlert() {
        return handlePrompts("alert");
    }

    /**
     * Returns the concatenation of all mails in the configured mail folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getAllMail(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, false, false, true);
    }

    /**
     * Returns unseen the concatenation of all mails in the configured mail
     * folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getAllMailUnseen(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, false, false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thoughtworks.selenium.DefaultSelenium#getConfirmation()
     */
    @Override
    public String getConfirmation() {
        return handlePrompts("confirmation");
    }

    /**
     * Returns the concatenation of all new (i.e. unseen) mails in the
     * configured mail folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getNewMail(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, true, false, true);
    }

    /**
     * Returns unseen the concatenation of all new (i.e. unseen) mails in the
     * configured mail folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getNewMailUnseen(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, true, false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thoughtworks.selenium.DefaultSelenium#getPrompt()
     */
    @Override
    public String getPrompt() {
        return handlePrompts("prompt");
    }

    /**
     * Returns the most recent mail in the configured mail folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getRecentMail(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, true, true, true);
    }

    /**
     * Returns unseen the most recent mail in the configured mail folder.
     * 
     * @param mailTargetID
     *            specifies what to get
     * @return mail content depending on target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     * @see {@link #getMail(String, boolean, boolean, boolean)}
     */
    public String[] getRecentMailUnseen(String mailTargetID) throws MessagingException, IOException {
        return Mailer.getMail(mailTargetID, true, true, false);
    }

    /**
     * Returns the shell error output of a command or script.
     * 
     * @param shell
     *            command or script to get the error output from
     * @return the shell error output
     * @throws Exception
     *             if the command or script cannot be executed
     */
    public String getShellError(String shell) throws Exception {
        ShellExecutionResult result = getShell(shell);
        String error = result.getErrorOutput().trim();

        recordStep("Shell error:\n" + error);
        return error;
    }

    /**
     * Returns the shell exit code of a command or script.
     * 
     * @param shell
     *            command or script to get the exit code from
     * @return the shell exit code
     * @throws Exception
     *             if the command or script cannot be executed
     */
    public String getShellExitCode(String shell) throws Exception {
        ShellExecutionResult result = getShell(shell);
        String exitCode = String.valueOf(result.getExitCode());

        recordStep("Shell exit code:\n" + exitCode);
        return exitCode;
    }

    /**
     * Returns the shell standard output of a command or script.
     * 
     * @param shell
     *            command or script to get the standard output from
     * @return the shell standard output
     * @throws Exception
     *             if the command or script cannot be executed
     */
    public String getShellOutput(String shell) throws Exception {
        ShellExecutionResult result = getShell(shell);
        String output = result.getStandardOutput().trim();

        recordStep("Shell output:\n" + output);
        return output;
    }

    /**
     * Get the first value stored in passed resultSet as a string. Cursor is
     * moved to first position automatically. ResultSet is closed after
     * retrieving value.
     * 
     * @param sql
     *            the SQL to query one value.
     * @return String value which is extracted from first position of passed
     *         ResultSet
     * @throws Exception
     *             if the data base operation to perform has failed
     */
    public String getSQL(String sql) throws Exception {
        // extract db connection from sql string
        String[] temp = splitSQLString(sql);

        return Database.getInstance().execQuery(temp[0], temp[1]);
    }

    /**
     * @return the timeout
     */
    public String getTimeout() {
        return this.timeout;
    }

    /**
     * Marks mails in mailbox with the given flag.
     * 
     * @param mailTargetID
     *            mail account and optional range (e.g. mail1:0-10)
     * @param flag
     *            the flag to set (read | unread | purge | answered | unanswered
     *            | recent | unrecent)
     * @throws MessagingException
     *             if connecting to mail account is not possible
     */
    public void markMessagesInMailbox(String mailTargetID, String flag) throws MessagingException {

        if (flag.equals("read")) {
            Mailer.executeMailboxOperation(MailboxOperation.READ, mailTargetID);
        } else if (flag.equals("unread")) {
            Mailer.executeMailboxOperation(MailboxOperation.UNREAD, mailTargetID);
        } else if (flag.equals("purge")) {
            Mailer.executeMailboxOperation(MailboxOperation.PURGE, mailTargetID);
        } else if (flag.equals("answered")) {
            Mailer.executeMailboxOperation(MailboxOperation.ANSWERED, mailTargetID);
        } else if (flag.equals("unanswered")) {
            Mailer.executeMailboxOperation(MailboxOperation.UNANSWERED, mailTargetID);
        } else if (flag.equals("recent")) {
            Mailer.executeMailboxOperation(MailboxOperation.RECENT, mailTargetID);
        } else if (flag.equals("unrecent")) {
            Mailer.executeMailboxOperation(MailboxOperation.UNRECENT, mailTargetID);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.thoughtworks.selenium.DefaultSelenium#open(java.lang.String)
     */
    @Override
    public void open(String url) {
        final String command = "open";

        String unescapedUrl = StringEscapeUtils.unescapeHtml(url);

        if (hasScreenhot(command, unescapedUrl, BEFORE)) {
            recordStep("Before oepening " + unescapedUrl);
        }
        super.open(unescapedUrl);

        if (hasScreenhot(command, unescapedUrl, AFTER)) {
            recordStep("Opened " + unescapedUrl);
        }
    }

    /**
     * Pauses the test execution thread.
     * 
     * @param ms
     *            time in milliseconds
     */
    public void pause(String ms) {
        long millis = Long.parseLong(ms);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Purges one or more mailboxes.
     * 
     * @param mailTargetIDs
     *            semicolon separated list of mail accounts to purge or * for
     *            all
     * @throws MessagingException
     *             thrown when there were errors accessing a mail account
     */
    public void purgeMailbox(String mailTargetIDs) throws MessagingException {
        Mailer.executeMailboxOperation(Mailer.MailboxOperation.PURGE, mailTargetIDs);
    }

    /**
     * Marks one or more mailboxes as read.
     * 
     * @param mailTargetIDs
     *            semicolon separated list of mail accounts to purge or * for
     *            all
     * @throws MessagingException
     *             thrown when there were errors accessing a mail account
     */
    public void readMailBox(String mailTargetIDs) throws MessagingException {
        Mailer.executeMailboxOperation(Mailer.MailboxOperation.READ, mailTargetIDs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thoughtworks.selenium.DefaultSelenium#setTimeout(java.lang.String)
     */
    @Override
    public void setTimeout(String timeout) {
        this.timeout = timeout;
        super.setTimeout(timeout);
    }

    /**
     * Start recording of test for report.
     */
    public void startRecordingTest() {
        this.reporter.startTest();
    }

    /**
     * Stops recording of test.
     */
    public void stopRecordingTest() {
        this.reporter.endTest();
    }

    /**
     * Calls {@link ScreenshottingSelenium#waitForPageToLoad(String)} with the
     * default time out value.
     */
    public void waitForPageToLoad() {
        waitForPageToLoad(WiLConfiguration.getInstance().getString(WiLConfiguration.MS_WAIT_FOR_PAGE_LOAD_PROPERTY_KEY));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.thoughtworks.selenium.DefaultSelenium#waitForPageToLoad(java.lang
     * .String)
     */
    @Override
    public void waitForPageToLoad(String timeout) {
        final String command = "waitForPageToLoad";

        if (hasScreenhot(command, "*", BEFORE)) {
            recordStep("Before waiting for page to load");
        }

        super.waitForPageToLoad(timeout);

        if (hasScreenhot(command, "*", AFTER)) {
            recordStep("Page loaded");
        }
    }

    /**
     * Returns a map with the result of the execution of a command or a script.
     * 
     * @param shell
     *            the command or script to execute
     * @return a map with the keys <code>ExitCode, Output and Error</code>
     * @throws IOException
     *             if the command or script cannot be opened
     */
    private ShellExecutionResult getShell(String shell) throws IOException {
        return ShellExecutor.run(shell);
    }

    /**
     * Handles all kinds of JavaScript prompts.
     * 
     * @param type
     *            type of prompt (alert, confirmation, prompt)
     * @return result
     */
    private String handlePrompts(String type) {
        if (recordLater.isRequired()) {
            // introduce a little delay in case we requested a later recording
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        String result = "";
        if (type.equals("alert")) {
            result = super.getAlert();
        } else if (type.equals("confirmation")) {
            result = super.getConfirmation();
        } else if (type.equals("prompt")) {
            result = super.getPrompt();
        }

        if (recordLater.isRequired()) {
            this.recordStep(recordLater.getMessage() + " (provoked a " + type + ")");
            recordLater.done();
        }

        return result;
    }

    private boolean hasScreenhot(String command, String target, String moment) {
        Boolean momentBoolean = WiLConfiguration.getInstance().getBoolean(command + moment, false);
        String targetList = WiLConfiguration.getInstance().getString(command + "Targets", "*");

        return momentBoolean && targetList.equals("*") || momentBoolean && targetList.contains(target);
    }

    /**
     * Reads options from the property files and the system properties. System
     * properties have priority.
     * 
     * @throws IOException
     *             if a property file cannot be opened
     * @throws URISyntaxException
     *             if a URI to a property file is wrong
     */
    private void initProperties() throws IOException, URISyntaxException {
        this.entirePageScreenshots =
                WiLConfiguration.getInstance().getBoolean(WiLConfiguration.TAKE_ENTIRE_PAGE_SCREENSHOTS_PROPERTY_KEY,
                        false);
        this.logEcho = WiLConfiguration.getInstance().getBoolean(WiLConfiguration.LOG_ECHO_PROPERTY_KEY, false);
    }

    /**
     * Splits a SQL string in it's components.
     * 
     * @param sql
     *            SQL string of format dbName:sqlQuery
     * @return array of length 2 (at position 0: database name, at position 1:
     *         SQl query)
     */
    private String[] splitSQLString(String sql) {
        String[] temp = sql.split(":", 2);

        Assert.assertTrue("No database connection supplied!", temp.length == 2);
        return temp;
    }

    /**
     * Records this step by creating a screenshot and assigning a message to it.
     * 
     * @param message
     *            the message to assign to screenshot
     */
    protected void recordStep(String message) {
        // delay taking screenshot if necessary
        long screenhotDelayMs =
                WiLConfiguration.getInstance().getLong(WiLConfiguration.SCREENSHOT_DELAY_MS_PROPERTY_KEY);

        if (screenhotDelayMs != 0) {
            try {
                Thread.sleep(screenhotDelayMs);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // take screenshot
        if (entirePageScreenshots) {

            int nextDirectiveIndex = reporter.getCurrentDirective() + 1;
            if (nextDirectiveIndex < reporter.getReporterEntries().size()) {
                ReporterEntry reporterEntry = reporter.getReporterEntries().get(nextDirectiveIndex);

                String nextCommand = reporterEntry.getDirective().getCommand();
                if (StringUtils.endsWithIgnoreCase(nextCommand, "alert")
                        || StringUtils.endsWithIgnoreCase(nextCommand, "confirmation")
                        || StringUtils.endsWithIgnoreCase(nextCommand, "prompt")) {
                    recordLater.recordLater(message);
                    return;
                }
            }

            // screenshot whole HTML canvas
            File tempScreenshotFile = null;
            try {
                tempScreenshotFile = File.createTempFile("selenium_screenshot" + Math.random(), ".png");

                // bypass the overwritten captureEntirePageScreenshot method to
                // prevent screenshot duplicates
                super.captureEntirePageScreenshot(tempScreenshotFile.getAbsolutePath(), "");
                this.reporter.recordStep(message, tempScreenshotFile);
            } catch (IOException e) {
                this.reporter.recordStep(message);
            } finally {
                // cleanup: delete temp file
                if (tempScreenshotFile != null && tempScreenshotFile.exists()) {
                    FileUtils.deleteQuietly(tempScreenshotFile);
                }
            }
        } else {
            // screenshot the traditional way
            this.reporter.recordStep(message);
        }
    }
}
