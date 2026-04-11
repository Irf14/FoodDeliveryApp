package com.foodapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserCreationAndCsvConversion() {
        // Arrange
        User user = new User("123", "john_doe", "password123", "CUSTOMER", "Downtown");

        // Act
        String csvOutput = user.toCsv();

        // Assert
        assertEquals("123;john_doe;password123;CUSTOMER;Downtown", csvOutput, "CSV format should match exactly separated by semicolons.");
    }

    @Test
    public void testUserFromCsvParsing() {
        // Arrange
        String csvLine = "456;jane_doe;secret456;RESTAURANT_OWNER;Uptown";

        // Act
        User user = User.fromCsv(csvLine);

        // Assert
        assertNotNull(user);
        assertEquals("456", user.getId());
        assertEquals("jane_doe", user.getUsername());
        assertEquals("secret456", user.getPassword());
        assertEquals("RESTAURANT_OWNER", user.getRole());
        assertEquals("Uptown", user.getAddress());
        System.out.println("User parsing tested successfully!");
    }
}
