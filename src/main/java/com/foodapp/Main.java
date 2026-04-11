package com.foodapp;

import com.foodapp.api.RestaurantWebServiceImpl;
import com.foodapp.storage.FileStorage;
import com.foodapp.ui.ConsoleUI;
import jakarta.xml.ws.Endpoint;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing Food Delivery Application...");

        // 1. Initialize data folders
        FileStorage.initialize();

        // 2. Publish SOAP Web Service
        String url = "http://localhost:8080/ws/restaurants";
        try {
            Endpoint.publish(url, new RestaurantWebServiceImpl());
            System.out.println("WSDL SOAP Service automatically published at: " + url + "?wsdl");
            System.out.println("You can view the WSDL in your browser to verify it's working.");
        } catch (Exception e) {
            System.out.println("Failed to publish Web Service. Port might be in use or missing dependencies. Error: " + e.getMessage());
        }

        // 3. Start Command Line Application
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
