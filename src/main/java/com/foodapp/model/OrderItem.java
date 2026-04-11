package com.foodapp.model;

public class OrderItem {
    private String menuItemId;
    private String name;
    private int quantity;
    private double price;

    public OrderItem() {}

    public OrderItem(String menuItemId, String name, int quantity, double price) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String toCsv() {
        return String.join(":", menuItemId, name, String.valueOf(quantity), String.valueOf(price));
    }

    public static OrderItem fromCsv(String str) {
        String[] parts = str.split(":", -1);
        if (parts.length >= 4) {
            return new OrderItem(parts[0], parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]));
        }
        return null;
    }
}
