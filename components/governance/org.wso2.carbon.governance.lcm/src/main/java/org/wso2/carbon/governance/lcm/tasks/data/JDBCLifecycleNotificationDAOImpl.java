/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.lcm.tasks.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.lcm.tasks.LCNotification;
import org.wso2.carbon.governance.lcm.tasks.dao.LifecycleNotificationDAO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class is the JDBC implementation of LifecycleNotificationDAO which is used to add schedulers and read schedules.
 */
public class JDBCLifecycleNotificationDAOImpl implements LifecycleNotificationDAO {

    /**
     * Variable used to log.
     */
    private static final Log log = LogFactory.getLog(JDBCLifecycleNotificationDAOImpl.class);

    /**
     * MySQL SELECT used in queries.
     */
    private final String sqlSelect = "SELECT ";

    /**
     * MySQL FROM used in queries.
     */
    private final String sqlFrom = " FROM ";

    /**
     * MySQL INSERT INTO used in queries.
     */
    private final String sqlInsertInto = "INSERT INTO ";

    /**
     * MySQL VALUES used in queries.
     */
    private final String sqlValues = " VALUES ";

    /**
     * MySQL WHERE used in queries.
     */
    private final String sqlWhere = " WHERE ";

    /**
     * Equals sign used in MySQL queries.
     */
    private final String equals = " = ";

    /**
     * Comma used in MySQL queries.
     */
    private final String comma = ",";

    /**
     * Question mark sign used in MySQL queries.
     */
    private final String questionMark = "?";

    /**
     * Open bracket used to build sql query.
     */
    private final String openBracket = "(";

    /**
     * Close bracket used to build sql query.
     */
    private final String closeBracket = ")";

    /**
     * String used in sql add scheduler to build parameter string.
     */
    private final String addSchedulerParameterString = "(?,?,?,?,?,?)";

    /**
     * Date format used in MySQL queries.
     */
    private final String dateFormat = "yyyy-M-d";

