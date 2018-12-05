package com.company.Model;

import com.company.Controller.SQLConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class Admin extends User {

    private final String UPDATE_PRICE_BY_UPC_QUERY = "UPDATE Product SET price = ? WHERE upc = ?";
    private final String UPDATE_PRICE_BY_NAME_QUERY = "UPDATE Product SET price = ? WHERE name = ?";
    private final String CREATE_REORDER_REQUEST = "INSERT INTO Reorder (orderNumber, product, store, stockRequested)" +
            "                                      VALUES (?, ?, ?, ?)";
    private final String ORDER_NUMBERS = "SELECT ordernumber FROM Reorder";
    private final String GET_UNFULFILLED_ORDERS = "SELECT ordernumber, product, store, stockRequested FROM Reorder" +
            " WHERE deliveryDate IS NULL";
    private final String UPDATE_STOCK = "UPDATE soldby SET numberinstock = ((SELECT stockrequested FROM " +
            "reorder WHERE store = ? AND product = ?) + (SELECT numberInStock FROM soldBy WHERE storeId = ? AND " +
            "productId = ?)) WHERE storeId = ? AND productId = ?";
    private final String UPDATE_REORDER_TABLE = "UPDATE reorder SET deliverydate = ?, fulfilledBy = (SELECT " +
            "distributedBy.vendor FROM distributedBy JOIN Product ON distributedBy.brand = product.brand " +
            "WHERE product.upc = ?) WHERE ordernumber = ?";
    private String username;

    public Admin(String username){
        super();
        this.username = username;
    }

    public void requestReorder(Store store, Product item, int quantity) {
        try {
            ArrayList<String> orderNumbers = new ArrayList<>();
            Random r = new Random();
            String ONString;
            PreparedStatement stmt;
            ResultSet rs;
            stmt = this.getCon().prepareStatement(ORDER_NUMBERS);
            rs = stmt.executeQuery();
            while(rs.next()){
                orderNumbers.add(rs.getString(1));
            }
            int orderNumber = r.nextInt((99999999 - 10000000) + 1) + 10000000;
            ONString = Integer.toString(orderNumber);
            while(orderNumbers.contains(ONString)){
                orderNumber = r.nextInt((99999999 - 10000000) + 1) + 10000000;
                ONString = Integer.toString(orderNumber);
            }
            stmt = this.getCon().prepareStatement(CREATE_REORDER_REQUEST);
            stmt.setString(1, ONString);
            stmt.setString(2, item.getUpc());
            stmt.setString(3, store.getId());
            stmt.setInt(4, quantity);
            stmt.executeUpdate();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void fulfillReorders(){
        try {
            PreparedStatement stmt = this.getCon().prepareStatement(GET_UNFULFILLED_ORDERS);
            ResultSet rs = stmt.executeQuery();
            Date date = new Date(Calendar.getInstance().getTime().getTime());
            if (!rs.next()) {
                System.out.println("No unfulfilled reorders!");
            } else {
                do {
                    String orderNumber = rs.getString(1);
                    String upc = rs.getString(2);
                    String storeID = rs.getString(3);
                    int stockRequested = rs.getInt(4);
                    stmt = this.getCon().prepareStatement(UPDATE_STOCK);
                    stmt.setString(1, storeID);
                    stmt.setString(2, upc);
                    stmt.setString(3, storeID);
                    stmt.setString(4, upc);
                    stmt.setString(5, storeID);
                    stmt.setString(6, upc);
                    stmt.executeUpdate();

                    stmt = this.getCon().prepareStatement(UPDATE_REORDER_TABLE);
                    stmt.setDate(1, date);
                    stmt.setString(2, upc);
                    stmt.setString(3, orderNumber);
                    stmt.executeUpdate();
                }while (rs.next()); }
        } catch (SQLException e){
            System.out.println("Error while fulfilling reorder");
            e.printStackTrace();
        }
    }


    public void updatePriceByUPC(String upc, double price) {
        try {
            PreparedStatement stmt = this.getCon().prepareStatement(UPDATE_PRICE_BY_UPC_QUERY);
            stmt.setDouble(1, price);
            stmt.setString(2, upc);
            int rs = stmt.executeUpdate();
        } catch (SQLException e){
            System.out.println("Error: updatingPrice for upc: " + upc);
        }
    }

    public void updatePriceByName(String name, double price) {
        try {
            PreparedStatement stmt = getCon().prepareStatement(UPDATE_PRICE_BY_NAME_QUERY);
            stmt.setDouble(1, price);
            stmt.setString(2, name);
            int rs = stmt.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> viewAllVendorNames() {
        return null;
    }

    public ArrayList<String> viewAllBrandNames() {
        return null;
    }


}
