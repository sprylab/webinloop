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

package com.sprylab.webinloop.util.mailer.tests;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sprylab.webinloop.WiLConfiguration;
import com.sprylab.webinloop.util.mailer.Mailer;
import com.sprylab.webinloop.util.mailer.Mailer.MailboxOperation;

@Test
public class MailerTest {

    private String mailAccount1 = "mail1";

    private String mailAccount2 = "mail2";

    private String sender = "test-sender@sprylab.com";

    private String recipient1 = "test-recipient1@sprylab.com";

    private String recipient2 = "test-recipient2@sprylab.com";

    private String subject = "subject";

    private String body = "body";

    private int nrOfMessages1 = 3;

    private int nrOfMessages2 = 5;

    private Mailer mailer1 = null;

    private Mailer mailer2 = null;

    private void sendMail(String recipient, String subject, String body) throws MessagingException {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()));
        msg.setSender(new InternetAddress(sender));
        msg.setRecipients(RecipientType.TO, recipient);
        msg.setSubject(subject);
        msg.setText(body);
        msg.setFlag(Flag.RECENT, true);
        Transport.send(msg);
    }

    private void prepareMailbox(String mailAccount, String recipient, int nrOfMessages) throws MessagingException,
        IOException {
        Mailer.executeMailboxOperation(MailboxOperation.PURGE, mailAccount);

        Mailer mailer = Mailer.getInstance(mailAccount);

        Assert.assertEquals(mailer.getNewMessageCount(), 0);
        Assert.assertEquals(mailer.getMessageCount(), 0);

        for (int i = 0; i < nrOfMessages; i++) {
            sendMail(recipient, subject + i, body + i);
        }

        String[] mails = Mailer.getMail(mailAccount, false, false, false);
        Assert.assertEquals(mails.length, nrOfMessages);
        Assert.assertEquals(mailer.getNewMessageCount(), nrOfMessages);
        Assert.assertEquals(mailer.getMessageCount(), nrOfMessages);
    }

    @BeforeTest
    public void init() throws MessagingException {
        WiLConfiguration.getInstance().setConfigDir(new File("src/test/resources/configs/MavenTest/"));

        mailer1 = Mailer.getInstance(mailAccount1);
        mailer2 = Mailer.getInstance(mailAccount2);
    }

    @BeforeMethod
    public void prepareMailbox() throws MessagingException, IOException {
        prepareMailbox(mailAccount1, recipient1, nrOfMessages1);
        prepareMailbox(mailAccount2, recipient2, nrOfMessages2);
    }

    @Test
    public void purgeMailbox1() throws MessagingException {
        Assert.assertEquals(mailer1.getMessageCount(), nrOfMessages1);

        Mailer.executeMailboxOperation(MailboxOperation.PURGE, mailAccount1);

        Assert.assertEquals(mailer1.getMessageCount(), 0);
    }

    @Test
    public void readMailbox1() throws MessagingException {
        Assert.assertEquals(mailer1.getNewMessageCount(), nrOfMessages1);

        Mailer.executeMailboxOperation(MailboxOperation.READ, mailAccount1);

        Assert.assertEquals(mailer1.getNewMessageCount(), 0);
    }

    @Test
    public void readMailbox1InRange() throws MessagingException {
        Assert.assertEquals(mailer1.getNewMessageCount(), nrOfMessages1);

        int lowerBound = 1;
        int upperBound = 3;

        Mailer.executeMailboxOperation(MailboxOperation.READ, mailAccount1 + ":" + lowerBound + "-" + upperBound);

        Assert.assertEquals(mailer1.getNewMessageCount(), upperBound - lowerBound - 1);
    }

    @Test
    public void purgeMailbox1InRange() throws MessagingException {
        Assert.assertEquals(mailer1.getMessageCount(), nrOfMessages1);

        int lowerBound = 0;
        int upperBound = 2;

        Mailer.executeMailboxOperation(MailboxOperation.PURGE, mailAccount1 + ":" + lowerBound + "-" + upperBound);

        Assert.assertEquals(mailer1.getMessageCount(), upperBound - lowerBound - 1);
    }

    @Test
    public void purgeMailbox1And2() throws MessagingException {
        Assert.assertEquals(mailer1.getMessageCount(), nrOfMessages1);
        Assert.assertEquals(mailer2.getMessageCount(), nrOfMessages2);

        Mailer.executeMailboxOperation(MailboxOperation.PURGE, mailAccount1 + ";" + mailAccount2);

        Assert.assertEquals(mailer1.getMessageCount(), 0);
        Assert.assertEquals(mailer2.getMessageCount(), 0);
    }

    @Test
    public void readMailbox1And2() throws MessagingException {
        Assert.assertEquals(mailer1.getNewMessageCount(), nrOfMessages1);
        Assert.assertEquals(mailer2.getNewMessageCount(), nrOfMessages2);

        Mailer.executeMailboxOperation(MailboxOperation.READ, mailAccount1 + ";" + mailAccount2);

        Assert.assertEquals(mailer1.getNewMessageCount(), 0);
        Assert.assertEquals(mailer2.getNewMessageCount(), 0);
    }

    @Test
    public void purgeMailboxAsterix() throws MessagingException {
        Assert.assertEquals(mailer1.getMessageCount(), nrOfMessages1);
        Assert.assertEquals(mailer2.getMessageCount(), nrOfMessages2);

        Mailer.executeMailboxOperation(MailboxOperation.PURGE, "*");

        Assert.assertEquals(mailer1.getMessageCount(), 0);
        Assert.assertEquals(mailer2.getMessageCount(), 0);
    }

    @Test
    public void readMailboxAsterix() throws MessagingException {
        Assert.assertEquals(mailer1.getNewMessageCount(), nrOfMessages1);
        Assert.assertEquals(mailer2.getNewMessageCount(), nrOfMessages2);

        Mailer.executeMailboxOperation(MailboxOperation.READ, "*");

        Assert.assertEquals(mailer1.getNewMessageCount(), 0);
        Assert.assertEquals(mailer2.getNewMessageCount(), 0);
    }

    @Test
    public void getMail1Subjects() throws MessagingException, IOException {
        Assert.assertEquals(mailer1.getMessageCount(), nrOfMessages1);

        String[] mailContents = Mailer.getMail(mailAccount1 + ":subject", false, false, false);
        for (int i = 0; i < mailContents.length; i++) {
            Assert.assertEquals(mailContents[i], subject + i);
        }

        Assert.assertEquals(mailContents.length, mailer1.getMessageCount());
    }

    @Test
    public void getMail1RemoveUnseen() throws MessagingException, IOException {
        Assert.assertEquals(mailer1.getNewMessageCount(), nrOfMessages1);

        String[] mailContents = Mailer.getMail(mailAccount1 + ":subject", true, false, true);
        for (int i = 0; i < mailContents.length; i++) {
            Assert.assertEquals(mailContents[i], subject + i);
        }

        Assert.assertEquals(mailer1.getNewMessageCount(), 0);
    }

}
