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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.tools.ToolProvider;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ITestListener;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.sprylab.webinloop.models.Test;
import com.sprylab.webinloop.models.TestSuite;
import com.sprylab.webinloop.reporter.Reporter;
import com.sprylab.webinloop.util.mailer.Mailer;
import com.sprylab.webinloop.util.ramcompile.CompileException;
import com.sprylab.webinloop.util.ramcompile.OnTheFlyInRAMIncrementally;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This class manages automated testing of web applications using Selenium (<a
 * href="http://seleniumhq.org/">http://seleniumhq.org/</a>) and TestNG (<a
 * href="http://testng.org/">http://testng.org/</a>).
 * 
 * @author rzimmer
 * 
 */
public class WebInLoop {

    /**
     * Logger instance.
     */
    private static Log log = LogFactory.getLog(WebInLoop.class);

    /**
     * Command line argument for the configuration directory.
     */
    private static final String CMD_LINE_ARG_CONFIG_DIR = "config";

    /**
     * Command line argument for the debug mode.
     */
    private static final String CMD_LINE_ARG_DEBUG = "d";

    /**
     * Command line argument for the help.
     */
    private static final String CMD_LINE_ARG_HELP = "help";

    /**
     * Command line argument for the test files.
     */
    private static final String CMD_LINE_ARG_INPUT_FILES = "testfiles";

    /**
     * Command line argument for the output directory for the source files.
     */
    private static final String CMD_LINE_ARG_OUTPUT_DIR = "output";

    /**
     * Command line argument for the translator mode.
     */
    private static final String CMD_LINE_ARG_TRANSLATOR_MODE = "t";

    /**
     * Name of the templates directory in JAR.
     */
    private static final String TEMPLATE_DIRECTORY = "templates";

    /**
     * Name of template file for the TestNG classes.
     */
    private static final String TESTNG_TEMPLATE_FILE = "testng.ftl";

    /**
     * Terminates the application with an error message.
     * 
     * @param message
     *            the error message
     */
    public static void abort(String message) {
        log.error(message);
        log.error("Aborting...");
        System.exit(1);
    }

    /**
     * Main function.
     * 
     * @param args
     *            command line arguments
     */
    public static void main(String[] args) {
        Options options = createOptions();

        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        final WebInLoop webInLoop = new WebInLoop();
        try {
            line = parser.parse(options, args);

            if (line.hasOption(CMD_LINE_ARG_HELP)) {
                printHelp(options, true);
            }

            boolean guiMode = !line.hasOption(CMD_LINE_ARG_INPUT_FILES);

            checkJavaRequirements(guiMode);

            webInLoop.setTranslatorMode(line.hasOption(CMD_LINE_ARG_TRANSLATOR_MODE));

            webInLoop.setDebugMode(line.hasOption(CMD_LINE_ARG_DEBUG));

            if (line.hasOption(CMD_LINE_ARG_OUTPUT_DIR)) {
                webInLoop.setOutputDir(new File(line.getOptionValue(CMD_LINE_ARG_OUTPUT_DIR)));
            }

            if (line.hasOption(CMD_LINE_ARG_CONFIG_DIR)) {
                webInLoop.setConfigDir(new File(line.getOptionValue(CMD_LINE_ARG_CONFIG_DIR)));
            }

            if (!guiMode) {
                // act as CLI version
                List<String> fileNamesList = Arrays.asList(line.getOptionValues(CMD_LINE_ARG_INPUT_FILES));
                List<File> filesList = new ArrayList<File>();

                for (String fileString : fileNamesList) {
                    File file = new File(fileString);
                    if (file.exists()) {
                        filesList.add(file);
                    }
                }
                webInLoop.setInputFiles(filesList);
                webInLoop.start();
            } else {
                // start GUI
                final Runnable gui = new Runnable() {

                    @Override
                    public void run() {
                        // start GUI to set input files
                        WebInLoopGui gui = new WebInLoopGui();
                        gui.setWebInLoop(webInLoop);
                        gui.pack();
                        gui.setVisible(true);
                    }
                };

                // set platform specific look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                SwingUtilities.invokeAndWait(gui);
            }
        } catch (ParseException e) {
            // display exception message and print help
            log.error(e.getMessage());
            printHelp(options, true);
        } catch (Exception e) {
            // display exception message
            log.error(e.getMessage(), e);

            // exit with error code
            System.exit(1);
        }
    }

    /**
     * Checks if the minimum Java requirements are fulfilled. If not, it will
     * exit the program.
     * 
     * @param guiMode
     *            if this flag is set it will display an error dialog instead of
     *            printing the error to the error stream.
     */
    private static void checkJavaRequirements(boolean guiMode) {
        String javaHome = System.getenv("JAVA_HOME");
        Float javaVersion = Float.valueOf(System.getProperty("java.specification.version"));

        String javaError = "";

        final double minJavaVersion = 1.6;
        if (javaVersion < minJavaVersion) {
            // check java version
            javaError +=
                    "The minimum Java version to run this program is 1.6. Please install a JDK >= 1.6 from http://java.sun.com.\n";
        } else if (ToolProvider.getSystemJavaCompiler() == null) {
            // no java compiler found
            javaError +=
                    "You do not seem to run this program from a JDK, because no java-compiler was found. Please install a JDK >= 1.6 from http://java.sun.com, if not already done.\n";
        }

        if (javaError != "") {
            // there were errors

            if (javaHome != null) {
                // JAVA_HOME set
                javaError +=
                        "You have specified a JAVA_HOME enviroment variable.\n\nPlease make sure that it is pointing to a JDK >= 1.6 and that you are launching the WebInLoop run-script:\n   run.cmd on Windows or\n   run.sh on *nix";
            }

            if (guiMode) {
                JOptionPane.showMessageDialog(null, javaError, "Java problem", JOptionPane.ERROR_MESSAGE);
            } else {
                log.error(javaError);
            }

            // quit program
            System.exit(1);
        }

    }

    /**
     * Creates all command line options and returns them.
     * 
     * @return the command line options.
     */
    private static Options createOptions() {
        Options options = new Options();
        Option help = new Option(CMD_LINE_ARG_HELP, "print this message");
        Option translatorMode =
                new Option(CMD_LINE_ARG_TRANSLATOR_MODE, false,
                        "excecute in translator mode, i.e. only write Java test classes and testng.xml");
        Option debugMode =
                new Option(CMD_LINE_ARG_DEBUG, false,
                        "excecute in translator mode, i.e. only write Java test classes and testng.xml");

        Option inputFile =
                OptionBuilder
                        .withArgName("file(s)")
                        .hasArgs()
                        .withDescription(
                                "execute given Selenium test suites or test cases (omit if you want to start in GUI mode)")
                        .create(CMD_LINE_ARG_INPUT_FILES);
        Option outputDir =
                OptionBuilder.withArgName("directory").hasArg()
                        .withDescription("output directory where the Java test classes and testng.xml are written to")
                        .create(CMD_LINE_ARG_OUTPUT_DIR);

        Option configDir =
                OptionBuilder
                        .withArgName("directory")
                        .hasArg()
                        .withDescription(
                                "directory, where to look for configuration files (default is current working directory)")
                        .create(CMD_LINE_ARG_CONFIG_DIR);

        options.addOption(help);
        options.addOption(translatorMode);
        options.addOption(debugMode);
        options.addOption(inputFile);
        options.addOption(outputDir);
        options.addOption(configDir);
        return options;
    }

    /**
     * Prints the help screen with all command line options.
     * 
     * @param options
     *            the command line options
     * @param exit
     *            flag indication if the application should exit.
     */
    private static void printHelp(Options options, boolean exit) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(WebInLoop.class.getSimpleName(), options, true);

        if (exit) {
            System.exit(1);
        }
    }

    /**
     * List storing all TestNG classes to test.
     */
    private List<Class<?>> classes = new ArrayList<Class<?>>();

    /**
     * The FreeMarker configuration.
     */
    private freemarker.template.Configuration configuration = new freemarker.template.Configuration();

    /**
     * The currently used test model.
     */
    private Test currentTestModel = null;

    /**
     * Flag indication if in debug mode or not.
     */
    private boolean debugMode = false;

    /**
     * List storing all input test files.
     */
    private List<File> inputFiles = new ArrayList<File>();

    /**
     * Number of test classes that failed to compile.
     */
    private int nrOfTestCompileErrors = 0;

    /**
     * The output directory for the sources.
     */
    private File outputDir = null;

    /**
     * The FreeMarker template root map that is applied to the template.
     */
    private Map<String, Object> root = new HashMap<String, Object>();

    /**
     * The test execution start date and time.
     */
    private Date startDate = null;

    /**
     * The FreeMarker template.
     */
    private Template template = null;

    /**
     * Concatenation of all compile errors.
     */
    private String testCompileErrorMessages = "";

    /**
     * The TestNG instance needed for test execution.
     */
    private TestNG testNg = new TestNG();

    /**
     * The test suite to test.
     */
    private TestSuite testSuite = null;

    /**
     * Flag indicating if in translator mode or not.
     */
    private boolean translatorMode = false;

    /**
     * Default constructor initializing the FreeMarker engine and the
     * configuration directory.
     */
    public WebInLoop() {
        // initialize configuration
        try {
            initConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // initialize template
        try {
            initTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // initialize properties
        setOutputDir(new File(WiLConfiguration.getInstance().getString(
                WiLConfiguration.TRANSLATOR_OUTPUT_DIR_PROPERTY_KEY)));
    }

    /**
     * @return the inputFiles
     */
    public List<File> getInputFiles() {
        return this.inputFiles;
    }

    /**
     * Returns the number of all test classes that will be executed.
     * 
     * @return the number of test classes
     */
    public int getNrOfTests() {
        return this.classes.size();
    };

    /**
     * @return the outputDir
     */
    public File getOutputDir() {
        return this.outputDir;
    }

    /**
     * @return the debugMode
     */
    public boolean isDebugMode() {
        return this.debugMode;
    }

    /**
     * @return the translatorMode
     */
    public boolean isTranslatorMode() {
        return this.translatorMode;
    }

    /**
     * Adds a test listener to the underlying TestNG object.
     * 
     * @param listener
     *            the test listener to add
     */
    public void registerTestListener(ITestListener listener) {
        this.testNg.addListener(listener);
    }

    /**
     * @param configDir
     *            the configDir to set
     */
    public void setConfigDir(File configDir) {
        WiLConfiguration.getInstance().setConfigDir(configDir);
    }

    /**
     * @param debugMode
     *            the debugMode to set
     */
    public void setDebugMode(boolean debugMode) {
        WiLConfiguration.getInstance().setDebugMode(debugMode);
    }

    /**
     * @param list
     *            the inputFiles to set
     */
    public void setInputFiles(List<File> list) {
        this.inputFiles = list;
    }

    /**
     * @param outputDir
     *            the outputDir to set
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @param translatorMode
     *            the translatorMode to set
     */
    public void setTranslatorMode(boolean translatorMode) {
        this.translatorMode = translatorMode;
    }

    /**
     * Starts the test execution. Note: The configuration has to be initialized
     * prior to calling this method.
     * 
     * @throws TransformerException
     *             if the template transformation fails
     * @throws IOException
     *             if there was a error accessing the test files in the file
     *             system
     */
    public void start() throws TransformerException, IOException {
        createDataModel();

        prepareTestSuite();

        if (nrOfTestCompileErrors > 0) {
            final int maxWidth = 500;
            final String testCompileErrorMessage =
                    "There were errors in processing all test files. This means that you have made uncorrect use of some commands.\n"
                            + "Here is an abstract:\n\n" + StringUtils.abbreviate(testCompileErrorMessages, maxWidth)
                            + "\n" + "Please check console output for more information.";
            if (nrOfTestCompileErrors == this.inputFiles.size()) {
                throw new RuntimeException(testCompileErrorMessage);
            } else {
                final String continueMessage = "NOTE: Will continue, but skip all faulty test cases.";
                log.error(testCompileErrorMessage);
                log.error(continueMessage);
            }
        }

        if (!isTranslatorMode()) {
            launchTests();
        }

        // create testng.xml file
        createTestNgXml();

        sendMailReport();
    }

    /**
     * Cleans up any existing test cases from previous test runs.
     */
    private void clearTestSuite() {
        this.classes.clear();
        this.root.clear();
        this.testNg.setXmlSuites(new ArrayList<XmlSuite>());
        Reporter.getInstance().getReporterTests().clear();
    }

    /**
     * Creates the test suite data model parsing the Selenium HTML input files.
     * 
     * @throws TransformerException
     *             if the template transformation fails
     * @throws FileNotFoundException
     *             if the specified test file does not exist
     */
    private void createDataModel() throws FileNotFoundException, TransformerException {
        if (this.inputFiles == null || this.inputFiles.size() == 0 || !this.inputFiles.get(0).exists()) {
            throw new FileNotFoundException("No valid test file specified.");
        }

        // get first file
        File inputFile = this.inputFiles.get(0);
        this.testSuite = TestSuite.createFromFile(inputFile);

        // add tests from the other files specified by the user
        for (int i = 1; i < this.inputFiles.size(); i++) {
            inputFile = this.inputFiles.get(i);
            TestSuite tempTestSuite = TestSuite.createFromFile(inputFile);
            this.testSuite.addTestSuite(tempTestSuite);
        }
    }

    /**
     * Creates a <code>testng.xml</code> file from the test suite model and
     * saves it in the output directory.
     */
    private void createTestNgXml() {
        if (this.outputDir != null) {
            XmlSuite xmlSuite = new XmlSuite();
            xmlSuite.setName(this.testSuite.getName());

            for (Test testModel : this.testSuite.getTests()) {
                XmlTest xmlTest = new XmlTest(xmlSuite);
                xmlTest.setName(testModel.getName());

                List<XmlClass> xmlClasses = new ArrayList<XmlClass>();
                xmlClasses.add(new XmlClass(testModel.getName()));
                xmlTest.setXmlClasses(xmlClasses);
            }

            try {
                FileWriter fileWriter = new FileWriter(new File(this.outputDir, "testng.xml"));
                fileWriter.write(xmlSuite.toXml());
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Cannot write testng.xml file to " + this.outputDir + ".");
            }
        }
    }

    /**
     * Initializes the FreeMarker configuration.
     */
    private void initConfiguration() {
        this.configuration.setClassForTemplateLoading(getClass(), "/");
        this.configuration.setObjectWrapper(new DefaultObjectWrapper());
    }

    /**
     * Initializes FreeMarker template.
     * 
     * @throws IOException
     *             if the template file could not be opened.
     */
    private void initTemplate() throws IOException {
        this.template = this.configuration.getTemplate(TEMPLATE_DIRECTORY + "/" + TESTNG_TEMPLATE_FILE);
    }

    /**
     * Creates a TestNG instance and runs the test suite containing all
     * processed test classes.
     * 
     * @throws IOException
     *             if the reporter directory could not be opened
     */
    private void launchTests() throws IOException {
        this.startDate = new Date();

        File reportingDir = null;
        if (WiLConfiguration.getInstance().getBoolean(WiLConfiguration.CREATE_TIMESTAMP_DIR_PROPERTY_KEY)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            String reportDate = dateFormat.format(this.startDate);
            reportingDir =
                    new File(WiLConfiguration.getInstance().getString(WiLConfiguration.LOG_DIR_PROPERTY_KEY)
                            + File.separator + reportDate);
        } else {
            reportingDir = new File(WiLConfiguration.getInstance().getString(WiLConfiguration.LOG_DIR_PROPERTY_KEY));
        }
        Reporter.getInstance().prepare(
                new File(reportingDir, WiLConfiguration.getInstance().getString(
                        WiLConfiguration.SCREENSHOT_DIR_PROPERTY_KEY)));

        final String defaultSuiteName =
                WiLConfiguration.getInstance().getString(WiLConfiguration.TESTSUITE_NAME_PROPERTY_KEY);
        this.testNg.setDefaultSuiteName(defaultSuiteName);
        this.testNg.setDefaultTestName(defaultSuiteName);

        this.testNg.setOutputDirectory(new File(reportingDir, WiLConfiguration.getInstance().getString(
                WiLConfiguration.TESTNG_DIR_PROPERTY_KEY)).getPath());
        this.testNg.setTestClasses(this.classes.toArray(new Class[0]));

        this.testNg.run();
    }

    /**
     * Prepares the test suite by processing all test models. This method calls
     * {@link #clearTestSuite()} first.
     */
    private void prepareTestSuite() {
        clearTestSuite();

        this.root.put("properties", WiLConfiguration.getInstance().toProperties());

        List<Test> tests = this.testSuite.getTests();
        for (Test testModel : tests) {
            this.currentTestModel = testModel;
            this.root.put("model", this.currentTestModel);
            try {
                processCurrentModel();
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Processes the current test model using FreeMarker and creates TestNG
     * classes from it. When running in stand-alone mode, each test class will
     * be compiled and registered in RAM. Else the test class will be written to
     * the file system.
     * 
     * @throws TemplateException
     *             if the template file could not be processed
     * @throws IOException
     *             if the template file could not be opened
     */
    private void processCurrentModel() throws TemplateException, IOException {
        if (!isTranslatorMode()) {
            // running in stand-alone mode:
            // compile classes on-the-fly and register them to be tested
            StringWriter strWriter = new StringWriter();
            processCurrentModel(strWriter);

            registerClassFromString(strWriter.toString());
        }

        if (this.outputDir != null) {
            if (!this.outputDir.exists()) {
                this.outputDir.mkdirs();
            }
            File outputFile = new File(this.outputDir, this.currentTestModel.getName() + ".java");
            processCurrentModel(new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8")));
        }
    }

    /**
     * Processes the current test model the supplied FreeMarker template file
     * and writes to the passed {@link Writer} object.
     * 
     * @param out
     *            the writer to write to
     * @throws TemplateException
     *             if the template file could not be processed
     * @throws IOException
     *             if the template file could not be opened
     */
    private void processCurrentModel(Writer out) throws TemplateException, IOException {
        this.template.process(this.root, out);
        out.flush();
    }

    /**
     * Compiles and registers a Java Class from a string.
     * 
     * @param source
     *            string containing the whole Java class source code
     */
    private void registerClassFromString(String source) {
        Class<?> c = null;
        try {
            c =
                    OnTheFlyInRAMIncrementally.compileClassFromString(source, this.currentTestModel.getName(),
                            WiLConfiguration.getInstance().getString(WiLConfiguration.PACKAGE_PROTERTY_KEY));
            this.classes.add(c);
        } catch (CompileException e) {
            testCompileErrorMessages += e.getMessage() + "\n\n";
            nrOfTestCompileErrors++;
        }
    }

    /**
     * Sends an mail report containing the test result as configured in the
     * property files.
     * 
     * @throws IOException
     *             if the TestNG report could not be opened
     */
    private void sendMailReport() throws IOException {
        if (WiLConfiguration.getInstance().getBoolean(WiLConfiguration.NOTIFY_BY_MAIL_PROPERTY_KEY, false)) {
            // mail notification desired
            String subject =
                    "WebInLoop test results from test run at " + new SimpleDateFormat().format(this.startDate) + ": ";
            boolean failed = Reporter.getInstance().hasFailed();

            if (failed) {
                subject += "FAIL";
            } else {
                subject += "SUCCESS";
            }

            String body = "";
            boolean bodyIsHtml = false;

            File emailReport = new File(this.testNg.getOutputDirectory(), "emailable-report.html");
            if (emailReport.exists()) {
                body = FileUtils.readFileToString(emailReport);
                bodyIsHtml = true;
            }

            try {
                Mailer.sendReportMessage(subject, body, bodyIsHtml);
            } catch (MessagingException e) {
                throw new RuntimeException("Sending e-mail report failed.", e);
            }
        }
    }
}
