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
 * Base class for all typed Selenium directives.
 * 
 * @author rzimmer
 * 
 */
public abstract class AbstractTypedDirective extends Directive {
    /**
     * The actual directive without any suffixes or prefixes.
     */
    protected String directiveName = "";

    /**
     * Creates a new directive.
     * 
     * @param command
     * @param target
     * @param value
     */
    protected AbstractTypedDirective(String command, String target, String value) {
        super(command, target, value);
    }

    /**
     * @return the directiveName
     */
    public String getDirectiveName() {
        return this.directiveName;
    }

    public void setDirectiveName(String[] keywords) {
        StringBuilder regexBuilder = new StringBuilder("(");
        for (int i = 0; i < keywords.length; i++) {
            regexBuilder.append(keywords[i]);
            if (i < keywords.length - 1) {
                regexBuilder.append("|");
            }
        }
        regexBuilder.append(")");

        this.directiveName = this.command.replaceFirst(regexBuilder.toString(), "");
    }

    /**
     * Returns the type of the directive as string representation.
     * 
     * @return string containing directive type
     */
    public abstract String getType();
}
