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

package com.sprylab.webinloop.tests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.testng.TestException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sprylab.webinloop.WebInLoop;
import com.sprylab.webinloop.reporter.Reporter;

/**
 * @author rzimmer
 * 
 */
@Test
public class SeleniumTest {

    private List<String> forbiddenTests = null;

    @BeforeTest
    public void setUp() {
        forbiddenTests =
                Arrays.asList("DogfoodTestRefreshFrame", "TestBaseUrl", "TestBreakPoint", "TestFailures",
                        "TestPauseAndResume", "TestRunFailedTests", "TestRunSuccessfulTests");
    }

    @Test
    public void testSeleniumSuite() throws TransformerException, IOException {
        // essential to compile Selenium tests
        System.setProperty("javac.classpath", System.getProperty("surefire.test.class.path", ""));

        WebInLoop webInLoop = new WebInLoop();

        // configure WebInLoop
        webInLoop.setConfigDir(new File("src/test/resources/configs/SeleniumSuite/"));

        File testsuiteFile = new File("target/test-classes/SeleniumSuite/tests/TestSuite.html");

        // remove all "forbidden" tests
        String testsuite = FileUtils.readFileToString(testsuiteFile);
        for (String forbiddenTest : forbiddenTests) {
            testsuite = testsuite.replaceFirst("<tr>.*" + forbiddenTest + ".*</tr>", "");
        }
        FileUtils.writeStringToFile(testsuiteFile, testsuite);

        // set testsuite
        List<File> inputFiles = Arrays.asList(testsuiteFile);
        webInLoop.setInputFiles(inputFiles);

        // execute WebInLoop
        webInLoop.start();

        // ask reporter if some tests have failed
        if (Reporter.getInstance().hasFailed()) {
            throw new TestException("At least one test has failed.");
        }
    }
}
