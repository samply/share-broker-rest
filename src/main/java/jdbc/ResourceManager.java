/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
 * Contact: info@osse-register.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package jdbc;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The Class ResourceManager.
 */
public class ResourceManager  {

    private static final DataSource dataSource;

    private static final String JAVA_COMP_ENV = "java:comp/env";

    private static final String JDBC_POSTGRES = "jdbc/postgres/samply.share.broker";
    
    static {
        try {
            Context initContext = new InitialContext();
            Context context = (Context) initContext.lookup(JAVA_COMP_ENV);
            dataSource = (DataSource) context.lookup(JDBC_POSTGRES);
        } catch (NamingException ex) {
            throw new ExceptionInInitializerError("dataSource not initialized");
        }
    }

    /**
     * Gets the DSL context.
     *
     * @param connection
     *            the sql connection
     * @return the DSL context
     * @throws SQLException
     *             the SQL exception
     */
    public static synchronized DSLContext getDSLContext(Connection connection) {
        Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
        return DSL.using(configuration);
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     * @throws SQLException
     *             the SQL exception
     */
    public static synchronized Configuration getConfiguration() throws SQLException {
        return new DefaultConfiguration().set(getConnection()).set(SQLDialect.POSTGRES);
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     * @throws SQLException
     *             the SQL exception
     */
    public static synchronized Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Close.
     *
     * @param connection
     *            the conn
     */
    public static void close(Connection connection) {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Close.
     *
     * @param statement
     *            the statement
     */
    public static void close(PreparedStatement statement) {
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Close.
     *
     * @param resultSet
     *            the result set
     */
    public static void close(ResultSet resultSet) {
        try {
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }

}
