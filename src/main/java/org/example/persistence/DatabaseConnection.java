package org.example.persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

public class DatabaseConnection {
    public static void main(String[] args) throws SQLException {
        Connection conn = getConnection();

        if (conn != null) {
            System.out.println("works");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\mmarc\\Documents\\FH\\SE1\\postgres_demo\\src\\main\\resources\\db\\init.sql"));
            String line;
            String initStatement = "";
            while ((line = br.readLine()) != null) {
                initStatement += line;
            }
            assert conn != null;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(initStatement);

            stmt.execute("SELECT * FROM Users");
            ResultSet rs = stmt.getResultSet();
            while(rs.next()){
                System.out.println(rs.getInt(1) + ", " + rs.getString(2));
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5332/postgres?user=postgres&password=password";
        return DriverManager.getConnection(dbUrl);
    }

}
