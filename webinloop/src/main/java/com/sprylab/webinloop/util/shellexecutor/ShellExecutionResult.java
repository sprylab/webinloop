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

package com.sprylab.webinloop.util.shellexecutor;

/**
 * Encapsulates the output of a shell exectution.
 * 
 * @author rzimmer
 * 
 */
public class ShellExecutionResult {

    /**
     * The exit code.
     */
    private int exitCode = 0;

    /**
     * The standard output.
     */
    private String standardOutput = "";

    /**
     * The error output.
     */
    private String errorOutput = "";

    /**
     * Creates an empty {@link ShellExecutionResult} object.
     */
    public ShellExecutionResult() {
        // do nothing
    }

    /**
     * Initializes a new {@link ShellExecutionResult} with the given values.
     * 
     * @param exitCode
     *            the exit code
     * @param standardOutput
     *            the standard output
     * @param errorOutput
     *            the error code
     */
    public ShellExecutionResult(int exitCode, String standardOutput, String errorOutput) {
        this.exitCode = exitCode;
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode() {
        return this.exitCode;
    }

    /**
     * @param exitCode
     *            the exitCode to set
     */
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * @return the standardOutput
     */
    public String getStandardOutput() {
        return this.standardOutput;
    }

    /**
     * @param standardOutput
     *            the standardOutput to set
     */
    public void setStandardOutput(String standardOutput) {
        this.standardOutput = standardOutput;
    }

    /**
     * @return the errorOutput
     */
    public String getErrorOutput() {
        return this.errorOutput;
    }

    /**
     * @param errorOutput
     *            the errorOutput to set
     */
    public void setErrorOutput(String errorOutput) {
        this.errorOutput = errorOutput;
    }

}
