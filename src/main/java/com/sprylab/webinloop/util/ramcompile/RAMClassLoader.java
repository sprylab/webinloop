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

package com.sprylab.webinloop.util.ramcompile;

import java.util.Map;

import javax.tools.JavaFileObject;

/**
 * A class loader that loads what's in the cache by preference, and if it can't
 * find the class there, loads from the standard parent.
 * 
 * It is important that everything in the demo use the same loader, so we pass
 * this to the JavaFileManager as well as calling it directly.
 */
public final class RAMClassLoader extends ClassLoader {

    private final Map<String, JavaFileObject> output;

    public RAMClassLoader(Map<String, JavaFileObject> output) {
        super(Thread.currentThread().getContextClassLoader());
        this.output = output;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        JavaFileObject jfo = this.output.get(name);
        if (jfo != null) {
            byte[] bytes = ((RAMJavaFileObject) jfo).baos.toByteArray();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
