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

package com.sprylab.webinloop.directives;

/**
 * Class representing a Selenium assertion directive.
 * 
 * @author rzimmer
 * 
 */
public class AssertionDirective extends AbstractTypedDirective {

    private enum Mode {
        ASSERT, VERIFY, WAIT_FOR;
    }

    /**
     * Array with all keywords used to identify a {@link AssertionDirective}.
     */
    public final static String[] keywords = {"assert", "verify", "waitFor" };

    /**
     * The actual mode of the assertion directive.
     */
    private Mode mode = null;

    /**
     * Flag indication if the assertion has to be negated.
     */
    private boolean negate = false;

    /**
     * Creates a new assertion directive and sets the corresponding assertion
     * mode.
     * 
     * @param command
     *            the command
     * @param target
     *            the target
     * @param value
     *            the value
     */
    public AssertionDirective(String command, String target, String value) {
        super(command, target, value);

        if (command.startsWith(keywords[0])) {
            this.mode = Mode.ASSERT;
        } else if (command.startsWith(keywords[1])) {
            this.mode = Mode.VERIFY;
        } else if (command.startsWith(keywords[2])) {
            this.mode = Mode.WAIT_FOR;
        }

        setDirectiveName(keywords);

        this.negate = this.directiveName.contains("Not");

        if (this.negate) {
            this.directiveName = this.directiveName.replaceFirst("Not", "");
        }
    }

    /**
     * @return the mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * @return the negate
     */
    public boolean isNegate() {
        return this.negate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sprylab.webinloop.directives.AbstractTypedDirective#getType()
     */
    @Override
    public String getType() {
        return "Assertion";
    }
}
