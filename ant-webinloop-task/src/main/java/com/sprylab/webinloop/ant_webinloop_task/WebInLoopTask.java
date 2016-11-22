/*******************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Copyright 2009 by sprylab technologies GmbH
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

package com.sprylab.webinloop.ant_webinloop_task;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import com.sprylab.webinloop.WebInLoop;

/**
 * Ant task that executes WebInLoop.
 * 
 * @author rzimmer
 * 
 */
public class WebInLoopTask extends Task {

    private File configDir;

    private boolean debugMode;

    private List<Path> inputFiles = new ArrayList<Path>();

    private File outputDir;

    private boolean translatorMode;

    /**
     * Adds a path to the inputFiles list.
     * 
     * @param path
     *            the path to add
     */
    public void addPath(Path path) {
        inputFiles.add(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        try {
            WebInLoop webInLoop = new WebInLoop();

            // configure WebInLoop
            webInLoop.setTranslatorMode(translatorMode);
            webInLoop.setDebugMode(debugMode);
            webInLoop.setOutputDir(outputDir);
            webInLoop.setConfigDir(configDir);
            webInLoop.setInputFiles(toFileList(inputFiles));

            // execute WebInLoop
            try {
                webInLoop.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * @param configDir
     *            the configDir to set
     */
    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    /**
     * @param debugMode
     *            the debugMode to set
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * @param inputFiles
     *            the inputFiles to set
     */
    public void setInputFiles(List<Path> inputFiles) {
        this.inputFiles = inputFiles;
    }

    /**
     * @param outputDir
     *            the outputDir to set
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @param translatorMode
     *            the translatorMode to set
     */
    public void setTranslatorMode(boolean translatorMode) {
        this.translatorMode = translatorMode;
    }

    public List<File> toFileList(List<Path> paths) {
        // using set to avoid duplicates
        Set<File> files = new HashSet<File>();

        for (Path path : paths) {
            String[] fileNames = path.list();
            for (int i = 0; i < fileNames.length; i++) {
                File file = new File(fileNames[i]);
                if (file.exists()) {
                    files.add(file);
                }
            }
        }

        return new ArrayList<File>(files);
    }
}
