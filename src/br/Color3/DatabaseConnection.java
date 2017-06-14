package br.Color3;

//packages used by Neo4j and MySQL
import java.sql.Connection;
import java.sql.DriverManager;

//packages used only by MongoDB
import com.mongodb.MongoClient;

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

    public static MongoClient mongoConnect() {
        MongoClient mongoClient = null; //localhost at port 27017

        try {
            mongoClient = new MongoClient("localhost", 27017);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return mongoClient;
    }
}
