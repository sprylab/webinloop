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

package com.sprylab.webinloop.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sprylab.webinloop.util.htmlparser.HTMLParser;

/**
 * Represents a model for a Selenium test suite.
 * 
 * @author rzimmer
 * 
 */
public class TestSuite {

    /**
     * Default name for a test suite.
     */
    private static final String DEFAULT_NAME = "Default Test Suite";

    /**
     * HTML-ID of the Selenium test suite table.
     */
    private static final String TESTSUITE_TABLE_ID = "suiteTable";

    /**
     * Factory method to create a new test suite model object from a file. This
     * method tries to parse the content of the file using FreeMarker. If the
     * file is not a Selenium HTML test suite, it assumes that it is a Selenium
     * HTML test case. In this case a default test suite is created and the test
     * case is added to it. In case of a Selenium test suite, all referenced
     * test cases are parsed, too.
     * 
     * @param file
     *            Selenium HTML test suite or test case
     * @return a new test suite object representing the passed file
     * @throws TransformerException
     *             if there are errors during the transformation
     * @throws FileNotFoundException
     *             if the provided file could not be found
     */
    public static TestSuite createFromFile(File file) throws FileNotFoundException, TransformerException {
        Document document = HTMLParser.parse(file);

        TestSuite testSuite = new TestSuite();

        // get test suite table element
        Element testSuiteTable = null;
        // workaround document.getElementById(TESTSUITE_TABLE_ID); not working
        NodeList tables = document.getElementsByTagName("table");
        for (int i = 0; i < tables.getLength(); i++) {
            Node table = tables.item(i);
            NamedNodeMap attributes = table.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Attr attribute = (Attr) attributes.item(j);
                if (attribute.getNodeName().equalsIgnoreCase("id")
                        && attribute.getNodeValue().equals(TESTSUITE_TABLE_ID)) {
                    testSuiteTable = (Element) table;
                    break;
                }
            }
        }

        if (testSuiteTable == null) {
            // HTML file is not a test suite, assume that is a test case
            // create default test suite with only one test case
            testSuite.setName(DEFAULT_NAME);
            testSuite.addTest(Test.createFromDocument(document, FilenameUtils.getBaseName(file.getName())));
        } else {
            // parse HTML test suite

            // test suite name lies in the first row of the test suite table
            NodeList tableRows = testSuiteTable.getElementsByTagName("tr");
            if (tableRows.getLength() > 1) {
                testSuite.setName(tableRows.item(0).getTextContent());
            }

            NodeList testSuiteTableLinks = testSuiteTable.getElementsByTagName("a");
            for (int i = 0; i < testSuiteTableLinks.getLength(); i++) {
                Element testCaseLinkElement = (Element) testSuiteTableLinks.item(i);
                File testCaseFile = new File(file.getParent(), testCaseLinkElement.getAttribute("href"));

                testSuite.addTest(Test.createFromFile(testCaseFile));
            }
        }

        return testSuite;
    }

    /**
     * Name of the test suite.
     */
    private String name = "";

    /**
     * List of all corresponding tests.
     */
    private List<Test> tests = new ArrayList<Test>();

    /**
     * Creating a {@link TestSuite} directly is not allowed. Use the factory
     * methods instead.
     */
    private TestSuite() {

    }

    /**
     * Adds a new test to the test suite. The test suite registers itself at the
     * test.
     * 
     * @param test
     *            the test to add
     */
    public void addTest(Test test) {
        if (!isTestCase(test.getName())) {
            test.setTestSuite(this);
            this.tests.add(test);
        }
    }

    /**
     * Returns true if the given name corresponds to a test case.
     * 
     * @param testCaseName
     *            the name of the test case to find
     * @return true if the given test case name really is a test case
     */
    private boolean isTestCase(String testCaseName) {
        for (Test test : this.tests) {
            if (test.getName().equals(testCaseName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds tests from another test suite to this test suite object.
     * 
     * @param testSuite
     *            test suite from which the tests should be added
     */
    public void addTestSuite(TestSuite testSuite) {
        List<Test> tests = testSuite.getTests();
        for (Test test : tests) {
            addTest(test);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the tests
     */
    public List<Test> getTests() {
        return this.tests;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param tests
     *            the tests to set
     */
    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
}
