package com.foodapp.model;

/**
 * Encapsulates the details of a User in the system.
 * Demonstrates Object-Oriented concept of Encapsulation (private fields, public getters/setters).
 */
public class User {
    private String id;
    private String username;
    private String password;
    private String role; // CUSTOMER, RESTAURANT_OWNER, RIDER
    private String address;

    // Constructor
    public User(String id, String username, String password, String role, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    /**
     * Converts object to a delimited string for easy text file storage.
     */
    public String toCsv() {
        return String.join(";", id, username, password, role, address);
    }

    /**
     * Creates a User from a delimited string (read from file).
     */
    public static User fromCsv(String csvLine) {
        String[] parts = csvLine.split(";", -1);
        if (parts.length >= 5) {
            return new User(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }
        return null;
    }
}
