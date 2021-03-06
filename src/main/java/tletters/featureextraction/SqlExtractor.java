package tletters.featureextraction;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * This class uses SQLite JDBC 3.8.11.2 driver developed by Taro L. Saito
 */
public class SqlExtractor extends Extractor {

    private static final String DEFAULT_DRIVER = "org.sqlite.JDBC";
    private static final String DEFAULT_URL = "jdbc:sqlite:";
    private static final String DEFAULT_DB_PATH = "src/main/resources/sqlite/features.db";
    private static final String DEFAULT_DB_EXTENSION = ".db";
    private static final String DEFAULT_TABLE_NAME = "features";

    private Connection conn;
    private Statement stat;

    public SqlExtractor() {
        super();
        init(DEFAULT_DRIVER, DEFAULT_URL + DEFAULT_DB_PATH);
    }

    public SqlExtractor(String driver, String url) {
        super();
        init(driver, url);
    }

    private void init(String driver, String dbPath) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException eString) {
            System.err.println("Could not init JDBC driver - driver not found");
        }
        try {
            conn = DriverManager.getConnection(DEFAULT_URL + dbPath + DEFAULT_DB_EXTENSION);
            stat = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("Could not create Sql connection");
            e.printStackTrace();
        }
        createTable();
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Could not close connection");
        }
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + DEFAULT_TABLE_NAME +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(255), features varchar(255))";
        try {
            stat.execute(query);
        } catch (SQLException e) {
            System.err.println("Could not create table");
        }
    }

    @Override
    public Properties load() {
        try {
            String query = "SELECT * FROM " + DEFAULT_TABLE_NAME;
            ResultSet result = stat.executeQuery(query);
            String name, features;
            while (result.next()) {
                name = result.getString("name");
                features = result.getString("features");
                properties.put(name, features);
            }
        } catch (SQLException e) {
            System.err.println("Could not retrieve data from table");
        }
        return properties;
    }

    @Override
    public void save(String featureName, double[] features) {
        try {
            String query = "INSERT INTO " + DEFAULT_TABLE_NAME + " values (NULL, ?, ?);";
            PreparedStatement prepStmt = conn.prepareStatement(query);
            prepStmt.setString(1, featureName);
            prepStmt.setString(2, Arrays.toString(features).replace('[', ' ').replace(']', ' ').replace(',', ' ').trim());
            prepStmt.execute();
        } catch (SQLException e) {
            System.err.println("Could not insert data into table");
        }
    }
}
