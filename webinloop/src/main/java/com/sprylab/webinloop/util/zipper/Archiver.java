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

package com.sprylab.webinloop.util.zipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Utility package that handles archiving a file or directory. It makes use of
 * the <a href="http://commons.apache.org/compress/">Apache Commons Compress
 * API</a>.
 * 
 * @author rzimmer
 * 
 */
public class Archiver {

    /**
     * String for creating a JAR archive.
     */
    public static final String JAR = "jar";

    /**
     * String for creating a TAR archive.
     */
    public static final String TAR = "tar";

    /**
     * String for creating a ZIP archive.
     */
    public static final String ZIP = "zip";

    /**
     * Archives a file or directory.
     * 
     * @param input
     *            the file or directory to archive (directories are archived
     *            recursively).
     * @param archive
     *            the archive final (note: it's extension determines the archive
     *            format)
     * @throws ArchiveException
     *             if the archiver name is not known
     * @throws IOException
     *             if an error occurs while opening the file
     */
    public static void zip(File input, File archive) throws ArchiveException, IOException {
        final OutputStream out = new FileOutputStream(archive);

        // determine archive format from file extension
        String archiverName = FilenameUtils.getExtension(archive.getName());
        ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream(archiverName, out);

        if (input.isDirectory()) {
            // add all files and subdirectories from input
            Iterator<File> iterator = FileUtils.iterateFiles(input, null, true);
            while (iterator.hasNext()) {
                File file = iterator.next();
                addFileToArchiveStream(input.getParentFile(), os, file);
            }
        } else if (input.isFile()) {
            // input is a file, only add this
            addFileToArchiveStream(input.getParentFile(), os, input);
        }

        // cleaning up
        os.finish();
        os.close();
        out.close();
    }

    /**
     * Adds one file to an {@link ArchiveOutputStream}.
     * 
     * @param parent
     *            the file which's path will be removed from the entry name of
     *            the file to add
     * @param os
     *            the {@link ArchiveOutputStream} to write to
     * @param file
     *            the file to add
     * @throws IOException
     *             if an error occurs while opening the file
     */
    private static void addFileToArchiveStream(File parent, ArchiveOutputStream os, File file) throws IOException {
        // clean name of entry in archive from the input's parent path
        String entryName = file.getAbsolutePath().replace(parent.getAbsolutePath() + File.separator, "");

        // create entry and add it to archive stream
        ArchiveEntry entry = os.createArchiveEntry(file, entryName);
        os.putArchiveEntry(entry);
        IOUtils.copy(new FileInputStream(file), os);
        os.closeArchiveEntry();
    }

    /**
     * Protect default constructor.
     */
    protected Archiver() {

    }
}
