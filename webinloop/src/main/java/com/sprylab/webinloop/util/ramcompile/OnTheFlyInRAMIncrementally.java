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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang.StringUtils;

/*
 * Demo of on-the-fly, all-in-RAM compilation (no disk files used).
 * Based on an example by Piotr Kobzda at
 *
 http://groups.google.com/group/pl.comp.lang.java/msg/d37010d1cce043d0
 *
 * This demo modifies Piotr's code to work incrementally. Each class is
 * compiled on-the-fly all-in-RAM and in its own compilation unit.
 Newer
 * classes can call or reference older classes.
 *
 * The intended application is a custom scripting language, where bits
 * of script arrive one at a time and cannot be batched up. We want to
 * compile each one as it arrives, and be able to use it at once. Also,
 * each new bit must also be able to call any of the
 previously-compiled
 * bits.
 *
 * The demo compiles two classes, Hello1 and Hello2. Hello1 calls
 * Hello2. Hello2 is compiled first, in one compiler task. Then Hello1
 * is compiled, in another compiler task. Finally Hello1 is loaded and
 * run.
 *
 * Written and debugged against Java 1.6.0 Beta 2 build 86, in Eclipse
 * 3.2 Jim Goodwin July 25 2006
 */

public class OnTheFlyInRAMIncrementally {

    /*
     * Help routine to convert a string to a URI.
     */
    static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> compileClassFromString(String source, String className, String packageName)
        throws CompileException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new RuntimeException("No compiler found.");
        }

        final Map<String, JavaFileObject> output = new HashMap<String, JavaFileObject>();

        ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<RAMClassLoader>() {

            public RAMClassLoader run() {
                return new RAMClassLoader(output);
            }
        });

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        StandardJavaFileManager sjfm = compiler.getStandardFileManager(diagnostics, null, null);
        JavaFileManager jfm = new RAMFileManager(sjfm, output, loader);

        SourceJavaFileObject javaSource = new SourceJavaFileObject(className, source);

        List<String> options = new ArrayList<String>();
        String classPath = System.getProperty("javac.classpath");
        if (classPath != null && !classPath.equals("")) {
            options.add("-cp");
            options.add(classPath);
        }

        CompilationTask task = compiler.getTask(null, jfm, diagnostics, options, null, Arrays.asList(javaSource));

        if (!task.call()) {
            throw new CompileException("Compilation of " + className + " failed:\n"
                    + StringUtils.join(diagnostics.getDiagnostics(), "\n"));
        }

        try {
            Class<?> c = Class.forName(packageName + "." + className, false, loader);
            return c;
        } catch (ClassNotFoundException e) {
            throw new CompileException("Compilation of " + className + " failed\n" + e.getMessage());
        }
    }
}
