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

package com.sprylab.webinloop.util.mailer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sprylab.webinloop.WiLConfiguration;
import com.sprylab.webinloop.reporter.Reporter;
import com.sprylab.webinloop.util.zipper.Archiver;

/**
 * Utility class for Mailing. This class is a singleton.
 * 
 * @author rzimmer
 * 
 */
public class Mailer {

    public enum MailboxOperation {
        ANSWERED, PURGE, READ, RECENT, UNANSWERED, UNREAD, UNRECENT
    }

    /**
     * Mailer instance.
     */
    private static Map<String, Mailer> instances = new HashMap<String, Mailer>();

    /**
     * Logger instance.
     */
    private static Log log = LogFactory.getLog(Mailer.class);

    /**
     * Closes all connections mailer objects may have.
     * 
     * @throws MessagingException
     */
    public static void closeAllConnections() {
        Collection<Mailer> allMailers = instances.values();
        for (Mailer mailer : allMailers) {
            try {
                mailer.disconnect();
            } catch (MessagingException e) {
                log.error("Error while closing mail account " + mailer.mailAccount, e);
            }
        }
    }

    /**
     * Executes a operation on a mailbox. Pass * for all configured mail
     * accounts or a semicolon separated list of accounts that may include
     * ranges.
     * 
     * @param mailAccounts
     *            semicolon separated list of mail accounts to purge or * for
     *            all
     * @throws MessagingException
     *             thrown when there were errors accessing a mail account
     */
    public static void executeMailboxOperation(MailboxOperation operation, String mailAccounts)
        throws MessagingException {
        List<String> mailboxesToConsider = new ArrayList<String>();

        String mailConnectionsValue = "";

        if (mailAccounts.equals("*")) {
            // requested to purge all mailbox - get value from configuration
            mailConnectionsValue = WiLConfiguration.getInstance().getString(WiLConfiguration.MAIL_CONNECTIONS);
        } else {
            // only purge the supplied mailbox(es)
            mailConnectionsValue = mailAccounts;
        }

        mailboxesToConsider.addAll(Arrays.asList(mailConnectionsValue.split(";")));

        // choose correct flags
        Map<Flag, Boolean> flagBitmap = new HashMap<Flag, Boolean>();
        switch (operation) {
            case PURGE:
                flagBitmap.put(Flag.DELETED, true);
                break;
            case READ:
            default:
                flagBitmap.put(Flag.SEEN, true);
                flagBitmap.put(Flag.RECENT, false);
                break;
            case UNREAD:
                flagBitmap.put(Flag.SEEN, false);
                break;
            case RECENT:
                flagBitmap.put(Flag.RECENT, true);
                break;
            case UNRECENT:
                flagBitmap.put(Flag.RECENT, false);
                break;
            case ANSWERED:
                flagBitmap.put(Flag.ANSWERED, true);
                break;
            case UNANSWERED:
                flagBitmap.put(Flag.ANSWERED, false);
                break;
        }

        // set flag for messages
        Set<Entry<Flag, Boolean>> entrySet = flagBitmap.entrySet();
        for (String mailAccount : mailboxesToConsider) {
            MailAccountParseResult mailAccParseResult = MailAccountParseResult.parse(mailAccount);

            Mailer mailer = Mailer.getInstance(mailAccParseResult.getMailAccount());

            Integer lowerBound = mailAccParseResult.getLowerBound();
            Integer upperBound = mailAccParseResult.getUpperBound();

            for (Entry<Flag, Boolean> entry : entrySet) {
                mailer.setFlagForMessages(entry.getKey(), entry.getValue(), lowerBound, upperBound);
            }

            if (operation == MailboxOperation.PURGE) {
                // expunge on close
                mailer.currentFolder.close(true);
            }
        }

    }

    /**
     * Returns the default Mailer instance.
     * 
     * @return instance object of Mailer
     * @throws MessagingException
     */
    public static Mailer getInstance(String mailAccount) throws MessagingException {
        Mailer mailer = instances.get(mailAccount);
        if (mailer == null) {
            mailer = new Mailer(mailAccount);

            instances.put(mailAccount, mailer);
        }
        return mailer;
    }

