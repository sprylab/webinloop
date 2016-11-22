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

package com.sprylab.webinloop;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.sprylab.webinloop.reporter.Reporter;

/**
 * @author rzimmer
 * 
 */
public class WebInLoopGui extends JFrame implements ITestListener {

    private static final long serialVersionUID = 1L;

    private JComboBox browserComboBox = null;

    private JLabel browserLabel = null;

    private JButton closeButton = null;

    private JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JPanel jPanel3 = null;

    private JLabel logoLabel = null;

    private JButton openTestButton = null;

    private JButton runButton = null;

    private JLabel testFileLabel = null;

    private JTextField testFileTextField = null;

    private JProgressBar testProgressBar = null;

    private JLabel testProgressLabel = null;

    private WebInLoop webInLoop = null; // @jve:decl-index=0:

    private JLabel webInLoopLabel = null;

    private JLabel windowSizeLabel = null;

    private JLabel windowSizeSeperatorLabel = null;

    private JComboBox xWindowSizeComboBox = null;

    private JComboBox yWindowSizeComboBox = null;

    /**
     * This is the default constructor.
     */
    public WebInLoopGui() {
        super();
        initialize();
    }

    /**
     * @return the webInLoop
     */
    public WebInLoop getWebInLoop() {
        return this.webInLoop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onFinish(org.testng.ITestContext)
     */
    @Override
    public void onFinish(ITestContext arg0) {
        this.testProgressLabel.setText("Test execution finished!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext arg0) {
        // reset progress bar and label
        this.testProgressBar.setMaximum(this.webInLoop.getNrOfTests());
        this.testProgressBar.setValue(0);
        this.testProgressLabel.setText("Test execution started...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.testng.ITestListener#onTestFailedButWithinSuccessPercentage(org.testng
     * .ITestResult)
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
        nextTest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestFailure(org.testng.ITestResult)
     */
    @Override
    public void onTestFailure(ITestResult arg0) {
        nextTest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSkipped(org.testng.ITestResult)
     */
    @Override
    public void onTestSkipped(ITestResult arg0) {
        nextTest();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestStart(org.testng.ITestResult)
     */
    @Override
    public void onTestStart(ITestResult arg0) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.testng.ITestListener#onTestSuccess(org.testng.ITestResult)
     */
    @Override
    public void onTestSuccess(ITestResult arg0) {
        nextTest();
    }

    /**
     * @param webInLoop
     *            the webInLoop to set
     */
    public void setWebInLoop(WebInLoop webInLoop) {
        this.webInLoop = webInLoop;
        this.webInLoop.registerTestListener(this);
    }

    /**
     * This method initializes browserComboBox.
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getBrowserComboBox() {
        if (this.browserComboBox == null) {
            this.browserComboBox = new JComboBox();

            // add items
            String[] items =
                    {"*firefox", "*firefoxproxy", "*iexplore", "*iexploreproxy", "*opera", "*safari", "*safariproxy",
                        "*googlechrome", "*konqueror" };
            for (String item : items) {
                this.browserComboBox.addItem(item);
            }
        }
        return this.browserComboBox;
    }

    /**
     * This method initializes closeButton.
     * 
     * @return javax.swing.JButton
     */
    private JButton getCloseButton() {
        if (closeButton == null) {
            closeButton = new JButton();
            closeButton.setName("closeButton");
            closeButton.setText("Close");
            closeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return closeButton;
    }

    /**
     * This method initializes jContentPane.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (this.jContentPane == null) {
            this.jContentPane = new JPanel();
            this.jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
            jContentPane.add(getJPanel(), null);
            jContentPane.add(getJPanel1(), null);
            jContentPane.add(getJPanel2(), null);
            jContentPane.add(getJPanel3(), null);
        }
        return this.jContentPane;
    }

    /**
     * This method initializes jPanel.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (this.jPanel == null) {
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setRows(1);
            webInLoopLabel = new JLabel();
            webInLoopLabel.setText("WebInLoop - Teststarter");
            webInLoopLabel.setFont(new Font("Dialog", Font.BOLD, 24));
            webInLoopLabel.setName("webInLoopLabel");
            this.jPanel = new JPanel();
            jPanel.setLayout(gridLayout1);
            jPanel.add(webInLoopLabel, null);
        }
        return this.jPanel;
    }

    /**
     * This method initializes jPanel1.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (this.jPanel1 == null) {
            GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
            gridBagConstraints17.gridx = 2;
            gridBagConstraints17.gridy = 3;
            windowSizeSeperatorLabel = new JLabel();
            windowSizeSeperatorLabel.setText("x");
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = GridBagConstraints.BOTH;
            gridBagConstraints6.gridy = 3;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.gridx = 3;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = GridBagConstraints.BOTH;
            gridBagConstraints5.gridy = 3;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.gridx = 1;
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.gridx = 0;
            gridBagConstraints16.gridy = 3;
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.fill = GridBagConstraints.BOTH;
            gridBagConstraints15.gridy = 0;
            gridBagConstraints15.weightx = 1.0;
            gridBagConstraints15.gridwidth = 3;
            gridBagConstraints15.gridx = 1;
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.gridx = 0;
            gridBagConstraints14.gridy = 1;
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridx = 0;
            gridBagConstraints13.gridy = 2;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.gridy = 0;
            testFileLabel = new JLabel();
            testFileLabel.setText("Test file:");
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 4;
            gridBagConstraints7.gridy = 0;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.gridy = 1;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 1;
            this.windowSizeLabel = new JLabel();
            this.windowSizeLabel.setText("Window:");
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.gridwidth = 3;
            gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridx = 1;
            this.browserLabel = new JLabel();
            this.browserLabel.setText("Browser:");
            this.jPanel1 = new JPanel();
            this.jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Options",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            this.jPanel1.setLayout(new GridBagLayout());
            jPanel1.add(getBrowserComboBox(), gridBagConstraints2);
            jPanel1.add(getOpenTestButton(), gridBagConstraints7);
            this.jPanel1.add(this.browserLabel, new GridBagConstraints());
            this.jPanel1.add(this.windowSizeLabel, gridBagConstraints10);
            this.jPanel1.add(this.browserLabel, gridBagConstraints3);
            this.jPanel1.add(this.windowSizeLabel, gridBagConstraints4);
            jPanel1.add(testFileLabel, gridBagConstraints12);
            jPanel1.add(browserLabel, gridBagConstraints14);
            jPanel1.add(getTestFileTextField(), gridBagConstraints15);
            jPanel1.add(windowSizeLabel, gridBagConstraints16);
            jPanel1.add(getXWindowSizeComboBox(), gridBagConstraints5);
            jPanel1.add(getYWindowSizeComboBox(), gridBagConstraints6);
            jPanel1.add(windowSizeSeperatorLabel, gridBagConstraints17);
        }
        return this.jPanel1;
    }

    /**
     * This method initializes jPanel2.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (this.jPanel2 == null) {
            GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
            gridBagConstraints22.gridx = 1;
            gridBagConstraints22.weightx = 0.5;
            gridBagConstraints22.weighty = 0.5;
            gridBagConstraints22.gridy = 1;
            logoLabel = new JLabel();
            logoLabel.setText("");
            logoLabel.setIcon(new ImageIcon(getClass().getResource("/templates/css/img/logo.png")));
            GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
            gridBagConstraints18.gridx = 0;
            gridBagConstraints18.gridy = 1;
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.gridy = 2;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 2;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridy = 0;
            this.jPanel2 = new JPanel();
            this.jPanel2.setLayout(new GridBagLayout());
            this.jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Test execution",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                    new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
            this.testProgressLabel = new JLabel();
            this.testProgressLabel.setText("No tests run yet");
            jPanel2.add(getTestProgressBar(), gridBagConstraints);
            this.jPanel2.add(this.testProgressLabel, gridBagConstraints1);
            this.jPanel2.add(getRunButton(), gridBagConstraints11);
            jPanel2.add(testProgressLabel, gridBagConstraints18);
            jPanel2.add(logoLabel, gridBagConstraints22);
            this.jPanel2.add(this.testProgressLabel, gridBagConstraints21);
        }
        return this.jPanel2;
    }

    /**
     * This method initializes jPanel3.
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.RIGHT);
            jPanel3 = new JPanel();
            jPanel3.setLayout(flowLayout);
            jPanel3.add(getCloseButton(), null);
        }
        return jPanel3;
    }

    /**
     * This method initializes openTestButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getOpenTestButton() {
        if (this.openTestButton == null) {
            this.openTestButton = new JButton();
            this.openTestButton.setText("Open");
            this.openTestButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setMultiSelectionEnabled(true);

                    // add file chooser that only accepts directories and HTML
                    // files
                    fileChooser.addChoosableFileFilter(new FileFilter() {

                        @Override
                        public boolean accept(File f) {
                            if (f.isDirectory()) {
                                return true;
                            }

                            String extension = FilenameUtils.getExtension(f.getName());
                            return extension.equalsIgnoreCase("htm") || extension.equalsIgnoreCase("html");
                        }

                        @Override
                        public String getDescription() {
                            return "Selenium HTML testfiles";
                        }
                    });

                    // set path of file chooser to the path in the corresponding
                    // text field if possible
                    File tempFile = new File(WebInLoopGui.this.testFileTextField.getText());
                    if (!tempFile.exists()) {
                        // path does not exist, set the current working
                        // directory as path
                        tempFile = new File(System.getProperty("user.dir"));
                    }
                    fileChooser.setCurrentDirectory(tempFile);

                    if (fileChooser.showOpenDialog(WebInLoopGui.this) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        WebInLoopGui.this.testFileTextField.setText(file.getAbsolutePath());
                    }
                }
            });
        }
        return this.openTestButton;
    }

    /**
     * This method initializes runButton.
     * 
     * @return javax.swing.JButton
     */
    private JButton getRunButton() {
        if (this.runButton == null) {
            this.runButton = new JButton();
            this.runButton.setText("Launch Tests");
            this.runButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // set properties from GUI
                    String browser = WebInLoopGui.this.browserComboBox.getSelectedItem().toString();
                    String windowWidth = WebInLoopGui.this.xWindowSizeComboBox.getSelectedItem().toString();
                    String windowHeight = WebInLoopGui.this.yWindowSizeComboBox.getSelectedItem().toString();

                    WiLConfiguration config = WiLConfiguration.getInstance();
                    config.initConfiguration();

                    config.setProperty(WiLConfiguration.BROWSER_PROPERTY_KEY, browser);
                    config.setProperty(WiLConfiguration.WINDOW_WIDTH_PROPERTY_KEY, windowWidth);
                    config.setProperty(WiLConfiguration.WINDOW_HEIGHT_PROPERTY_KEY, windowHeight);

                    // TODO support more than one file
                    File inputFile = new File(WebInLoopGui.this.testFileTextField.getText());
                    webInLoop.setInputFiles(Arrays.asList(inputFile));

                    // create worker object to execute webInLoop.start() method
                    // in an own thread
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                        @Override
                        protected Void doInBackground() throws Exception {
                            try {
                                WebInLoopGui.this.webInLoop.start();
                            } catch (Exception ex) {
                                showErrorDialog(ex.getMessage());
                                ex.printStackTrace();
                            }

                            File indexHtml =
                                    new File(Reporter.getInstance().getReportsDir().getAbsoluteFile(),
                                            Reporter.INDEX_HTML_FILE);
                            if (!indexHtml.exists()) {
                                JOptionPane
                                        .showMessageDialog(
                                                WebInLoopGui.this,
                                                "The report file does not exist. This means an error has occured while executing the test cases.\n"
                                                        + "Please check the console output for more information.\n\n"
                                                        + "Possible errors are:\n"
                                                        + "* if you get java.net.MalformedURLException, you probably have not specified a correct and fully qualified base URL (like e.g. http://www.sprylab.com) in your test case or in selenium.xml\n"
                                                        + "* errors in your test case (represented by Java compiler errors)",
                                                "No test report produced", JOptionPane.WARNING_MESSAGE);
                            } else if (Desktop.isDesktopSupported()) {
                                // open index.htm in default browser
                                Desktop desktop = Desktop.getDesktop();
                                if (desktop.isSupported(Desktop.Action.OPEN)) {
                                    try {
                                        desktop.open(indexHtml);
                                    } catch (Exception ex) {
                                        showErrorDialog("Could not open test result in browser. Error:\n"
                                                + ex.getMessage());
                                    }
                                }
                            }

                            return null;
                        }

                    };

                    worker.execute();
                }
            });
        }
        return this.runButton;
    }

    /**
     * This method initializes testFileTextField.
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTestFileTextField() {
        if (testFileTextField == null) {
            testFileTextField = new JTextField();
        }
        return testFileTextField;
    }

    /**
     * This method initializes testProgressBar.
     * 
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getTestProgressBar() {
        if (this.testProgressBar == null) {
            this.testProgressBar = new JProgressBar();
            this.testProgressBar.setStringPainted(true);
        }
        return this.testProgressBar;
    }

    /**
     * This method initializes xWindowSizeComboBox.
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getXWindowSizeComboBox() {
        if (xWindowSizeComboBox == null) {
            String[] xWindowSizes = {"640", "800", "1024", "1280", "max" };

            xWindowSizeComboBox = new JComboBox(xWindowSizes);
            xWindowSizeComboBox.setEditable(true);
            xWindowSizeComboBox.setSelectedIndex(xWindowSizes.length - 1);
        }
        return xWindowSizeComboBox;
    }

    /**
     * This method initializes yWindowSizeComboBox.
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getYWindowSizeComboBox() {
        if (yWindowSizeComboBox == null) {
            String[] yWindowSizes = {"480", "600", "768", "1024", "max" };

            yWindowSizeComboBox = new JComboBox(yWindowSizes);
            yWindowSizeComboBox.setEditable(true);
            yWindowSizeComboBox.setSelectedIndex(yWindowSizes.length - 1);
        }
        return yWindowSizeComboBox;
    }

    /**
     * This method initializes this.
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(412, 365);
        this.setContentPane(getJContentPane());
        this.setTitle("WebInLoop GUI");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Gets called when processing the next test case. Currently adds one step
     * to the progress bar.
     */
    private void nextTest() {
        this.testProgressBar.setValue(this.testProgressBar.getValue() + 1);
    }

    /**
     * Displays an error Dialog with a specific error message.
     * 
     * @param message
     *            the message to display
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "An error occured", JOptionPane.ERROR_MESSAGE);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
