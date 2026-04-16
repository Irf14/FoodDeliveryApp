package com.foodapp.api;

import com.foodapp.model.*;
import com.foodapp.service.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * Lightweight REST API Server using Java's built-in HttpServer.
 * Bridges the Savoria frontend UI to the Java backend services.
 * All data reads/writes go through FileStorage → txt files.
 */
public class ApiServer {

    private final UserService userService = new UserService();
    private final RestaurantService restaurantService = new RestaurantService();
    private final SearchService searchService = new SearchService(restaurantService);
    private final OrderService orderService = new OrderService();

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ---- API Endpoints ----
        server.createContext("/api/login", this::handleLogin);
        server.createContext("/api/register", this::handleRegister);
        server.createContext("/api/restaurants", this::handleRestaurants);
        server.createContext("/api/menu", this::handleMenu);
        server.createContext("/api/search/food", this::handleSearchFood);
        server.createContext("/api/search/area", this::handleSearchArea);
        server.createContext("/api/orders", this::handleOrders);
        server.createContext("/api/order/place", this::handlePlaceOrder);
        server.createContext("/api/order/status", this::handleOrderStatus);
        server.createContext("/api/restaurant/register", this::handleRegisterRestaurant);
        server.createContext("/api/restaurant/toggle", this::handleToggleRestaurant);
        server.createContext("/api/menu/add", this::handleAddMenuItem);

        // ---- Static Files (serves the webapp) ----
        server.createContext("/", this::handleStaticFiles);