    /**
     * Gets the content of one or more mails in the configured mail folders.
     * 
     * @param mailTargetID
     *            the selector which part to get from mail folder. The syntax is
     *            <code>NameOfMailAccount:{from|to|cc|bcc|subject|body}</code>
     * @param onlyNew
     *            flag indicating that only new (unseen) mails should be
     *            analyzed
     * @param onlyRecent
     *            flag indicating that only the most recent mail should be
     *            analyzed
     * @param removeUnseen
     *            flag indication that the mail's unseen flag should be removed
     * @return the mail's content specified by target
     * @throws MessagingException
     *             if the messages cannot be retrieved
     * @throws IOException
     *             if mail configuration cannot be read from file system
     */
    public static String[] getMail(String mailTargetID, boolean onlyNew, boolean onlyRecent, boolean removeUnseen)
        throws MessagingException, IOException {
        List<String> result = new ArrayList<String>();

        MailAccountParseResult mailAccParseResult = MailAccountParseResult.parse(mailTargetID);

        Mailer mailer = Mailer.getInstance(mailAccParseResult.getMailAccount());

        Integer lowerBound = mailAccParseResult.getLowerBound();
        Integer upperBound = mailAccParseResult.getUpperBound();

        List<Message> messages = null;
        if (onlyNew) {
            messages = Arrays.asList(mailer.getNewMessages(lowerBound, upperBound, removeUnseen));
        } else {
            messages = Arrays.asList(mailer.getAllMessages(lowerBound, upperBound));
        }

        if (onlyRecent && (messages.size() > 1)) {
            // remove all but the last message from the list
            messages = Arrays.asList(messages.get(messages.size() - 1));
        }

        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
            Message message = iterator.next();

            String mailTarget = mailAccParseResult.getTarget();
            if (mailTarget == null) {
                mailTarget = "body";
            }

            if (mailTarget.equals("from")) {
                result.add(InternetAddress.toString(message.getFrom()));
            } else if (mailTarget.equals("to")) {
                result.add(InternetAddress.toString(message.getRecipients(Message.RecipientType.TO)));
            } else if (mailTarget.equals("cc")) {
                result.add(InternetAddress.toString(message.getRecipients(Message.RecipientType.CC)));
            } else if (mailTarget.equals("bcc")) {
                result.add(InternetAddress.toString(message.getRecipients(Message.RecipientType.BCC)));
            } else if (mailTarget.equals("subject")) {
                result.add(message.getSubject());
            } else if (mailTarget.equals("body")) {
                if (message.isMimeType("multipart/*")) {
                    // MultiPart message
                    MimeMultipart multiPart = (MimeMultipart) message.getContent();

                    // analyze MultiParts
                    for (int i = 0; i < multiPart.getCount(); i++) {
                        BodyPart bodyPart = multiPart.getBodyPart(i);
                        if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/html")) {
                            // Part is of type "text/*", so add it to result
                            result.add(bodyPart.getContent().toString());
                        }
                    }
                } else {
                    // no MultiPart message - assume that it is plain text
                    result.add(message.getContent().toString());
                }
            }
        }

        if (!WiLConfiguration.getInstance().getBoolean(WiLConfiguration.CACHE_MAIL_CONNECTION_PROPERTY_KEY)) {
            mailer.disconnect();
        }

        return result.toArray(new String[0]);
    }

    /**
     * Converts a message to a string.
     * 
     * @param message
     *            the message to convert
     * @return the string representation of the message
     * @throws MessagingException
     *             on all mailing errors
     * @throws IOException
     *             if a {@link DataHandler} fails
     */
    public static String messageToString(Message message) throws MessagingException, IOException {
        String result = "";
        result += "from: " + InternetAddress.toString(message.getFrom()) + "\n";
        result += "subject: " + message.getSubject() + "\n";
        result += "content: " + message.getContent();
        return result;
    }

    /**
     * Sends a message.
     * 
     * @param from
     *            the sender's address
     * @param to
     *            ; separated list of addresses of the recipients
     * @param subject
     *            the mail subject
     * @param body
     *            the mail body
     * @param bodyIsHtml
     *            true if the mail body is HTML code
     * @param attachment
     *            the file to attach to mail
     * @param mailAcc
     *            the mail account to use for sending
     * @throws MessagingException
     *             on all mailing errors
     * @throws IOException
     *             if a {@link DataHandler} fails
     */
    public static void sendMessage(String from, String to, String subject, String body, boolean bodyIsHtml,
            File attachment, String mailAcc) throws MessagingException, IOException {

        URLName urlName =
                new URLName(WiLConfiguration.getInstance().getString(mailAcc + "." + WiLConfiguration.URL_PROPERTY_KEY));

        WiLConfiguration.getInstance().setProperty("mail.smtp.host", urlName.getHost());

        Session session = Session.getInstance(WiLConfiguration.getInstance().toProperties());
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));

        msg.setRecipients(RecipientType.TO, InternetAddress.parse(to, false));

        msg.setSubject(subject);

        if ((attachment != null) && attachment.exists()) {
            // attachment supplied, create multipart message

            // first body part (plain text)
            MimeBodyPart mbp1 = new MimeBodyPart();

            if (!bodyIsHtml) {
                mbp1.setText(body);
            } else {
                mbp1.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "text/html")));
            }

            // second body part (if attachment file supplied)
            MimeBodyPart mbp2 = null;

            mbp2 = new MimeBodyPart();
            mbp2.attachFile(attachment);

            // create multi part message
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            msg.setContent(mp);
        } else {
            if (!bodyIsHtml) {
                msg.setText(body);
            } else {
                msg.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "text/html")));
            }
        }

        Transport tr = session.getTransport("smtp");

        boolean smtpAuth = urlName.getPassword() != null;

        if (smtpAuth) {

            tr.connect(urlName.getHost(), urlName.getPort(), urlName.getUsername(), urlName.getPassword());
        } else {
            tr.connect();
        }
        msg.saveChanges();
        tr.sendMessage(msg, msg.getAllRecipients());
        tr.close();

    }

    /**
     * Sends a report by mail. The sender, the recipients and the mail account
     * to use get retrieved from the properties map. In the properties file, the
     * user can specify if he wants a HTML summary (by setting
     * <i>reportMailAttachment</i> to <i>html</i>) or a archived version of the
     * full report (by setting <i>reportMailAttachment</i> to <i>zip</i>,
     * <i>tar</i> or <i>jar</i>).
     * 
     * @param subject
     *            the mail subject
     * @param body
     *            the mail body
     * @param bodyIsHtml
     *            true if the mail body is HTML code
     * @throws MessagingException
     *             on all mailing errors
     * @throws IOException
     *             if a {@link DataHandler} fails
     */
    public static void sendReportMessage(String subject, String body, boolean bodyIsHtml) throws MessagingException,
        IOException {
        String from = WiLConfiguration.getInstance().getString(WiLConfiguration.REPORT_MAIL_FROM_PROPERTY_KEY);
        String to = WiLConfiguration.getInstance().getString(WiLConfiguration.REPORT_MAIL_TO_PROPERTY_KEY);
        String mailAcc = WiLConfiguration.getInstance().getString(WiLConfiguration.REPORT_MAIL_ACCOUNT_PROPERTY_KEY);

        // prepare attachment
        File reportsDir = Reporter.getInstance().getReportsDir();
        File attachment = null;
        if (WiLConfiguration.getInstance().getString(WiLConfiguration.REPORT_MAIL_ATTACHMENT_PROPERTY_KEY, "")
                .equalsIgnoreCase("html")) {
            // only summary HTML file is desired
            attachment = new File(reportsDir, Reporter.INDEX_HTML_FILE);
        } else {
            // supposing some archive format to archive the reports directory
            attachment =
                    new File(reportsDir.getParent(), reportsDir.getName()
                            + "."
                            + WiLConfiguration.getInstance().getString(
                                    WiLConfiguration.REPORT_MAIL_ATTACHMENT_PROPERTY_KEY, Archiver.ZIP));

            try {
                // archive reports directory
                Archiver.zip(reportsDir, attachment);
            } catch (ArchiveException e) {
                log.error("ZIP file to mail not created successfully.", e);
            }
        }

        // send mail
        sendMessage(from, to, subject, body, bodyIsHtml, attachment, mailAcc);
    }

    /**
     * Reference to the current folder that is used.
     */
    private Folder currentFolder = null;

    /**
     * Reference to the current store that is used.
     */
    private Store currentStore = null;

    /**
     * Mail account identifier of this mailer object
     */
    private String mailAccount = "";

    /**
     * Creates a new Mailer object and initializes the properties.
     * 
     * @throws MessagingException
     */
    protected Mailer(String mailAccount) throws MessagingException {
        this.mailAccount = mailAccount;

        connect();
    }

    /**
     * Connects to a mail account specified by the name used in the properties
     * file.
     * 
     * @throws MessagingException
     *             on all mailing errors
     */
    private void connect() throws MessagingException {
        if (this.currentStore == null) {
            Session session = Session.getInstance(WiLConfiguration.getInstance().toProperties(), null);

            URLName urln =
                    new URLName(WiLConfiguration.getInstance().getString(
                            mailAccount + "." + WiLConfiguration.URL_PROPERTY_KEY));
            this.currentStore = session.getStore(urln);
        }

        if (!this.currentStore.isConnected()) {
            this.currentStore.connect();
        }
    }

    /**
     * Closes all current connection from a mail account.
     * 
     * @throws MessagingException
     */
    private void disconnect() throws MessagingException {
        if (this.currentFolder != null && this.currentFolder.isOpen()) {
            this.currentFolder.close(true);
        }

        if (this.currentStore != null && this.currentStore.isConnected()) {
            this.currentStore.close();
        }
    }

    /**
     * Returns all messages from the current folder.
     * 
     * @return array of messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public Message[] getAllMessages() throws MessagingException {
        openFolder();

        return this.currentFolder.getMessages();
    }

    /**
     * Returns all messages from the current folder in the given range.
     * 
     * @param lowerBound
     *            lower bound (inclusive)
     * @param upperBound
     *            upper bound (inclusive)
     * @param removeUnseen
     *            true, if the UNSEEN flag should be removed from the message
     * @return array of Message[]
     * @throws MessagingException
     */
    public Message[] getAllMessages(Integer lowerBound, Integer upperBound) throws MessagingException {
        Message[] allMessages = this.getAllMessages();

        return limitMessagesToRange(allMessages, lowerBound, upperBound);
    }

    /**
     * Gets the number of all messages stored in this folder.
     * 
     * @return number of messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public int getMessageCount() throws MessagingException {
        openFolder();

        return this.currentFolder.getMessageCount();
    }

    /**
     * Gets the number of <i>new</i> (unread) messages stored in this folder.
     * 
     * @return number of messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public int getNewMessageCount() throws MessagingException {
        openFolder();

        return this.currentFolder.getUnreadMessageCount();
    }

    /**
     * Returns all new messages from the current folder, that is all messages
     * with the flag UNSEEN not set.
     * 
     * @param removeUnseen
     *            true, if the UNSEEN flag should be removed from the message
     * @return array of messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public Message[] getNewMessages(boolean removeUnseen) throws MessagingException {
        openFolder();

        return getNewMessages(1, this.getNewMessageCount(), removeUnseen);
    }

    /**
     * Returns all new messages in the given range from the current folder, that
     * is a number of messages with the flag UNSEEN not set.
     * 
     * @param lowerBound
     *            lower bound (inclusive)
     * @param upperBound
     *            upper bound (inclusive)
     * @param removeUnseen
     *            true, if the UNSEEN flag should be removed from the message
     * @return array of Message[]
     * @throws MessagingException
     */
    public Message[] getNewMessages(Integer lowerBound, Integer upperBound, boolean removeUnseen)
        throws MessagingException {
        openFolder();

        SearchTerm searchTerm =
                new OrTerm(new FlagTerm(new Flags(Flag.SEEN), false), new FlagTerm(new Flags(Flag.RECENT), true));

        Message[] newMessages = this.currentFolder.search(searchTerm);
        Message[] result = limitMessagesToRange(newMessages, lowerBound, upperBound);

        if (removeUnseen) {
            Flags supportedFlags = this.currentFolder.getPermanentFlags();

            Flags seenFlag = new Flags(Flag.SEEN);
            if (supportedFlags.contains(seenFlag)) {
                this.currentFolder.setFlags(result, seenFlag, true);
            }

            Flags recentFlag = new Flags(Flag.RECENT);
            if (supportedFlags.contains(recentFlag)) {
                // servers usually don't support setting this by client,
                // mock-javamail does so we check it here...
                this.currentFolder.setFlags(result, recentFlag, false);
            }
        }
        return result;
    }

    /**
     * Checks, if the current folder has messages.
     * 
     * @return true if there are messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public boolean hasMessages() throws MessagingException {
        openFolder();

        return this.currentFolder.getMessageCount() > 0;
    }

    /**
     * Checks, if the current folder has at least one <i>new</i> (unread)
     * message.
     * 
     * @return true if there are <i>new</i> messages
     * @throws MessagingException
     *             on all mailing errors
     */
    public boolean hasNewMessages() throws MessagingException {
        openFolder();

        return this.currentFolder.getUnreadMessageCount() > 0;
    }

    /**
     * Limits an array of Messages to the given bounds and returns a new one.
     * 
     * @param messages
     *            array of messages
     * @param lowerBound
     *            lower bound (1 for the first element)
     * @param upperBound
     *            upper bound (messages.length for the last element)
     * @return a shortened array
     */
    public Message[] limitMessagesToRange(Message[] messages, Integer lowerBound, Integer upperBound) {
        int nrOfMessages = messages.length;

        if (lowerBound == 1 && upperBound == messages.length) {
            // all mails requested - don't need to shorten array
            return messages;
        }

        Message[] result = new Message[0];
        if (lowerBound <= upperBound && nrOfMessages > 0) {

            if (lowerBound < 1) {
                lowerBound = 1;
            }

            if (upperBound > nrOfMessages) {
                upperBound = nrOfMessages;
            }

            result = new Message[upperBound - lowerBound + 1];
            for (int i = lowerBound; i <= upperBound; i++) {
                result[i - lowerBound] = messages[i - lowerBound];
            }
        }

        return result;
    }

    /**
     * Lists all mails in the current folder.
     * 
     * @throws MessagingException
     *             on all mailing errors
     * @throws IOException
     *             if a {@link DataHandler} fails
     */
    public void listMails() throws MessagingException, IOException {
        openFolder();

        Message[] messages = getAllMessages();
        for (Message message : messages) {
            System.out.println(messageToString(message));
        }
    }

    /**
     * Opens the folder specified in the property file for the specified mail
     * account.
     * 
     * @param mailAccount
     *            name of the mail account used in the property file.
     * @throws MessagingException
     *             on all mailing errors
     */
    private void openFolder() throws MessagingException {
        if (this.currentStore == null || !this.currentStore.isConnected()) {
            connect();
        }

        if (this.currentFolder == null) {
            this.currentFolder =
                    this.currentStore.getFolder(WiLConfiguration.getInstance().getString(
                            mailAccount + "." + WiLConfiguration.FOLDER_PROPERTY_KEY, "INBOX"));
        }
        if (!this.currentFolder.isOpen()) {
            this.currentFolder.open(Folder.READ_WRITE);
        }
    }

    /**
     * Sets the specific flag for a given mail account.
     * 
     * @param mailTargetID
     *            ID of the mail account
     * @param flag
     *            flag to set
     * @param set
     *            set it or remove it
     * @throws MessagingException
     *             thrown when there were errors accessing a mail account
     */
    public void setFlagForMessages(Flag flag, boolean set, Integer lowerBound, Integer upperBound)
        throws MessagingException {
        openFolder();

        Flags supportedFlags = this.currentFolder.getPermanentFlags();
        if (supportedFlags.contains(flag)) {
            if (lowerBound < 1) {
                lowerBound = 1;
            }

            int messagesCount = this.currentFolder.getMessageCount();
            if (upperBound > messagesCount) {
                upperBound = messagesCount;
            }

            this.currentFolder.setFlags(lowerBound, upperBound, new Flags(flag), set);
        }
    }
}
