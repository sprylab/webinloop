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

/**
 * Represents an error message in a test report.
 * 
 * @author rzimmer
 * 
 */
public class ReporterErrorMessage {

    /**
     * The error message.
     */
    private String errorMessage = "";

    /**
     * The corresponding screenshot.
     */
    private ReporterScreenshot screenshot = null;

    /**
     * Creates an empty {@link ReporterErrorMessage} object.
     */
    public ReporterErrorMessage() {
        // do nothing
    }

    /**
     * Initializes an {@link ReporterErrorMessage} object with the given values.
     * 
     * @param errorMessage
     *            the error message
     * @param screenshot
     *            the corresponding screenshot
     */
    public ReporterErrorMessage(String errorMessage, ReporterScreenshot screenshot) {
        this.errorMessage = errorMessage;
        this.screenshot = screenshot;
    }

    /**
     * Initializes an {@link ReporterErrorMessage} object with the error
     * message.
     * 
     * @param errorMessage
     *            the error message
     */
    public ReporterErrorMessage(String errorMessage) {
        this(errorMessage, null);
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the screenshot
     */
    public ReporterScreenshot getScreenshot() {
        return this.screenshot;
    }

    /**
     * @param screenshot
     *            the screenshot to set
     */
    public void setScreenshot(ReporterScreenshot screenshot) {
        this.screenshot = screenshot;
    }
}
