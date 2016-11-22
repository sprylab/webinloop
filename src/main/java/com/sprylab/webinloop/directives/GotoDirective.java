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
 * Class representing a goto directive.
 * 
 * @author bkolb
 * 
 */
public class GotoDirective extends AbstractTypedDirective {

    public final static String[] keywords = {"goto" };

    /**
     * Creates a new action directive.
     * 
     * @param command
     *            the command
     * @param target
     *            the target
     * @param value
     *            the value
     */
    public GotoDirective(String command, String target, String value) {
        super(command, target, value);

        this.directiveName = command;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sprylab.webinloop.directives.AbstractTypedDirective#getType()
     */
    @Override
    public String getType() {
        return "Goto";
    }
}
