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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sprylab.webinloop.directives.AbstractTypedDirective;
import com.sprylab.webinloop.directives.TypedDirectiveFactory;
import com.sprylab.webinloop.util.htmlparser.HTMLParser;

/**
 * Represents a model for a Selenium test. Used to write Java TestNG classes
 * from it.
 * 
 * @author rzimmer
 * 
 */
public class Test {

    /**
     * Factory method to create a new test model object from a file. This method
     * tries to parse the content of the file using {@link HTMLParser}, it will
     * call {@link createFromDocument}.
     * 
     * @param file
     *            the Selenium HTML test case file
     * @return a new test model object representing the passed file
     * @throws TransformerException
     *             if there are errors during the transformation
     * @throws FileNotFoundException
     *             if the provided file could not be found
     */
    public static Test createFromFile(File file) throws FileNotFoundException, TransformerException {
        Document document = HTMLParser.parse(file);
        return Test.createFromDocument(document, FilenameUtils.getBaseName(file.getName()));
    }

    /**
     * Constructs a new test model object by analyzing the DOM tree of a
     * Selenium HTML test case.
     * 
     * @param document
     *            DOM tree to analyze
     * @param name
     *            name of the test model to create
     * @return a new test model object representing the passed node model
     */
    public static Test createFromDocument(Document document, String name) {
        Test testModel = new Test();
        testModel.setName(name);

        // get and set the base URL of the test case
        NodeList linkTags = document.getElementsByTagName("link");
        for (int i = 0; i < linkTags.getLength(); i++) {
            Node linkTag = linkTags.item(i);
            NamedNodeMap linkTagAttributes = linkTag.getAttributes();

            Node relItem = linkTagAttributes.getNamedItem("rel");
            if ((relItem != null) && relItem.getTextContent().equals("selenium.base")) {
                testModel.setBaseUrl(linkTagAttributes.getNamedItem("href").getTextContent());
                break;
            }
        }

        // get all rows, each row represents one instruction
        NodeList tableRowNodes = document.getElementsByTagName("tr");
        for (int i = 0; i < tableRowNodes.getLength(); i++) {
            Element tableRowNode = (Element) tableRowNodes.item(i);
            NodeList tableCellNodes = tableRowNode.getElementsByTagName("td");

            if (tableCellNodes.getLength() == 3) {
                // get command and parameters from table cells
                String command = HTMLParser.trimAndCleanNbsp(tableCellNodes.item(0).getTextContent());
                String target = HTMLParser.trimAndCleanNbsp(tableCellNodes.item(1).getTextContent());
                String value = HTMLParser.trimAndCleanNbsp(tableCellNodes.item(2).getTextContent());

                if (!command.isEmpty()) {
                    // process parsed commands
                    testModel.getDirectives().add(TypedDirectiveFactory.createFromCommand(command, target, value));
                }
            }
        }

        return testModel;
    }

    /**
     * The test case's base URL.
     */
    private String baseUrl = "";

    /**
     * List of all directives which are called during the test case's run.
     */
    private List<AbstractTypedDirective> directives = new ArrayList<AbstractTypedDirective>();

    /**
     * The name of the test case.
     */
    private String name = "";

    /**
     * The test suite to which this test model belongs to.
     */
    private TestSuite testSuite = null;

    /**
     * Creating a {@link Test} directly is not allowed. Use the factory methods
     * instead.
     */
    private Test() {

    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * @return the directives
     */
    public List<AbstractTypedDirective> getDirectives() {
        return this.directives;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the testSuite
     */
    public TestSuite getTestSuite() {
        return this.testSuite;
    }

    /**
     * @param baseUrl
     *            the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @param directives
     *            the directives to set
     */
    public void setDirectives(List<AbstractTypedDirective> directives) {
        this.directives = directives;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param testSuite
     *            the testSuite to set
     */
    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }
}
