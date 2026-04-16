package com.foodapp.service;

import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import com.foodapp.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RestaurantService {

    private static final String REST_FILE = "restaurants.txt";
    private static final String MENU_FILE = "menus.txt";

    public Restaurant registerRestaurant(String name, String ownerId, String area, String openTime, String closeTime) {
        // Check if a restaurant with exactly the same name and owner already exists
        List<Restaurant> existingRests = getAllRestaurants();
        for (Restaurant existing : existingRests) {
            if (existing.getName().equalsIgnoreCase(name) && existing.getOwnerId().equals(ownerId)) {
                System.out.println("Error: You already have a restaurant registered with this name!");
                return null; // Prevent duplicate
            }
        }

        String id = UUID.randomUUID().toString();
        Restaurant restaurant = new Restaurant(id, name, ownerId, area, openTime, closeTime, true);

        List<String> lines = FileStorage.readAllLines(REST_FILE);
        lines.add(restaurant.toCsv());
        FileStorage.writeAllLines(REST_FILE, lines);

        return restaurant;
    }

    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> list = new ArrayList<>();
        List<String> lines = FileStorage.readAllLines(REST_FILE);
        for (String line : lines) {
            list.add(Restaurant.fromCsv(line));
        }
        return list;
    }

    public void addMenuItem(String restaurantId, String name, double price, int quantity, String customizations) {
        List<MenuItem> existingRests = getMenuForRestaurant(restaurantId);
        for (MenuItem existing : existingRests) {
            if (existing.getName().equalsIgnoreCase(name)) {
                System.out.println("Error: You already have a MenuItem registered with this name!");
                return; // Prevent duplicate
            }
        }
        String id = UUID.randomUUID().toString();
        MenuItem item = new MenuItem(id, restaurantId, name, price, true, quantity, customizations);

        List<String> lines = FileStorage.readAllLines(MENU_FILE);
        lines.add(item.toCsv());
        FileStorage.writeAllLines(MENU_FILE, lines);
    }

    public List<MenuItem> getMenuForRestaurant(String restaurantId) {
        List<MenuItem> list = new ArrayList<>();
        List<String> lines = FileStorage.readAllLines(MENU_FILE);
        for (String line : lines) {
            MenuItem item = MenuItem.fromCsv(line);
            if (item != null && item.getRestaurantId().equals(restaurantId)) {
                list.add(item);
            }
        }
        return list;
    }

    public void toggleRestaurantStatus(String restaurantId, boolean isOpen) {
        List<Restaurant> list = getAllRestaurants();
        List<String> updatedLines = new ArrayList<>();
        for (Restaurant r : list) {
            if (r.getId().equals(restaurantId)) {
                r.setOpen(isOpen);
            }
            updatedLines.add(r.toCsv());
        }
        FileStorage.writeAllLines(REST_FILE, updatedLines);
    }

    public void updateMenuItem(String itemId, String name, double price, int quantity, String customizations, boolean available) {
        List<String> lines = FileStorage.readAllLines(MENU_FILE);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            MenuItem item = MenuItem.fromCsv(line);
            if (item != null && item.getId().equals(itemId)) {
                item.setName(name);
                item.setPrice(price);
                item.setQuantity(quantity);
                item.setCustomizations(customizations);
                item.setAvailable(available);
                updatedLines.add(item.toCsv());
            } else {
                updatedLines.add(line);
            }
        }
        FileStorage.writeAllLines(MENU_FILE, updatedLines);
    }

    public void removeMenuItem(String itemId) {
        List<String> lines = FileStorage.readAllLines(MENU_FILE);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            MenuItem item = MenuItem.fromCsv(line);
            if (item != null && !item.getId().equals(itemId)) {
                updatedLines.add(line);
            }
        }
        FileStorage.writeAllLines(MENU_FILE, updatedLines);
    }
}
