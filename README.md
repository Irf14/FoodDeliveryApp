# Core Java Food Delivery Application

A pure Core Java project built for university coursework. This application demonstrates essential Object-Oriented Principles (OOP), clean separation of concerns, file-based data tracking, and a command-line interface.

## 🚀 Features

### Customer Features
- Register and login as a Customer.
- Search for open restaurants by area.
- Browse restaurant menus.
- Place orders with multiple items and quantity tracking.
- Apply discount codes (e.g., `SAVE10`).
- View and track the status of current orders.

### Restaurant Owner Features
- Register a new restaurant.
- Toggle restaurant open/close status manually.
- Add new menu items with custom pricing and availability.
- Update customer order statuses (e.g., PREPARING -> DELIVERING).

## 🛠️ Architecture & Design Principles

This project strict adheres to standard Java development best practices:

- **Modularity:** Strictly maintains "One class per functionality".
- **Encapsulation:** Utilizing private fields and getters/setters in the `/model` package.
- **Single Responsibility:** Separating data writing/reading to a dedicated `/storage` layer, disconnected from business logic `/service` components.
- **WSDL Web Service API:** Features a built-in JAX-WS implementation. By annotating the `RestaurantWebServiceImpl` with `@WebService`, we automatically expose queries over SOAP protocols when the main application starts.

## 💾 Data Storage
The application utilizes an extremely simple **File Storage** mechanism. Data is persisted automatically into `.txt` and `.csv` files stored under the `/data` directory. No external databases (like MySQL) are required, resulting in minimal friction to get the codebase running.

## 💻 How to Run
1. Ensure you have **Java (JDK 17+)** installed on your system.
2. Clone this repository (or open it your favorite IDE like Eclipse or IntelliJ IDEA).
3. The entry point of the application is:
   `src/main/java/com/foodapp/Main.java`
4. Run `Main.java`! The application will start the CLI loop in your Terminal, and the WSDL API will be published to `http://localhost:8080/ws/restaurants?wsdl`. 
