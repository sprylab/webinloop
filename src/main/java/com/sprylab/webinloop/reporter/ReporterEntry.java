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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sprylab.webinloop.directives.Directive;

/**
 * Represents one entry (that is, one row) in the test overview table.
 * 
 * @author rzimmer
 * 
 */
public class ReporterEntry {

    /**
     * The directive to run.
     */
    private Directive directive = null;

    /**
     * List with all error meessages for this reporter entry.
     */
    private List<ReporterErrorMessage> errorMessages = new ArrayList<ReporterErrorMessage>();

    /**
     * Flag indicating if the execution of the directive has failed.
     */
    private boolean failed = false;

    /**
     * Flag indication if the directive has been run already.
     */
    private boolean run = false;

    /**
     * The timestamp when this directive was run.
     */
    private Date timestamp = null;

    /**
     * Constructs a new {@link ReporterEntry} object based on the directive
     * passed as argument.
     * 
     * @param directive
     *            the directive that this entry is representing
     */
    public ReporterEntry(Directive directive) {
        this.directive = directive;
    }

    /**
     * Adds an error message to this entry.
     * 
     * @param errorMessage
     *            the error message to add
     */
    public void addErrorMessage(ReporterErrorMessage errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    /**
     * @return the directive
     */
    public Directive getDirective() {
        return this.directive;
    }

    /**
     * @return the errorMessages
     */
    public List<ReporterErrorMessage> getErrorMessages() {
        return this.errorMessages;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the failed
     */
    public boolean isFailed() {
        return this.failed;
    }

    /**
     * @return the run
     */
    public boolean isRun() {
        return this.run;
    }

    /**
     * @param directive
     *            the directive to set
     */
    public void setDirective(Directive directive) {
        this.directive = directive;
    }

    /**
     * @param failed
     *            the failed to set
     */
    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    /**
     * @param run
     *            the run to set
     */
    public void setRun(boolean run) {
        this.run = run;

        if (this.run) {
            // if directive is now running, set the timestamp
            this.timestamp = new Date();
        }
    }
}
