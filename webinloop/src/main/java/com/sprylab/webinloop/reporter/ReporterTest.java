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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DurationFormatUtils;

import com.sprylab.webinloop.directives.Directive;

import freemarker.template.Template;

/**
 * This class creates a report for a single test.
 * 
 * @author rzimmer
 * 
 */
public class ReporterTest {

    /**
     * Number of total failed tests.
     */
    private static int failedTests = 0;

    /**
     * @return the failedTests
     */
    public static int getFailedTests() {
        return failedTests;
    }

    /**
     * Name of the baseUrl.
     */
    private String baseUrl = "";

    /**
     * Index of the actual directive that is been executing.
     */
    private int currentDirective = 0;

    /**
     * Directory for the test report.
     */
    private File currentTestDir = null;

    /**
     * Flag indicating if the test has been failed.
     */
    private boolean failed = false;

    /**
     * Name of the test.
     */
    private String name = "";

    /**
     * Stores the number of failed entries.
     */
    private int nrOfFailedEntries = 0;

    /**
     * Stores the number of run entries.
     */
    private int nrOfRunEntries = 0;

    /**
     * List of all entries of which the test consists of.
     */
    private List<ReporterEntry> reporterEntries = new ArrayList<ReporterEntry>();

    /**
     * Time when the test run was invoked.
     */
    private Date startTime = null;

    /**
     * Time when the test run was finished.
     */
    private Date stopTime = null;

    /**
     * Creates a new {@link ReporterTest} object.
     * 
     * @param name
     */
    public ReporterTest(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.startTime = new Date();
    }

    /**
     * Adds a new {@link ReporterEntry} representing the passed directive.
     * 
     * @param directive
     *            directive to add
     */
    public void addSourceDirective(Directive directive) {
        ReporterEntry newEntry = new ReporterEntry(directive);
        this.reporterEntries.add(newEntry);
    }

    /**
     * Called if a single test is finished. It writes index.html file for the
     * report of the test that has ended.
     */
    public void endTest() {
        setStopTime(new Date());

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("reporterTest", this);
        model.put("screenshotting", Reporter.getInstance().isScreenshotting());

        File summary = new File(this.currentTestDir, "index.html");
        Writer out = null;
        try {
            Template temp =
                    Reporter.getInstance().getConfiguration()
                            .getTemplate(Reporter.TEMPLATES_DIR + "/" + Reporter.TEST_TEMPLATE);
            out = new OutputStreamWriter(new FileOutputStream(summary), Charset.forName("UTF-8"));
            temp.process(model, out);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // close output stream if not null
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * @return the currentDirective
     */
    public int getCurrentDirective() {
        return this.currentDirective;
    }

    /**
     * @return the current reporter entry that is being executed
     */
    public ReporterEntry getCurrentReporterEntry() {
        return this.reporterEntries.get(this.currentDirective);
    }

    /**
     * @return the duration of the test run
     */
    public long getDuration() {
        return this.stopTime.getTime() - this.startTime.getTime();
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
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the nrOfFailedEntries
     */
    public int getNrOfFailedEntries() {
        return this.nrOfFailedEntries;
    }

    /**
     * @return the nrOfRunEntries
     */
    public int getNrOfRunEntries() {
        return this.nrOfRunEntries;
    }

    /**
     * @return the reporterEntries
     */
    public List<ReporterEntry> getReporterEntries() {
        return this.reporterEntries;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * @return the stopTime
     */
    public Date getStopTime() {
        return this.stopTime;
    }

    /**
     * @return the failed
     */
    public boolean isFailed() {
        return this.failed;
    }

    /**
     * Writes text to an specific log file.
     * 
     * @param text
     *            the text to log
     */
    public void logEcho(String text) {
        Reporter.getInstance().getLogFilePrinter().println(text);
    }

    /**
     * Records the actual step by adding a already taken screenshot to the
     * current {@link ReporterEntry}.
     * 
     * @param screenshotFile
     *            the screenshot file
     * @param description
     *            description of the screenshot
     */
    public void recordStep(String description, File screenshotFile) {
        ReporterErrorMessage errorMessage = new ReporterErrorMessage(description);

        if (Reporter.getInstance().isScreenshotting()) {
            errorMessage.setScreenshot(new ReporterScreenshot(this.currentTestDir, screenshotFile));
        }

        getCurrentReporterEntry().addErrorMessage(errorMessage);
    }

    /**
     * Records the actual step by adding a new screenshot to the current
     * {@link ReporterEntry}.
     * 
     * @param description
     *            description of the screenshot
     */
    public void recordStep(String description) {
        recordStep(description, null);
    }

    /**
     * @param currentDirective
     *            the currentDirective to set
     */
    public void setCurrentDirective(int currentDirective) {
        this.currentDirective = currentDirective;

        getCurrentReporterEntry().setRun(true);
        this.nrOfRunEntries++;
    }

    /**
     * @param failed
     *            the failed to set
     */
    public void setFailed(boolean failed) {
        if (this.failed != failed) {
            // only do something if this test has not failed yet
            this.failed = failed;
            if (this.failed) {
                failedTests++;
            }
        }
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @param stopTime
     *            the stopTime to set
     */
    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * Marks the current {@link ReporterEntry} and this test as failed. Normally
     * called, if a verification error has arised.
     */
    public void setVerificationFailed() {
        getCurrentReporterEntry().setFailed(true);
        this.nrOfFailedEntries++;

        setFailed(true);
    }

    /**
     * Starts a report for a single test. Registers itself to the
     * {@link Reporter}.
     */
    public void startTest() {
        this.currentTestDir = new File(Reporter.getInstance().getReportsDir(), getName());
        this.currentTestDir.mkdirs();

        Reporter.getInstance().addTest(this);
    }

    /**
     * Method that is called if a test fails. The test and the current
     * {@link ReporterEntry} will be set as failed and all remaining
     * {@link ReporterEntry}s as <i>not run</i>.
     */
    public void testFailed() {
        ReporterEntry currentEntry = getCurrentReporterEntry();
        currentEntry.setFailed(true);
        this.nrOfFailedEntries++;

        // set all remaining ReporterEntries to not run
        List<ReporterEntry> notRunList =
                this.reporterEntries.subList(this.currentDirective, this.reporterEntries.size() - 1);
        for (ReporterEntry reporterEntry : notRunList) {
            reporterEntry.setRun(false);
        }

        setFailed(true);
    }
}
