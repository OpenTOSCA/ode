/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.il.dbutil;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.derby.impl.io.VFMemoryStorageFactory;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.ode.il.config.OdeConfigProperties;

public class DerbyEmbeddedDB extends InternalDB {

    private String _dbUrl = null;
    private String _dbName = null;

    public DerbyEmbeddedDB(OdeConfigProperties props) {
        super(props);
    }

    /**
     * Initialize embedded (DERBY) database.
     */
    @Override
    protected void initDataSource() throws DatabaseConfigException {
        _dbName = _odeConfig.getDbEmbeddedName();
        if (_workRoot == null) {
            _dbUrl = "jdbc:derby:memory:" + _dbName + ";create=true";
            try {
                DriverManager.getConnection(_dbUrl).close();
            } catch (SQLException se) {
                __log.error("",se);
            }
            EmbeddedDataSource dds = new EmbeddedDataSource();
            dds.setDatabaseName("memory:" + _dbName);
            dds.setUser("sa");
            dds.setCreateDatabase("true");
            _datasource = dds;
        } else {
            _dbUrl = "jdbc:derby:" + _workRoot + File.separator + _dbName;
            if (_odeConfig.isDbEmbeddedCreate()) {
                _dbUrl += ";create=true";
            }
            String clazz = org.apache.derby.jdbc.EmbeddedDriver.class.getName();
            _connectionManager = new DatabaseConnectionManager(_txm, _odeConfig);
            try {
                _connectionManager.init(_dbUrl, clazz, "sa", null);
            } catch (DatabaseConfigException ex) {
                __log.error("Unable to initialize connection pool", ex);
            }
            _datasource = _connectionManager.getDataSource();
            __log.debug("Using Embedded Database: " + _dbUrl);
        }
    }

    public void shutdownDB() {
        if (_connectionManager != null) {
            try {
                _connectionManager.shutdown();
            } catch (DatabaseConfigException ex) {
                __log.error("unable to shutdown connection pool", ex);
            }
        }
        try {
            DriverManager.getConnection(_dbUrl + ";shutdown=true").close();
        } catch (SQLException ex) {
            // Shutdown will always return an exeption!
            if (ex.getErrorCode() != 45000) {
                __log.error("Error shutting down Derby: " + ex.getErrorCode(), ex);
            }
        } catch (Throwable ex) {
            __log.debug("Error shutting down Derby.", ex);
        }
        if (_workRoot == null) {
            try {
                //Don't worry about File IO, this is only virtual
                VFMemoryStorageFactory.purgeDatabase(new File(_dbName).getCanonicalPath());
            } catch (Exception e) {
                __log.debug("Error shutting down Derby.", e);
            }
        }
    }
}