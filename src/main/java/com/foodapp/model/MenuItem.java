package com.foodapp.model;

public class MenuItem {
    private String id;
    private String restaurantId;
    private String name;
    private double price;
    private boolean available;
    private int quantity;
    private String customizations; // e.g. "Add-ons: extra cheese, spicy"

    public MenuItem() {}

    public MenuItem(String id, String restaurantId, String name, double price, boolean available, int quantity, String customizations) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.price = price;
        this.available = available;
        this.quantity = quantity;
        this.customizations = customizations;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCustomizations() { return customizations; }
    public void setCustomizations(String customizations) { this.customizations = customizations; }

    public String toCsv() {
        return String.join(";", id, restaurantId, name, String.valueOf(price), String.valueOf(available), String.valueOf(quantity), customizations);
    }

    public static MenuItem fromCsv(String csv) {
        String[] parts = csv.split(";", -1);
        if (parts.length >= 7) {
            return new MenuItem(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), 
                   Boolean.parseBoolean(parts[4]), Integer.parseInt(parts[5]), parts[6]);
        }
        return null;
    }
}
