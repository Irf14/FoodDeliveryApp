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
}
