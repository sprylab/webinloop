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

package com.sprylab.webinloop.reporter;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.sprylab.webinloop.WiLConfiguration;

/**
 * Class representing a screenshot in a {@link ReporterEntry}.
 * 
 * @author rzimmer
 * 
 */
public class ReporterScreenshot {

    /**
     * The extension of the image format used to save the screenshot.
     */
    private static final String IMAGE_EXTENSION = "png";

    /**
     * The image file name.
     */
    private static final String IMAGE_FILE_NAME = "image";

    /**
     * Robot that is used for taking the screenshots.
     */
    private static Robot robot;

    /**
     * Screen dimension.
     */
    private static Rectangle screenBounds;

    /**
     * Map that saves the actual number of images in a directory. Key: directory
     * name, value: number of images
     */
    private static HashMap<String, Integer> directoyImageCount = new HashMap<String, Integer>();

    static {
        // look for left window offset value in properties and set it to 0 if
        // it's not available
        int leftWindowOffset =
                WiLConfiguration.getInstance().getInt(WiLConfiguration.LEFT_WINDOW_OFFSET_PROPERTY_KEY, 0);

        // look for uper window offset value in properties and set it to 0 if
        // it's not available
        int uperWindowOffset =
                WiLConfiguration.getInstance().getInt(WiLConfiguration.UPPER_WINDOW_OFFSET_PROPERTY_KEY, 0);

        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        // set screen bounds for capturing screenshots
        int windowWidth = screenDim.width;
        int windowHeight = screenDim.height;

        // look for window width in properties and set it if not "max"
        try {
            String windowWidthProperty =
                    WiLConfiguration.getInstance().getString(WiLConfiguration.WINDOW_WIDTH_PROPERTY_KEY);
            if (!windowWidthProperty.equals("max")) {
                windowWidth = Integer.valueOf(windowWidthProperty);
            }
        } catch (NumberFormatException e) {
            windowWidth = screenDim.width;
        }

        // look for window height in properties and set it if not "max"
        try {
            String windowHeightProperty =
                    WiLConfiguration.getInstance().getString(WiLConfiguration.WINDOW_HEIGHT_PROPERTY_KEY);
            if (!windowHeightProperty.equals("max")) {
                windowHeight = Integer.valueOf(windowHeightProperty);
            }
        } catch (NumberFormatException e) {
            windowHeight = screenDim.height;
        }

        screenBounds = new Rectangle(leftWindowOffset, uperWindowOffset, windowWidth, windowHeight);

        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The screenshot file.
     */
    private File screenshot = null;

    /**
     * Creates a new {@link ReporterScreenshot} object and takes the
     * corresponding screenshot.
     * 
     * @param directory
     *            the directory where the screenshot will be saved
     */
    public ReporterScreenshot(File directory) {
        this.screenshot = getNewScreenshotFile(directory);
        takeScreenshot();
    }

    /**
     * Creates a new {@link ReporterScreenshot} object. The screenshot itself
     * has already been taken and is specified by the <code>screenhotFile</code>
     * parameter.
     * 
     * @param directory
     *            the directory where the screenshot will be saved
     * @param screenshotFile
     *            the screenshot file
     */
    public ReporterScreenshot(File directory, File screenshotFile) {
        this.screenshot = getNewScreenshotFile(directory);

        try {
            FileUtils.copyFile(screenshotFile, this.screenshot);
        } catch (Exception e) {
            // try to do it the conventional way
            takeScreenshot();
        }
    }

    /**
     * @return the screenshot
     */
    public File getScreenshot() {
        return this.screenshot;
    }

    /**
     * @return the file name of the screenshot
     */
    public String getScreenshotFileName() {
        return this.screenshot.getName();
    }

    /**
     * Determines the new file for the screenshot to take.
     * 
     * @param directory
     *            parent directory of the screenshot
     * @return the new screenshot file
     */
    private File getNewScreenshotFile(File directory) {
        Integer currentImageCount = directoyImageCount.get(directory.getName());
        if (currentImageCount == null) {
            currentImageCount = 0;
        }

        File newScreenshotFile = new File(directory, IMAGE_FILE_NAME + currentImageCount++ + "." + IMAGE_EXTENSION);
        directoyImageCount.put(directory.getName(), currentImageCount);

        return newScreenshotFile;
    }

    /**
     * Takes a screenshot of the desktop in the pre-calculated bounds.
     */
    private void takeScreenshot() {
        final BufferedImage image = ReporterScreenshot.robot.createScreenCapture(ReporterScreenshot.screenBounds);
        try {
            ImageIO.write(image, IMAGE_EXTENSION, this.screenshot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
