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

package com.sprylab.webinloop.maven;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.testng.TestException;

import com.sprylab.webinloop.WebInLoop;
import com.sprylab.webinloop.WiLConfiguration;
import com.sprylab.webinloop.reporter.Reporter;

/**
 * This is the WebInLoop Maven plugin MOJO.
 * 
 * @goal webinloop
 * @requiresDependencyResolution compile
 * 
 */
public class WebInLoopMojo extends AbstractMojo {

    /**
     * @parameter default-value="false"
     */
    private boolean translatorMode;

    /**
     * @parameter
     */
    private File configDir;

    /**
     * @parameter default-value="false"
     */
    private boolean debugMode;

    /**
     * @parameter default-value="sources"
     */
    private File outputDir;

    /**
     * @parameter
     * @required
     */
    private File[] inputFiles;

    /**
     * @parameter
     */
    private String browser;

    /**
     * @parameter
     */
    private Map<?, ?> additionalProperties;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter default-value="false"
     */
    private boolean testFailureIgnore;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            // set classpath to make javac resolve all needed artifacts for
            // compiling
            String classpathString = StringUtils.join(project.getCompileClasspathElements(), File.pathSeparator);
            System.setProperty("javac.classpath", classpathString);

            // set additional system properties
            if (additionalProperties != null) {
                System.getProperties().putAll(additionalProperties);
            }

            WebInLoop webInLoop = new WebInLoop();

            // configure WebInLoop
            webInLoop.setTranslatorMode(translatorMode);
            webInLoop.setDebugMode(debugMode);
            webInLoop.setOutputDir(outputDir);
            webInLoop.setConfigDir(configDir);

            if (browser != null && !browser.isEmpty()) {
                WiLConfiguration.getInstance().setProperty(WiLConfiguration.BROWSER_PROPERTY_KEY, browser);
            }

            webInLoop.setInputFiles(Arrays.asList(inputFiles));

            // execute WebInLoop
            webInLoop.start();

            // ask reporter if some tests have failed
            if (!testFailureIgnore && Reporter.getInstance().hasFailed()) {
                throw new TestException("At least one test has failed.");
            }
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
