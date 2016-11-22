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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sprylab.webinloop.WiLConfiguration;

/**
 * @author rzimmer
 * 
 */
public class Database {

    /**
     * Logger instance.
     */
    private static Log log = LogFactory.getLog(Database.class);

    /**
     * The database instance.
     */
    private static Database instance = new Database();

    /**
     * Returns the instance to access the database.
     * 
     * @return database instance
     */
    public static Database getInstance() {
        return instance;
    }

    /**
     * Configurations for DB connection saved in a hash map where key is the
     * connection name and value the DB configuration.
     */
    private HashMap<String, DatabaseConfiguration> dbConfigs = new HashMap<String, DatabaseConfiguration>();

    /**
     * Connections to DBs.
     */
    private HashMap<String, Connection> dbConnections = new HashMap<String, Connection>();

    /**
	 * 
	 */
    public Database() {
        // extract db connections
        String dbConnectionsProperty =
                WiLConfiguration.getInstance().getString(WiLConfiguration.DB_CONNECTIONS_PROPERTY_KEY);
        if (dbConnectionsProperty != null) {
            // db connections supplied, now analyze them
            String[] dbConnections = dbConnectionsProperty.split(";");
            for (String dbConnection : dbConnections) {
                // build db configuration for each defined connection
                String prefix = dbConnection + ".";
                this.dbConfigs.put(dbConnection, DatabaseConfiguration.createFromProperties(prefix));
            }
        }
    }

    /**
     * Returns a DB connection given the DB key string.
     * 
     * @param dbConnection
     *            the DB connection to retrieve
     * @return the data base connection if a configuration was provided.
     * @throws Exception
     *             if establishing a connection to the database failed
     */
    private Connection getDbConnection(String dbConnection) throws Exception {
        DatabaseConfiguration dbConfig = this.dbConfigs.get(dbConnection);
        Connection connection = this.dbConnections.get(dbConnection);

        if ((connection == null) && (dbConfig != null)) {
            connection =
                    createDatabaseConnection(dbConnection, dbConfig.getJdbcDriver(), dbConfig.getJdbcURL(),
                            dbConfig.getDbUser(), dbConfig.getDbPassword());
        }
        return connection;
    }

    /**
     * Create connection to database if not existent.
     * 
     * @param db
     *            name of the database to connect to
     * @param driver
     *            the database driver
     * @param jdbcUrl
     *            String defining URL to connect to
     * @param dbUser
     *            String defining user for database
     * @param dbPassword
     *            String defining password for database
     * @return Connection to database specified by parameter DB
     * @throws Exception
     *             if connection can not be established or initialization of
     *             fails.
     */
    private Connection createDatabaseConnection(String db, String driver, String jdbcUrl, String dbUser,
            String dbPassword) throws Exception {
        if (driver == null || jdbcUrl == null || dbUser == null || dbPassword == null) {
            throw new IllegalArgumentException(
                    "One or more DB parameter is missing. Please check that you have specified the following DB parameters correctly: driver, JBDC URL, username, password.");
        }

        Connection connection = this.dbConnections.get(db);

        if (connection == null) {
            try {
                DriverManager.registerDriver((java.sql.Driver) Class.forName(driver).newInstance());
            } catch (ClassNotFoundException e) {
                throw new Exception("The JBDC driver " + driver
                        + " could not be found. Please make sure that it is in your classpath.", e);
            }
            connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        }
        return connection;
    }

    /**
     * Close all connections to databases.
     */
    public void closeDatabaseConnections() {
        for (Connection dbConnection : this.dbConnections.values()) {
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (SQLException e) {
                    // don't handle, only log it
                    log.error("There was a error while trying to close the databases.", e);
                }
                dbConnection = null;
            }
        }
    }

    /**
     * Executes a database update.
     * 
     * @param db
     *            database identifier
     * @param sql
     *            SQL statement
     * @return number of changed rows
     * @throws Exception
     *             if no database connection could be established or the
     *             statement could not be executed
     */
    public int execUpdate(String db, String sql) throws Exception {
        Statement statement = getDbConnection(db).createStatement();
        int rows = statement.executeUpdate(sql);
        statement.close();

        return rows;
    }

    /**
     * Executes a database query.
     * 
     * @param db
     *            database identifier
     * @param sql
     *            SQL statement
     * @return query result as string
     * @throws Exception
     *             if no database connection could be established or the
     *             statement could not be executed
     */
    public String execQuery(String db, String sql) throws Exception {
        Statement statement = getDbConnection(db).createStatement();
        ResultSet rs = statement.executeQuery(sql);
        String result;
        if (rs.next()) {
            result = String.valueOf(rs.getObject(1));
            rs.close();
            statement.close();
        } else {
            rs.close();
            statement.close();
            throw new SQLException("Result is empty: " + sql);
        }

        return result;
    }
}
