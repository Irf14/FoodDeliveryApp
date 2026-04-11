package com.foodapp.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String id;
    private String customerId;
    private String restaurantId;
    private String status; // PLACED, PREPARING, DELIVERING, COMPLETED
    private double totalPrice;
    private String discountCode;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
    }

    public Order(String id, String customerId, String restaurantId, String status, double totalPrice, String discountCode) {
        this.id = id;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.discountCode = discountCode;
        this.items = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void addItem(OrderItem item) { this.items.add(item); }

    public String toCsv() {
        List<String> itemStrs = new ArrayList<>();
        for (OrderItem item : items) {
            itemStrs.add(item.toCsv());
        }
        String itemsJoined = String.join("|", itemStrs);
        return String.join(";", id, customerId, restaurantId, status, String.valueOf(totalPrice), discountCode, itemsJoined);
    }

    public static Order fromCsv(String csv) {
        String[] parts = csv.split(";", -1);
        if (parts.length >= 7) {
            Order order = new Order(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]), parts[5]);
            String itemsStr = parts[6];
            if (!itemsStr.isEmpty()) {
                String[] itemParts = itemsStr.split("\\|");
                for (String itemStr : itemParts) {
                    OrderItem item = OrderItem.fromCsv(itemStr);
                    if (item != null) order.addItem(item);
                }
            }
            return order;
        }
        return null;
    }
}
