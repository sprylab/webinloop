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
 * Factory class for creating typed directives.
 * 
 * @author rzimmer
 * 
 */
public class TypedDirectiveFactory {

    /**
     * Instantiating of this class is disallowed.
     */
    private TypedDirectiveFactory() {

    }

    /**
     * Iterates over all keywords and determines, if the passed command is
     * starts with one of the keywords.
     * 
     * @param command
     *            the command to check
     * @param keywords
     *            the array of keywords to compare with
     * @return true if the command starts with one of the keywords
     */
    private static boolean isDirective(String command, String[] keywords) {
        boolean result = false;

        // iterate over all keywords
        for (String keyword : keywords) {
            result |= command.startsWith(keyword);
        }

        return result;
    }

    /**
     * Factory method to create a new typed directive from a command. The
     * command will be analyzed and the corresponding directive will be
     * returned. If the command is not a {@link AssertionDirective} or a
     * {@link AccessorDirective}, it is assumed that it is a
     * {@link ActionDirective}.
     * 
     * @param command
     *            the command to analyze
     * @param target
     *            the target
     * @param value
     *            the value
     * @return new object either instance of {@link AssertionDirective} or
     *         {@link AccessorDirective} or {@link ActionDirective}.
     */
    public static AbstractTypedDirective createFromCommand(String command, String target, String value) {

        if (isDirective(command, AssertionDirective.keywords)) {
            return new AssertionDirective(command, target, value);
        } else if (isDirective(command, AccessorDirective.keywords)) {
            return new AccessorDirective(command, target, value);
        } else if (isDirective(command, GotoDirective.keywords)) {
            return new GotoDirective(command, target, value);
        } else {
            return new ActionDirective(command, target, value);
        }
    }
}
