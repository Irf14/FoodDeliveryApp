package com.foodapp;

import com.foodapp.api.RestaurantWebServiceImpl;
import com.foodapp.storage.FileStorage;
import com.foodapp.ui.ConsoleUI;
import jakarta.xml.ws.Endpoint;

public class Main {
    public static void main(String[] args) {
        // Log startup
        System.out.println("Initializing Food Delivery Application...");

        // 1. Initialize data folders
        FileStorage.initialize();
            
        // 2. Publish SOAP Web Service (Requirement)
        String soapUrl = "http://localhost:8080/ws/restaurants";
        try {
            Endpoint.publish(soapUrl, new RestaurantWebServiceImpl());
            System.out.println("WSDL SOAP Service published at: " + soapUrl + "?wsdl");
        } catch (Exception e) {
            System.out.println("SOAP Service failed: " + e.getMessage());
        }

        // 3. Start Console UI (Main interaction)
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
