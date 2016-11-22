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

package com.sprylab.webinloop.util.htmlparser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.lang.StringUtils;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * This class helps parsing a HTML file.
 * 
 * @author rzimmer
 * 
 */
public class HTMLParser {

    /**
     * Parses a HTML file and returns the corresponding {@link Document}.
     * 
     * @param file
     *            the HTML file to parse
     * @return the document object
     * @throws TransformerException
     *             if the HTML file could not be transformed correctly
     * @throws FileNotFoundException
     *             if the HTML file is not found
     */
    public static Document parse(File file) throws TransformerException, FileNotFoundException {
        BufferedInputStream bufferedFileInputStream = new BufferedInputStream(new FileInputStream(file));
        Reader inputFileReader =
                new BufferedReader(new InputStreamReader(bufferedFileInputStream, Charset.forName("utf-8")));

        XMLReader reader = new Parser();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        DOMResult result = new DOMResult();
        transformer.transform(new SAXSource(reader, new InputSource(inputFileReader)), result);

        // result is a Document element
        return (Document) result.getNode();

    }

    /**
     * Replaces in a string all repeating whitespaces with a single one, trims
     * it and cleans <code>&nbsp;</code> by replacing all <code>&nbsp;</code>
     * with normal spaces.
     * 
     * @param string
     *            the string to trim and clean
     * @return the trimmed and cleaned string
     */
    public static String trimAndCleanNbsp(String string) {
        // replace repeated whitespaces with a single one
        string = string.replaceAll("\\s{2,}", " ");

        // &nbsp; to whitespace
        string = StringUtils.replaceChars(string, (char) 160, (char) 32);

        // return trimmed version
        return StringUtils.trimToEmpty(string);
    }

    /**
     * Protect default constructor.
     */
    protected HTMLParser() {
        // do nothing
    }
}
