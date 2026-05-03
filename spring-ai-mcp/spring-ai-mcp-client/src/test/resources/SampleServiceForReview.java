package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SampleServiceForReview {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void processData(String userInput) {
        String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getItems() {
        List<String> items = new ArrayList();
        items.add("apple");
        items.add("banana");
        items.add("orange");
        return items;
    }

    public int calculate(int a, int b) {
        int result = a / b;
        return result;
    }

    public void doNothing() {
        int x = 1;
        int y = 2;
        int z = x + y;
    }

    public String getUserData(String id) {
        return null;
    }
}
