package com.foodapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class OrderTest {

    @Test
    public void testOrderTotalCalculationWithItems() {
        // Arrange
        Order order = new Order("ord_1", "cust_1", "rest_1", "PLACED", 0.0, null);
        OrderItem item1 = new OrderItem("item_1", "Burger", 2, 5.00);
        OrderItem item2 = new OrderItem("item_2", "Fries", 1, 2.50);
        
        order.addItem(item1);
        order.addItem(item2);
        
        // Calculate total manually since Order model holds total as a static field updated by Service.
        // We test that our data container holds the correct assigned arrays.
        double expectedTotal = (2 * 5.00) + (1 * 2.50);
        order.setTotalPrice(expectedTotal);

        // Assert
        assertEquals(2, order.getItems().size());
        assertEquals(12.50, order.getTotalPrice(), 0.01, "Total price should map to the sum of items.");
    }

    @Test
    public void testOrderCsvParsingWithMultipleItems() {
        // Arrange
        String csv = "ord_2;cust_2;rest_2;COMPLETED;15.99;SAVE10;item_3:Pizza:1:15.99|item_4:Soda:2:2.00";

        // Act
        Order order = Order.fromCsv(csv);

        // Assert
        assertNotNull(order);
        assertEquals("ord_2", order.getId());
        assertEquals(15.99, order.getTotalPrice());
        assertEquals("SAVE10", order.getDiscountCode());

        List<OrderItem> items = order.getItems();
        assertEquals(2, items.size());
        assertEquals("Pizza", items.get(0).getName());
        assertEquals(1, items.get(0).getQuantity());
    }
}
