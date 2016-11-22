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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

public class MailAccountParseResult {

    /**
     * ID for mail account.
     */
    private String mailAccount = "";

    /**
     * Lower bound for mail list range.
     */
    private Integer lowerBound = null;

    /**
     * Upper bound for mail list range.
     */
    private Integer upperBound = null;

    /**
     * Target component of mail.
     */
    private String target = "";

    /**
     * Splits a mail string in it's components and creates a corresponding
     * {@link MailAccountParseResult} object.
     * 
     * @param string
     *            mail string of format
     *            <code>mailAccount[:from-to]:mailtarget</code>
     * @return a new {@link MailAccountParseResult} instance with all relevant
     *         information set
     */
    public static MailAccountParseResult parse(String string) throws MessagingException {
        MailAccountParseResult result = new MailAccountParseResult();

        String mailAccountRegex = "(\\w+)(:(\\d+)-(\\d+))?(:(\\w+))?";
        Pattern mailAccountPattern = Pattern.compile(mailAccountRegex);
        Matcher matcher = mailAccountPattern.matcher(string);

        if (matcher.matches()) {
            result.mailAccount = matcher.group(1);

            try {
                int tempLowerBound = Integer.parseInt(matcher.group(3));
                int tempUpperBound = Integer.parseInt(matcher.group(4));

                if (tempLowerBound > tempUpperBound) {
                    // correct order
                    int temp = tempUpperBound;
                    tempUpperBound = tempLowerBound;
                    tempLowerBound = temp;
                }

                if (tempLowerBound < 0) {
                    // correct lower bound
                    tempLowerBound = 0;
                }

                result.lowerBound = tempLowerBound;
                result.upperBound = tempUpperBound;

            } catch (NumberFormatException e) {
                result.lowerBound = 0;
                result.upperBound = Integer.MAX_VALUE;
            }

            result.target = matcher.group(6);

        } else {
            throw new MessagingException("Cannot parse mail string: " + string);
        }

        return result;
    }

    public String getMailAccount() {
        return mailAccount;
    }

    public Integer getLowerBound() {
        return lowerBound;
    }

    public Integer getUpperBound() {
        return upperBound;
    }

    public String getTarget() {
        return target;
    }

}
