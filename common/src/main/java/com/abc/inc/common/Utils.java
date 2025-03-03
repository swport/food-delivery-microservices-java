package com.abc.inc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static String getFullStackTrace(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static void closeRdbConnection(Connection connection) {
        closeRdbConnection(connection, null, null);
    }

    public static void closeRdbConnection(Connection connection, PreparedStatement statement) {
        closeRdbConnection(connection, null, statement);
    }

    public static void closeRdbConnection(Connection connection, ResultSet resultSet, PreparedStatement statement) {
        if(resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Error closing resultSet: ", e);
            }
        }
        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("Error closing statement: ", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Error closing connection: ", e);
            }
        }
    }
}