    /**
     * This method is used get scheduler bran list filtering by notification method.
     *
     * @param registry              core registry.
     * @return                      Array list of scheduler data objects.
     * @throws GovernanceException  Throws when an error occurs reading data and committing transaction.
     */
    @Override
    public ArrayList<LCNotification> getValidNotifications(Registry registry)
            throws GovernanceException {
        if (registry == null) {
            return null;
        }

        String sql = getValidNotificationQuery();

        ArrayList<LCNotification> schedulerBeans = new ArrayList<LCNotification>();
        try {
            registry.beginTransaction();
        } catch (RegistryException e) {
            throw new GovernanceException("Error while reading data from registry while invoking beginTransaction for"
                    + " query: " + sql, e);
        }
        JDBCDatabaseTransaction.ManagedRegistryConnection connection =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, getCurrentDate());

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LCNotification schedulerBean = new LCNotification();
                schedulerBean.setRegPath(resultSet.getString(LifecycleNotificationDAO.REG_PATH));
                schedulerBean.setLcName(resultSet.getString(LifecycleNotificationDAO.REG_LC_NAME));
                schedulerBean.setLcCheckpointId(resultSet.getString(LifecycleNotificationDAO.REG_LC_CHECKPOINT_ID));
                schedulerBean.setNotificationDate(resultSet.getString(LifecycleNotificationDAO
                        .REG_LC_NOTIFICATION_DATE));
                schedulerBean.setTenantId(resultSet.getInt(LifecycleNotificationDAO.REG_TENANT_ID));
                schedulerBeans.add(schedulerBean);
            }
            try {
                registry.commitTransaction();
            } catch (RegistryException e) {
                throw new GovernanceException("Error while committing transaction of getting scheduler objects from "
                        + "query: " + sql, e);
            }
            return schedulerBeans;
        } catch (SQLException sqlException) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException registryException) {
                throw new GovernanceException("Error while committing transaction for query: " + sql,
                        registryException);
            }
            throw new GovernanceException("SQL error while getting schedulers from query: " + sql, sqlException);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error("SQL error while closing the prepared statement for sql :" + sql, e);
                }
            }
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("SQL error while closing the result set after executing sql :" + sql, e);
                }
            }
        }
    }

    /**
     * This method builds the SQL query to select schedulers from database.
     * Ex: "SELECT REG_PATH,REG_LC_NAME,REG_LC_CHECKPOINT_ID,REG_TENANT_ID FROM REG_CHECKPOINT_SCHEDULER WHERE
     * REG_LC_NOTIFICATION_DATE = ?".
     *
     * @return  SQL query to select schedulers from database.
     */
    private String getValidNotificationQuery() {

        StringBuilder queryBuilder = new StringBuilder(sqlSelect);
        queryBuilder.append(LifecycleNotificationDAO.REG_PATH);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_NAME);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_CHECKPOINT_ID);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_TENANT_ID);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_NOTIFICATION_DATE);
        queryBuilder.append(sqlFrom);
        queryBuilder.append(LifecycleNotificationDAO.TABLE_NAME);
        queryBuilder.append(sqlWhere);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_NOTIFICATION_DATE);
        queryBuilder.append(equals);
        queryBuilder.append(questionMark);
        return queryBuilder.toString();
    }

    /**
     * This method to used add schedulers.
     *
     * @param registry              core registry.
     * @param schedulerBean         checkpoint notification scheduler bean.
     * @return                      true is scheduler added successfully.
     * @throws GovernanceException  Throws when:
     *                              <ul>
     *                                  <li>If reading data from registry while invoking beginTransaction occurs an
     *                                  error.</li>
     *                                  <li>If committing transaction of adding checkpoint notification occurs an
     *                                  error.</li>
     *                              </ul>
     */
    @Override
    public boolean addScheduler(Registry registry, LCNotification schedulerBean)
            throws GovernanceException {
        if (registry == null && schedulerBean == null) {
            return false;
        }
        String sql = getAddSchedulerQuery();
        try {
            registry.beginTransaction();
        } catch (RegistryException e) {
            throw new GovernanceException("Error while reading data from registry while invoking beginTransaction for"
                    + " query: " + sql, e);
        }

        // Get database connection.
        JDBCDatabaseTransaction.ManagedRegistryConnection connection =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, schedulerBean.getRegPath());
            preparedStatement.setString(2, schedulerBean.getLcName());
            preparedStatement.setString(3, schedulerBean.getLcCheckpointId());
            preparedStatement.setString(4, schedulerBean.getUUID());
            preparedStatement.setInt(5, schedulerBean.getTenantId());
            preparedStatement.setString(6, schedulerBean.getNotificationDate());
            boolean result = preparedStatement.execute();
            try {
                registry.commitTransaction();
                return result;
            } catch (RegistryException e) {
                throw new GovernanceException("Error while committing transaction of adding checkpoint notification "
                        + "scheduler query: " + sql, e);
            }
        } catch (SQLException sqlException) {
            try {
                registry.rollbackTransaction();
            } catch (RegistryException registryException) {
                throw new GovernanceException("Error while transaction rollback for query: " + sql, registryException);
            }
            throw new GovernanceException("SQL error while creating scheduler data entry using query: " + sql,
                    sqlException);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error("SQL error while closing the prepared statement for sql :" + sql, e);
                }
            }
        }
    }

    /**
     * This method used to get current date in yyyy-M-d format.
     *
     * @return      current date in yyyy-M-d format.
     */
    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat(this.dateFormat);
        //get current date time with Calendar()
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    /**
     * This method is used to build add scheduler query.
     *
     * @return  add scheduler query.
     */
    private String getAddSchedulerQuery() {
        StringBuilder queryBuilder = new StringBuilder(sqlInsertInto);
        queryBuilder.append(LifecycleNotificationDAO.TABLE_NAME);
        queryBuilder.append(openBracket);
        queryBuilder.append(LifecycleNotificationDAO.REG_PATH);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_NAME);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_CHECKPOINT_ID);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_UUID);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_TENANT_ID);
        queryBuilder.append(comma);
        queryBuilder.append(LifecycleNotificationDAO.REG_LC_NOTIFICATION_DATE);
        queryBuilder.append(closeBracket);
        queryBuilder.append(sqlValues);
        queryBuilder.append(addSchedulerParameterString);
        return queryBuilder.toString();
    }
}
