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

package com.sprylab.webinloop.util.db;

import com.sprylab.webinloop.WiLConfiguration;

/**
 * Configuration class for DB connections.
 * 
 * @author rzimmer
 */
public class DatabaseConfiguration {

    /**
     * Factory method for a new DB configuration object created using
     * information from the passed property object.
     * 
     * @param prefix
     *            the prefix of the DB connection
     * @return a DB configuration object containing DB information
     */
    public static DatabaseConfiguration createFromProperties(String prefix) {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDbUser(WiLConfiguration.getInstance().getString(prefix + WiLConfiguration.DB_USER_PROPERTY_KEY));
        dbConfig.setDbPassword(WiLConfiguration.getInstance().getString(
                prefix + WiLConfiguration.DB_PASSWORD_PROPERTY_KEY));
        dbConfig.setJdbcDriver(WiLConfiguration.getInstance().getString(
                prefix + WiLConfiguration.JDBC_DRIVER_PROPERTY_KEY));
        dbConfig.setJdbcURL(WiLConfiguration.getInstance().getString(prefix + WiLConfiguration.JDBC_URL_PROPERTY_KEY));
        return dbConfig;
    }

    /**
     * The DB password.
     */
    private String dbPassword = "";

    /**
     * The DB user.
     */
    private String dbUser = "";

    /**
     * The JBDC driver.
     */
    private String jdbcDriver = "";

    /**
     * The JBDC URL.
     */
    private String jdbcURL = "";

    /**
     * Default constructor creating an empty DB configuration.
     */
    public DatabaseConfiguration() {
    }

    /**
     * @return the dbPassword
     */
    public String getDbPassword() {
        return this.dbPassword;
    }

    /**
     * @return the dbUser
     */
    public String getDbUser() {
        return this.dbUser;
    }

    /**
     * @return the jdbcDriver
     */
    public String getJdbcDriver() {
        return this.jdbcDriver;
    }

    /**
     * @return the jdbcURL
     */
    public String getJdbcURL() {
        return this.jdbcURL;
    }

    /**
     * @param dbPassword
     *            the dbPassword to set
     */
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    /**
     * @param dbUser
     *            the dbUser to set
     */
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * @param jdbcDriver
     *            the jdbcDriver to set
     */
    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    /**
     * @param jdbcURL
     *            the jdbcURL to set
     */
    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }
}
