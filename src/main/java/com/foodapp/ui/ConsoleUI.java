package com.foodapp.ui;

import com.foodapp.model.Order;
import com.foodapp.model.OrderItem;
import com.foodapp.model.Restaurant;
import com.foodapp.model.User;
import com.foodapp.service.OrderService;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.UserService;
import com.foodapp.service.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private UserService userService;
    private RestaurantService restaurantService;
    private OrderService orderService;
    private SearchService searchService;
    private Scanner scanner;
    
    private User loggedInUser = null;

    public ConsoleUI() {
        this.userService = new UserService();
        this.restaurantService = new RestaurantService();
        this.orderService = new OrderService();
        this.searchService = new SearchService(restaurantService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=====================================");
        System.out.println("  Welcome to Food Delivery App CLI  ");
        System.out.println("=====================================");

        while (true) {
            if (loggedInUser == null) {
                showLoginMenu();
            } else if (loggedInUser.getRole().equals("CUSTOMER")) {
                showCustomerMenu();
            } else if (loggedInUser.getRole().equals("RESTAURANT_OWNER")) {
                showRestaurantOwnerMenu();
            } else {
                System.out.println("Invalid Role. Logging out...");
                loggedInUser = null;
            }
        }
    }

    private void showLoginMenu() {
        System.out.println("\n1. Login\n2. Register\n3. Exit");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            
            loggedInUser = userService.login(username, password);
            if (loggedInUser == null) {
                System.out.println("Invalid credentials!");
            } else {
                System.out.println("Login successful! Welcome " + loggedInUser.getUsername());
            }
        } else if (choice.equals("2")) {
            System.out.println("\n--- Registration ---");
            System.out.println("1. Register as Customer");
            System.out.println("2. Apply as Restaurant Owner");
            System.out.print("Choose option: ");
            String regChoice = scanner.nextLine();

            if (!regChoice.equals("1") && !regChoice.equals("2")) {
                System.out.println("Invalid option. Returning to main menu...");
                return;
            }

            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Address/Area: ");
            String address = scanner.nextLine();

            User u = null;
            if (regChoice.equals("1")) {
                u = userService.registerCustomer(username, password, address);
            } else if (regChoice.equals("2")) {
                u = userService.registerOwner(username, password, address);
            }

            if (u != null) {
                System.out.println("Registered successfully! Please login.");
            }
        } else if (choice.equals("3")) {
            System.out.println("Exiting application...");
            System.exit(0);
        }
    }

    private void showCustomerMenu() {
        System.out.println("\n--- Customer Dashboard ---");
        System.out.println("1. Find Nearest Restaurants by Area");
        System.out.println("2. View My Orders / Track Status");
        System.out.println("3. Logout");
        System.out.print("Choose option: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter your delivery area: ");
            String area = scanner.nextLine();
            List<Restaurant> list = searchService.findRestaurantsByArea(area);
            if (list.isEmpty()) {
                System.out.println("No open restaurants found in " + area);
                return;
            }
            
            System.out.println("Available Restaurants:");
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + ". " + list.get(i).getName());
            }
            
            System.out.print("Select a restaurant by number (or 0 to cancel): ");
            int restChoice = Integer.parseInt(scanner.nextLine());
            if (restChoice > 0 && restChoice <= list.size()) {
                Restaurant r = list.get(restChoice - 1);
                placeOrderFlow(r);
            }
        } else if (choice.equals("2")) {
            List<Order> myOrders = orderService.getCustomerOrders(loggedInUser.getId());
            if (myOrders.isEmpty()) System.out.println("No orders found.");
            for (Order o : myOrders) {
                System.out.println("Order ID: " + o.getId() + " | Status: " + o.getStatus() + " | Total: $" + o.getTotalPrice());
            }
        } else if (choice.equals("3")) {
            loggedInUser = null;
        }
    }

    private void placeOrderFlow(Restaurant r) {
        System.out.println("--- Selected Restaurant: " + r.getName() + " ---");
        System.out.println("Menu:");
        // Assuming we added a getMenuForRestaurant in service (which we did)
        var menuItems = restaurantService.getMenuForRestaurant(r.getId());
        if (menuItems.isEmpty()) {
            System.out.println("No menu items available.");
            return;
        }
        
        for (int i = 0; i < menuItems.size(); i++) {
            var mi = menuItems.get(i);
            System.out.println((i + 1) + ". " + mi.getName() + " - $" + mi.getPrice() + (mi.isAvailable() ? " (Available)" : " (Out of stock)"));
        }

        List<OrderItem> cart = new ArrayList<>();
        while(true) {
            System.out.print("Select item number to add to cart (or 0 to checkout): ");
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice == 0) break;
            
            if (itemChoice > 0 && itemChoice <= menuItems.size()) {
                var selectedItem = menuItems.get(itemChoice - 1);
                if (!selectedItem.isAvailable()) {
                    System.out.println("Item is out of stock!");
                    continue;
                }
                
                System.out.print("Quantity: ");
                int qty = Integer.parseInt(scanner.nextLine());
                cart.add(new OrderItem(selectedItem.getId(), selectedItem.getName(), qty, selectedItem.getPrice()));
                System.out.println(qty + "x " + selectedItem.getName() + " added to cart.");
            }
        }

        if (!cart.isEmpty()) {
            System.out.print("Enter discount code (or press Enter to skip): ");
            String code = scanner.nextLine();
            if (code.isEmpty()) code = null;

            Order newOrder = orderService.placeOrder(loggedInUser.getId(), r.getId(), cart, code);
            System.out.println("Order placed successfully! Total: $" + newOrder.getTotalPrice());
        }
    }

    private void showRestaurantOwnerMenu() {
        System.out.println("\n--- Restaurant Owner Dashboard ---");
        System.out.println("1. Register a new Restaurant");
        System.out.println("2. Toggle Restaurant Open/Close Status");
        System.out.println("3. Add Menu Item");
        System.out.println("4. Update Order Status");
        System.out.println("5. Logout");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine();
        // Core implementation shown. Full error handling omitted for brevity to keep it simple.
        if (choice.equals("1")) {
            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Area: "); String area = scanner.nextLine();
            System.out.print("Open Time (e.g. 09:00): "); String open = scanner.nextLine();
            System.out.print("Close Time: "); String close = scanner.nextLine();
            
            restaurantService.registerRestaurant(name, loggedInUser.getId(), area, open, close);
            System.out.println("Restaurant registered!");
        } else if (choice.equals("3")) {
            System.out.print("Enter your exact Restaurant ID: ");
            String rId = scanner.nextLine();
            System.out.print("Item name: "); String name = scanner.nextLine();
            System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine());
            System.out.print("Quantity available: "); int qty = Integer.parseInt(scanner.nextLine());
            System.out.print("Customizations (e.g. Add-on extra cheese): "); String cust = scanner.nextLine();
            
            restaurantService.addMenuItem(rId, name, price, qty, cust);
            System.out.println("Menu item added!");
        } else if (choice.equals("4")) {
            System.out.print("Enter Order ID to update: ");
            String oId = scanner.nextLine();
            System.out.print("New Status (PREPARING, DELIVERING, COMPLETED): ");
            String newStatus = scanner.nextLine();
            orderService.updateOrderStatus(oId, newStatus);
            System.out.println("Order status updated!");
        } else if (choice.equals("5")) {
            loggedInUser = null;
        } else {
            System.out.println("Not implemented in this mockup flow or invalid choice.");
        }
    }
}
