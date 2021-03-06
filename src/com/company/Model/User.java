package com.company.Model;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class User {

    public enum UserType {
        admin, customer
    }
    private final String STORE_BY_ID_QUERY = "SELECT * FROM Store WHERE id = ?";
    private final String STORE_BY_TIME_QUERY = "SELECT * FROM Store WHERE openTime >= ? AND closeTime <= ?";
    private final String STORE_BY_STATE_QUERY = "SELECT * FROM Store WHERE state = ?";
    private final String GET_PRODUCT_FROM_NAME = "SELECT * FROM Product WHERE name = ?";
    private final String GET_PRODUCT_FROM_UPC= "SELECT * FROM Product WHERE upc = ?";
    private final String STORE_BY_PRODUCT_QUERY = "SELECT * FROM Store WHERE id IN (SELECT storeID FROM " +
            "soldBy WHERE productid IN (SELECT upc FROM Product WHERE name = ?))";
    private final String PRODUCT_BY_NAME_QUERY = "SELECT product.* FROM Product JOIN soldBy ON " +
            "soldBy.productId = product.upc WHERE soldBy.storeId = ? AND product.name = ? ORDER BY product.name ASC";
    private final String PRODUCT_BY_PRICE_RANGE = "SELECT product.* FROM Product JOIN soldBy ON " +
            "soldBy.productId = product.upc WHERE soldBy.storeId = ? AND product.price > ? AND price < ? ORDER BY " +
            "product.name ASC";
    private final String PRODUCT_BY_PRICE_AND_TYPE = "SELECT product.* FROM Product JOIN soldBy ON" +
            " soldBy.productId = product.upc WHERE soldBy.storeId = ? AND product.price > ? AND price < ? AND " +
            "type = ? ORDER BY product.name ASC";
    private final String PRODUCT_BY_BRAND_QUERY = "SELECT product.* FROM Product JOIN soldBy ON " +
            "soldBy.productId = product.upc WHERE soldBy.storeId = ? AND brand = ?";
    private final String PRODUCT_BY_TYPE = "SELECT product.* FROM Product JOIN soldBy ON" +
            " soldBy.productId = product.upc WHERE soldBy.storeId = ? AND type = ? ORDER BY product.name ASC";
    private final String ALL_PRODUCTS_IN_STORE = "SELECT product.* FROM Product JOIN soldBy ON" +
            " soldBy.productId = product.upc WHERE soldBy.storeId = ? ORDER BY product.name ASC";

    private Connection con;
    Store store;

    public User(Connection con){
        this.con = con;
        this.store = null;
    }

    ///////////////// GETTERS and SETTERS ///////////////////

    public void setStore(Store s){
        this.store = s;
        s.setCon(con);
    }
    public Connection getCon() {
        return con;
    }
    public Store getStore() {
        return store;
    }


    ////////////////// APPLICATION ///////////////////


    public void printCurrentStore(){
        if (this.store == null) {
            System.out.println("You currently do not have a store chosen! Use \"store set\" to set your store");
        } else {
            System.out.println(this.store.toString());
        }
    }

    /**
     * This function will set the user's main store to the store given by the
     * id.
     *
     * NOTE: if the user wants to change their store, the must use the changeStore() method
     * @param storeId  the id of the store
     */
    public void selectMainStore(String storeId){
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(STORE_BY_ID_QUERY);
            stmt.setString(1, storeId);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL ERROR: Couldn't set main store.");
            System.out.println(e.getMessage());
        }

        // get the store the user wants to set as their main store
        ArrayList<Store> result = Store.returnListOfStores(rs);
        assert result.size() == 0;
        // set the store to the only store in the result list
        setStore(result.get(0));

        // set the connection to the store
        store.setCon(this.con);
    }

    /**
     * This method should be overridden by each subclass so that
     * each user returns their propper command lines with their
     * restricted permissions
     * @return
     */
    public abstract picocli.CommandLine initCLI();

    //
    // QUERY METHODS
    //

    /**
     * This function will query the database to find all the stores
     * in a given state and print the list to the user
     * @param state the state abbreviation (i.e MA, WA, OR, NY, CA)
     */
    public void queryStoreByState(String state) {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(STORE_BY_STATE_QUERY);
            stmt.setString(1, state);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query by state");
            System.out.println(e.getMessage());
        }
        Store.printDatabaseResults(rs);
    }

    /**
     * this will query the database to find a single store
     * by a given id and print it
     * @param id the id number of the store as a string
     */
    public void queryStorebyID(String id) {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(STORE_BY_ID_QUERY);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query by id");
            System.out.println(e.getMessage());
        }
        Store.printDatabaseResults(rs);
    }

    /**
     * Will query the database and print out the
     * list of stores that carry the specified product
     * @param productName the name of the proudct
     */
    public void queryStoreByProduct(String productName) {
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(STORE_BY_PRODUCT_QUERY);
            stmt.setString(1, productName);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query by product");
            System.out.println(e.getMessage());
        }
        Store.printDatabaseResults(rs);
    }

    public void queryStoreByTime(int start, int end) {
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(STORE_BY_TIME_QUERY);
            stmt.setInt(1, start);
            stmt.setInt(2, end);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query by time");
            System.out.println(e.getMessage());
        }
        Store.printDatabaseResults(rs);
    }



    ////////////////////////// Product Related Queries //////////////////


    public Product createProductFromName(String name){
        try{
            PreparedStatement stmt = getCon().prepareStatement(GET_PRODUCT_FROM_NAME);
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new Product(name, rs.getString(2), rs.getString(1), rs.getDouble(6));
            }else{
                System.out.println("That product does not exist!");
                return null;
            }
        } catch (SQLException e){
            System.out.println("Error in createProductFromName");
            System.out.println("SQL Error in trying to query by time");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Product createProductFromUPC(String upc){
        try{
            PreparedStatement stmt = getCon().prepareStatement(GET_PRODUCT_FROM_UPC);
            stmt.setString(1, upc);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new Product(rs.getString("name"), rs.getString("brand"), rs.getString("upc"), rs.getDouble(6));
            }else{
                System.out.println("That product does not exist!");
                return null;
            }
        } catch (SQLException e){
            System.out.println("Error in createProductFromUPC");
            System.out.println(e.getMessage());
            return null;
        }
    }


    /**
     * Will query the database and print out the product
     * with the given name
     * @param name the name of the product
     */
    public void queryProductByName(String name) {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(PRODUCT_BY_NAME_QUERY);
            stmt.setString(1, store.getId());
            stmt.setString(2, name);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query product by name");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    /**
     * Will query the database and print a list of products
     * for a given price range
     * @param start the lower bound of the item as a double
     * @param end the upper bound of the item as a double
     */
    public void queryProductByPriceRange(double start, double end) {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(PRODUCT_BY_PRICE_RANGE);
            stmt.setString(1, store.getId());
            stmt.setDouble(2, start);
            stmt.setDouble(3, end);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query product by price range");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    /**
     * Queries the database and prints all items of a given price range
     * that are of a certain type (i.e snacks)
     * @param type
     * @param start
     * @param end
     */
    public void queryProductByTypeAndRange(String type, double start, double end) {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(PRODUCT_BY_PRICE_AND_TYPE);
            stmt.setString(1, store.getId());
            stmt.setDouble(2, start);
            stmt.setDouble(3, end);
            stmt.setString(4, type);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query product by price range and type");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    /**
     * Queries the database and prints a lists of items for a given brand
     * @param brand the brand name
     */
    public void queryProductByBrand(String brand) {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(PRODUCT_BY_BRAND_QUERY);
            stmt.setString(1, store.getId());
            stmt.setString(2, brand);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query product by brand");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    public void queryProductByType(String type) {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(PRODUCT_BY_TYPE);
            stmt.setString(1, store.getId());
            stmt.setString(2, type);
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query product by type");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    public void queryAllProducts() {
        if (!checkStoreSet()) return;
        ResultSet rs = null;
        try {
            PreparedStatement stmt = con.prepareStatement(ALL_PRODUCTS_IN_STORE);
            stmt.setString(1, store.getId());
            rs = stmt.executeQuery();
        } catch (SQLException e){
            System.out.println("SQL Error in trying to query all products");
            System.out.println(e.getMessage());
        }
        Product.printDatabaseResults(rs);
    }

    public boolean checkStoreSet() {
        if (getStore() == null) {
            System.out.println("Please use \"store set <id>\" to use this command.");
            return false;
        }
        return true;
    }
}
