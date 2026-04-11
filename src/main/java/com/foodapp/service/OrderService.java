package com.foodapp.service;

import com.foodapp.model.Order;
import com.foodapp.model.OrderItem;
import com.foodapp.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private static final String ORDER_FILE = "orders.txt";

    public Order placeOrder(String customerId, String restaurantId, List<OrderItem> items, String discountCode) {
        double total = 0;
        for (OrderItem item : items) {
            total += (item.getPrice() * item.getQuantity());
        }

        // Apply discount logic
        if (discountCode != null && discountCode.equals("SAVE10")) {
            total = total * 0.90; // 10% off
            System.out.println("Discount SAVE10 applied!");
        }

        String id = UUID.randomUUID().toString();
        Order order = new Order(id, customerId, restaurantId, "PLACED", total, discountCode);
        order.setItems(items);

        List<String> lines = FileStorage.readAllLines(ORDER_FILE);
        lines.add(order.toCsv());
        FileStorage.writeAllLines(ORDER_FILE, lines);

        return order;
    }

    public List<Order> getCustomerOrders(String customerId) {
        List<Order> list = new ArrayList<>();
        List<String> lines = FileStorage.readAllLines(ORDER_FILE);
        for (String line : lines) {
            Order o = Order.fromCsv(line);
            if (o != null && o.getCustomerId().equals(customerId)) {
                list.add(o);
            }
        }
        return list;
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        List<Order> list = new ArrayList<>();
        List<String> lines = FileStorage.readAllLines(ORDER_FILE);
        for (String line : lines) {
            Order o = Order.fromCsv(line);
            if (o != null) {
                if (o.getId().equals(orderId)) {
                    o.setStatus(newStatus);
                }
                list.add(o);
            }
        }
        
        List<String> updatedLines = new ArrayList<>();
        for (Order o : list) {
            updatedLines.add(o.toCsv());
        }
        FileStorage.writeAllLines(ORDER_FILE, updatedLines);
    }
}
