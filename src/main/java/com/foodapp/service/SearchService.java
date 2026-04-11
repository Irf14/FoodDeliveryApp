package com.foodapp.service;

import com.foodapp.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private RestaurantService restaurantService;

    public SearchService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Searches for nearest restaurants based on area.
     */
    public List<Restaurant> findRestaurantsByArea(String area) {
        List<Restaurant> all = restaurantService.getAllRestaurants();
        List<Restaurant> result = new ArrayList<>();
        
        for (Restaurant r : all) {
            if (r.getArea().equalsIgnoreCase(area) && r.isOpen()) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Searches restaurants by name.
     */
    public List<Restaurant> findRestaurantsByName(String nameQuery) {
        List<Restaurant> all = restaurantService.getAllRestaurants();
        List<Restaurant> result = new ArrayList<>();
        
        for (Restaurant r : all) {
            if (r.getName().toLowerCase().contains(nameQuery.toLowerCase())) {
                result.add(r);
            }
        }
        return result;
    }
}