        server.setExecutor(null);
        server.start();
        System.out.println("[Savoria UI] Web app running at: http://localhost:" + port);
        System.out.println("[Savoria UI] Open this URL in your browser to use the app!");
    }

    // ====== HELPERS ======

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes("UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2) {
                try { map.put(kv[0], URLDecoder.decode(kv[1], "UTF-8")); }
                catch (Exception e) { map.put(kv[0], kv[1]); }
            }
        }
        return map;
    }

    private Map<String, String> parseBody(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), "UTF-8");
        return parseQuery(body);
    }

    // ====== USER JSON ======
    private String userToJson(User u) {
        if (u == null) return "null";
        return "{\"id\":\"" + esc(u.getId()) + "\",\"username\":\"" + esc(u.getUsername())
                + "\",\"role\":\"" + esc(u.getRole()) + "\",\"address\":\"" + esc(u.getAddress()) + "\"}";
    }

    // ====== RESTAURANT JSON ======
    private String restaurantToJson(Restaurant r) {
        if (r == null) return "null";
        return "{\"id\":\"" + esc(r.getId()) + "\",\"name\":\"" + esc(r.getName())
                + "\",\"ownerId\":\"" + esc(r.getOwnerId()) + "\",\"area\":\"" + esc(r.getArea())
                + "\",\"openingTime\":\"" + esc(r.getOpeningTime()) + "\",\"closingTime\":\"" + esc(r.getClosingTime())
                + "\",\"open\":" + r.isOpen() + "}";
    }

    private String restaurantsToJson(List<Restaurant> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(restaurantToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    // ====== MENUITEM JSON ======
    private String menuItemToJson(MenuItem m) {
        if (m == null) return "null";
        return "{\"id\":\"" + esc(m.getId()) + "\",\"restaurantId\":\"" + esc(m.getRestaurantId())
                + "\",\"name\":\"" + esc(m.getName()) + "\",\"price\":" + m.getPrice()
                + ",\"available\":" + m.isAvailable() + ",\"quantity\":" + m.getQuantity()
                + ",\"customizations\":\"" + esc(m.getCustomizations()) + "\"}";
    }

    private String menuItemsToJson(List<MenuItem> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(menuItemToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    // ====== ORDER JSON ======
    private String orderToJson(Order o) {
        if (o == null) return "null";
        StringBuilder items = new StringBuilder("[");
        if (o.getItems() != null) {
            for (int i = 0; i < o.getItems().size(); i++) {
                if (i > 0) items.append(",");
                OrderItem oi = o.getItems().get(i);
                items.append("{\"menuItemId\":\"" + esc(oi.getMenuItemId()) + "\",\"name\":\"" + esc(oi.getName())
                        + "\",\"quantity\":" + oi.getQuantity() + ",\"price\":" + oi.getPrice() + "}");
            }
        }
        items.append("]");
        String dc = o.getDiscountCode() == null ? "null" : "\"" + esc(o.getDiscountCode()) + "\"";
        return "{\"id\":\"" + esc(o.getId()) + "\",\"customerId\":\"" + esc(o.getCustomerId())
                + "\",\"restaurantId\":\"" + esc(o.getRestaurantId()) + "\",\"status\":\"" + esc(o.getStatus())
                + "\",\"totalPrice\":" + o.getTotalPrice() + ",\"discountCode\":" + dc
                + ",\"items\":" + items + "}";
    }

    private String ordersToJson(List<Order> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(orderToJson(list.get(i)));
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // ====== HANDLERS ======

    private void handleLogin(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        User u = userService.login(p.get("username"), p.get("password"));
        if (u != null) {
            sendJson(ex, 200, userToJson(u));
        } else {
            sendJson(ex, 401, "{\"error\":\"Invalid credentials\"}");
        }
    }

    private void handleRegister(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        String role = p.getOrDefault("role", "CUSTOMER");
        User u;
        if ("RESTAURANT_OWNER".equals(role)) {
            u = userService.registerOwner(p.get("username"), p.get("password"), p.get("address"));
        } else {
            u = userService.registerCustomer(p.get("username"), p.get("password"), p.get("address"));
        }
        if (u != null) {
            sendJson(ex, 200, userToJson(u));
        } else {
            sendJson(ex, 400, "{\"error\":\"Username already taken\"}");
        }
    }

    private void handleRestaurants(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
        String ownerId = q.get("ownerId");
        List<Restaurant> list;
        if (ownerId != null) {
            list = new ArrayList<>();
            for (Restaurant r : restaurantService.getAllRestaurants()) {
                if (r.getOwnerId().equals(ownerId)) list.add(r);
            }
        } else {
            list = restaurantService.getAllRestaurants();
        }
        sendJson(ex, 200, restaurantsToJson(list));
    }

    private void handleMenu(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
        String restId = q.get("restaurantId");
        List<MenuItem> items;
        if (restId != null) {
            items = restaurantService.getMenuForRestaurant(restId);
        } else {
            items = restaurantService.getAllMenuItems();
        }
        sendJson(ex, 200, menuItemsToJson(items));
    }

    private void handleSearchFood(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
        String query = q.getOrDefault("name", "");
        // Search both name AND customizations
        List<MenuItem> allItems = restaurantService.getAllMenuItems();
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : allItems) {
            if (item.isAvailable() && (item.getName().toLowerCase().contains(query.toLowerCase())
                    || item.getCustomizations().toLowerCase().contains(query.toLowerCase()))) {
                result.add(item);
            }
        }
        sendJson(ex, 200, menuItemsToJson(result));
    }

    private void handleSearchArea(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
        List<Restaurant> result = searchService.findRestaurantsByArea(q.getOrDefault("area", ""));
        sendJson(ex, 200, restaurantsToJson(result));
    }

    private void handleOrders(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
        String custId = q.get("customerId");
        List<Order> list = (custId != null) ? orderService.getCustomerOrders(custId) : new ArrayList<>();
        sendJson(ex, 200, ordersToJson(list));
    }

    private void handlePlaceOrder(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        String customerId = p.get("customerId");
        String restaurantId = p.get("restaurantId");
        String discountCode = p.get("discountCode");
        String itemsRaw = p.getOrDefault("items", "");

        List<OrderItem> items = new ArrayList<>();
        if (!itemsRaw.isEmpty()) {
            for (String part : itemsRaw.split("\\|")) {
                String[] fields = part.split(":", 4);
                if (fields.length >= 4) {
                    items.add(new OrderItem(fields[0], fields[1],
                            Integer.parseInt(fields[2]), Double.parseDouble(fields[3])));
                }
            }
        }

        Order order = orderService.placeOrder(customerId, restaurantId, items, discountCode);
        sendJson(ex, 200, orderToJson(order));
    }

    private void handleOrderStatus(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        orderService.updateOrderStatus(p.get("orderId"), p.get("status"));
        sendJson(ex, 200, "{\"success\":true}");
    }

    private void handleRegisterRestaurant(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        Restaurant r = restaurantService.registerRestaurant(
                p.get("name"), p.get("ownerId"), p.get("area"), p.get("openTime"), p.get("closeTime"));
        if (r != null) {
            sendJson(ex, 200, restaurantToJson(r));
        } else {
            sendJson(ex, 400, "{\"error\":\"Restaurant already exists with this name\"}");
        }
    }

    private void handleToggleRestaurant(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        restaurantService.toggleRestaurantStatus(p.get("restaurantId"), Boolean.parseBoolean(p.get("open")));
        sendJson(ex, 200, "{\"success\":true}");
    }

    private void handleAddMenuItem(HttpExchange ex) throws IOException {
        if (handleCors(ex)) return;
        Map<String, String> p = parseBody(ex);
        restaurantService.addMenuItem(
                p.get("restaurantId"), p.get("name"),
                Double.parseDouble(p.getOrDefault("price", "0")),
                Integer.parseInt(p.getOrDefault("quantity", "0")),
                p.getOrDefault("customizations", "Regular"));
        sendJson(ex, 200, "{\"success\":true}");
    }

    private boolean handleCors(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    // ====== STATIC FILE SERVER ======

    private void handleStaticFiles(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        // Resolve relative to project's webapp directory
        File file = new File("src/main/webapp" + path);
        if (!file.exists()) {
            String resp = "404 Not Found";
            ex.sendResponseHeaders(404, resp.length());
            ex.getResponseBody().write(resp.getBytes());
            ex.getResponseBody().close();
            return;
        }

        String mime = "text/plain";
        if (path.endsWith(".html")) mime = "text/html";
        else if (path.endsWith(".css")) mime = "text/css";
        else if (path.endsWith(".js")) mime = "application/javascript";
        else if (path.endsWith(".png")) mime = "image/png";
        else if (path.endsWith(".jpg")) mime = "image/jpeg";
        else if (path.endsWith(".svg")) mime = "image/svg+xml";

        ex.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
        byte[] bytes = Files.readAllBytes(file.toPath());
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }
}
