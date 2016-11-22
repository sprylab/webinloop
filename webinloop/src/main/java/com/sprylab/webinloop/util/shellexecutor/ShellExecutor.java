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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Executes a program or script on the shell.
 * 
 * @author rzimmer
 * 
 */
public class ShellExecutor {

    /**
     * Protected constructor.
     */
    protected ShellExecutor() {
        // do nothing
    }

    /**
     * Runs a programm or a script on a shell.
     * 
     * @param shell
     *            the command line
     * @return the corresponding {@link ShellExecutionResult}
     * @throws IOException
     *             when the command line could not be executed
     */
    public static ShellExecutionResult run(String shell) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        CommandLine cmdLine = CommandLine.parse(shell);
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(output, error);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        int exitCode = 0;
        try {
            exitCode = executor.execute(cmdLine);
        } catch (ExecuteException e) {
            // retrieve exit code from exception
            exitCode = e.getExitValue();
        }

        return new ShellExecutionResult(exitCode, output.toString(), error.toString());
    }
}
