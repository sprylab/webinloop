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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

/**
 * A JavaFileManager that presents the contents of the cache as a file system to
 * the compiler. To do this, it must do four things:
 * 
 * It remembers our special loader and returns it from getClassLoader()
 * 
 * It maintains our cache, adding class "files" to it when the compiler calls
 * getJavaFileForOutput
 * 
 * It implements list() to add the classes in our cache to the result when the
 * compiler is asking for the classPath. This is the key trick: it is what makes
 * it possible for the second compilation task to compile a call to a class from
 * the first task.
 * 
 * It implements inferBinaryName to give the right answer for cached classes.
 */

public class RAMFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private final Map<String, JavaFileObject> output;

    private final ClassLoader ldr;

    public RAMFileManager(StandardJavaFileManager sjfm, Map<String, JavaFileObject> output, ClassLoader ldr) {
        super(sjfm);
        this.output = output;
        this.ldr = ldr;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling)
        throws IOException {
        JavaFileObject jfo = new RAMJavaFileObject(name, kind);
        this.output.put(name, jfo);
        return jfo;
    }

    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location) {
        return this.ldr;
    }

    @Override
    public String inferBinaryName(Location loc, JavaFileObject jfo) {
        String result;

        if ((loc == StandardLocation.CLASS_PATH) && (jfo instanceof RAMJavaFileObject)) {
            result = jfo.getName();
        } else {
            result = super.inferBinaryName(loc, jfo);
        }

        return result;
    }

    @Override
    public Iterable<JavaFileObject> list(Location loc, String pkg, Set<Kind> kind, boolean recurse) throws IOException {

        Iterable<JavaFileObject> result = super.list(loc, pkg, kind, recurse);

        if ((loc == StandardLocation.CLASS_PATH) && pkg.equals("just.generated")
                && kind.contains(JavaFileObject.Kind.CLASS)) {
            ArrayList<JavaFileObject> temp = new ArrayList<JavaFileObject>(3);
            for (JavaFileObject jfo : result) {
                temp.add(jfo);
            }
            for (Entry<String, JavaFileObject> entry : this.output.entrySet()) {
                temp.add(entry.getValue());
            }
            result = temp;
        }
        return result;
    }
}
