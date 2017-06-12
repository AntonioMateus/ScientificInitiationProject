package br.Color3;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    //MySQL JDBC driver name and database URL
    static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String MYSQL_DB_URL = "jdbc:mysql://localhost";
    static final String NEO4J_DB_URL = "jdbc:neo4j:bolt://localhost";

    public static Connection mysqlConnect() {
        //Database credentials
        final String USER = "root";
        final String PASS = "admin";
        Connection conn = null;

        try {
            Class.forName(MYSQL_JDBC_DRIVER);
            conn = DriverManager.getConnection(MYSQL_DB_URL, USER, PASS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public static Connection neo4jConnect() {
        //Database credentials
        final String USER = "neo4j";
        final String PASS = "admin";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(NEO4J_DB_URL, USER, PASS);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
