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

import javax.mail.MessagingException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sprylab.webinloop.util.mailer.MailAccountParseResult;

@Test
public class MailAccountParseResultTest {

    private String buildMailTargetString(String account, Integer lowerBound, Integer upperBound, String target) {
        String result = account;

        if (lowerBound != null && upperBound != null) {
            result += ":" + lowerBound + "-" + upperBound;
        }

        if (target != null) {
            result += ":" + target;
        }

        return result;
    }

    private void testParseMailTargetString(String account, Integer lowerBound, Integer upperBound, String target)
        throws MessagingException {

        String mailTarget = buildMailTargetString(account, lowerBound, upperBound, target);

        MailAccountParseResult result = MailAccountParseResult.parse(mailTarget);

        if (lowerBound == null) {
            lowerBound = 0;
        }

        if (upperBound == null) {
            upperBound = Integer.MAX_VALUE;
        }

        Assert.assertEquals(result.getMailAccount(), account);
        Assert.assertEquals(result.getLowerBound(), lowerBound);
        Assert.assertEquals(result.getUpperBound(), upperBound);
        Assert.assertEquals(result.getTarget(), target);
    }

    @Test
    public void parseAccount() throws MessagingException {
        String account = "mail1";
        Integer lowerBound = null;
        Integer upperBound = null;
        String target = null;

        testParseMailTargetString(account, lowerBound, upperBound, target);

    }

    @Test
    public void parseAccountAndTarget() throws MessagingException {
        String account = "mail1";
        Integer lowerBound = null;
        Integer upperBound = null;
        String target = "subject";

        testParseMailTargetString(account, lowerBound, upperBound, target);
    }

    @Test
    public void parseAccountRangeAndTarget() throws MessagingException {
        String account = "mail1";
        Integer lowerBound = 1;
        Integer upperBound = 10;
        String target = "subject";

        testParseMailTargetString(account, lowerBound, upperBound, target);
    }

}
