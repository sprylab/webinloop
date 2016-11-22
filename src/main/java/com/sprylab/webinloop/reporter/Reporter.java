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

package com.sprylab.webinloop.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import com.sprylab.webinloop.WiLConfiguration;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * This class produces an HTML test report. For report generation the FreeMarker
 * library is used.
 * 
 * The report is divided into a summary that lists all run tests and a page for
 * every single test that displays the screenshots.
 * 
 * This class is a singleton.
 */
public final class Reporter {

    /**
     * Name of the index html file to create.
     */
    public static final String INDEX_HTML_FILE = "index.html";

    /**
     * Name of the templates directory.
     */
    public static final String TEMPLATES_DIR = "templates";

    /**
     * Name of template for single test.
     */
    public static final String TEST_TEMPLATE = "test.ftl";

    /**
     * Name of the log file for echo commands.
     */
    private static final String ECHO_LOG_FILE = "echoLog.txt";

    /**
     * Saves the singleton instance of this class.
     */
    private static final Reporter INSTANCE = new Reporter();

    /**
     * Name of summary template.
     */
    private static final String SUMMARY_TEMPLATE = "summary.ftl";;

    /**
     * All files that should be copied from the directory specified by
     * TEMPLATES_DIR to the reportsDir.
     */
    private static final String[] templateFiles = {"css/style.css", "css/img/arrow_down.png",
        "css/img/arrow_down_hover.png", "css/img/arrow_right.png", "css/img/arrow_right_hover.png",
        "css/img/failed.png", "css/img/logo.png", "css/img/shadow.gif", "css/img/shadowAlpha.png",
        "css/img/success.png", "css/slimbox/closelabel.gif", "css/slimbox/loading.gif", "css/slimbox/nextlabel.gif",
        "css/slimbox/prevlabel.gif", "css/slimbox/slimbox2.css", "js/jquery.js", "js/slimbox2.js", "js/webinloop.js" };

    /**
     * Gets the Reporter instance.
     * 
     * @return the reporter instance
     */
    public static Reporter getInstance() {
        return INSTANCE;
    }

    /**
     * List to store information about every single test for the test summary.
     */
    public List<ReporterTest> reporterTests = new ArrayList<ReporterTest>();

    /**
     * Configuration of template engine.
     */
    private freemarker.template.Configuration configuration = new freemarker.template.Configuration();

    /**
     * A writer for echo commands to a log file.
     */
    private PrintWriter logFilePrinter = null;

    /**
     * Directory where the report data will be stored.
     */
    private File reportsDir = null;

    /**
     * Flag for taking screenshots.
     */
    private boolean screenshotting = true;

    /**
     * Instantiating of this class is disallowed.
     */
    private Reporter() {

    }

    /**
     * Adds a {@link ReporterTest} to the list of {@link ReporterTest}s.
     * 
     * @param test
     *            the {@link ReporterTest} to add
     */
    public void addTest(ReporterTest test) {
        this.reporterTests.add(test);
    }

    /**
     * Returns the template configuration.
     * 
     * @return the configuration
     */
    public freemarker.template.Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Calculates the difference in seconds between the start of the first and
     * the end of the last test and returns it.
     * 
     * @return total test run duration of the test suite in seconds
     */
    public long getDuration() {
        long result = 0;
        int size = this.reporterTests.size();
        if (size > 0) {
            result =
                    this.reporterTests.get(size - 1).getStopTime().getTime()
                            - this.reporterTests.get(0).getStartTime().getTime();
        }
        return result;
    }

    /**
     * Returns the total duration as a human readable string.
     * 
     * @return the duration as string
     */
    public String getDurationString() {
        return DurationFormatUtils.formatDurationWords(getDuration(), true, true);
    }

    /**
     * Returns number of failed tests.
     * 
     * @return number of failed tests.
     */
    public int getFailedTests() {
        return ReporterTest.getFailedTests();
    }

    /**
     * @return the logFilePrinter
     */
    public PrintWriter getLogFilePrinter() {
        if (logFilePrinter == null) {
            // create printer if it not already exists
            try {
                this.logFilePrinter = new PrintWriter(new BufferedWriter(new FileWriter(getLogFile())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.logFilePrinter;
    }

    /**
     * @return the reporterTests
     */
    public List<ReporterTest> getReporterTests() {
        return this.reporterTests;
    }

    /**
     * @return the reportsDir
     */
    public File getReportsDir() {
        return this.reportsDir;
    }

    /**
     * Checks if there were at least one failed test.
     * 
     * @return true if at least one test has failed else false
     */
    public boolean hasFailed() {
        return getFailedTests() != 0;
    }

    /**
     * @return the screenshotting
     */
    public boolean isScreenshotting() {
        return this.screenshotting;
    }

    /**
     * Called if all test were run. It generates the overview page of all run
     * test.
     */
    public void makeSummary() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("reporter", this);

        File summary = new File(getReportsDir(), INDEX_HTML_FILE);
        Writer out = null;
        try {
            Template temp = this.configuration.getTemplate(TEMPLATES_DIR + "/" + SUMMARY_TEMPLATE);
            out = new OutputStreamWriter(new FileOutputStream(summary), Charset.forName("UTF-8"));
            temp.process(model, out);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (logFilePrinter != null) {
                try {
                    logFilePrinter.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            File logFile = getLogFile();
            if (logFile.length() == 0) {
                FileUtils.deleteQuietly(logFile);
            }
        }
    }

    /**
     * Set up reporting environment for all tests. This means that directories
     * for the reports are created at passed location. This method has to be
     * called the first time this class is used.
     * 
     * @param reportsDir
     *            File handle that points to directory where the report will be
     *            stored.
     * @return
     * @throws IOException
     *             If setting up of resource directory fails.
     */
    public void prepare(File reportsDir) throws IOException {
        setReportsDir(reportsDir);
        this.reportsDir.mkdirs();

        // copy all files from the template directory to the reports dir

        // FIXME use a more generic approach to copy a whole directory from
        // file system or jar archive
        for (String file : templateFiles) {
            final URL urlToFile = this.getClass().getResource("/" + TEMPLATES_DIR + "/" + file);
            FileUtils.copyURLToFile(urlToFile, new File(reportsDir, file));
        }

        this.configuration.setClassForTemplateLoading(getClass(), "/");
        this.configuration.setObjectWrapper(new DefaultObjectWrapper());

        this.screenshotting = WiLConfiguration.getInstance().getBoolean(WiLConfiguration.TAKE_SCREENSHOTS_PROPERTY_KEY);

        File logFile = getLogFile();
        if (logFile.exists()) {
            FileUtils.deleteQuietly(logFile);
        }
    }

    /**
     * @param reportsDir
     *            the reportsDir to set
     */
    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }

    /**
     * @return the log file to use for echo commands
     */
    private File getLogFile() {
        return new File(reportsDir, ECHO_LOG_FILE);
    }
}
