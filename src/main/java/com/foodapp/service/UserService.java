package com.foodapp.service;

import com.foodapp.model.User;
import com.foodapp.storage.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService {

    private static final String FILE_NAME = "users.txt";

    public User login(String username, String password) {
        List<String> lines = FileStorage.readAllLines(FILE_NAME);
        for (String line : lines) {
            User user = User.fromCsv(line);
            if (user != null && user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user; // Login successful
            }
        }
        return null; // Login failed
    }

    public User register(String username, String password, String role, String address) {
        // Check if username already exists
        List<String> lines = FileStorage.readAllLines(FILE_NAME);
        for (String line : lines) {
            User existing = User.fromCsv(line);
            if (existing != null && existing.getUsername().equals(username)) {
                System.out.println("Username already taken!");
                return null;
            }
        }

        // Create new user
        String id = UUID.randomUUID().toString();
        User newUser = new User(id, username, password, role, address);
        lines.add(newUser.toCsv());
        
        FileStorage.writeAllLines(FILE_NAME, lines);
        return newUser;
    }
}
