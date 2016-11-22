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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.tools.SimpleJavaFileObject;

/**
 * A JavaFileObject that uses RAM instead of disk to store the file. It gets
 * written to by the compiler, and read from by the loader.
 */
public class RAMJavaFileObject extends SimpleJavaFileObject {

    ByteArrayOutputStream baos;

    RAMJavaFileObject(String name, Kind kind) {
        super(OnTheFlyInRAMIncrementally.toURI(name), kind);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException, IllegalStateException,
        UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream openInputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
        return new ByteArrayInputStream(this.baos.toByteArray());
    }

    @Override
    public OutputStream openOutputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
        return this.baos = new ByteArrayOutputStream();
    }

}
