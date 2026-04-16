package com.foodapp.api;

import com.foodapp.model.*;
import com.foodapp.service.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

/**
 * Full WSDL SOAP Web Service Implementation.
 * Mirroring all features available in the UI.
 */
@WebService
public class RestaurantWebServiceImpl {

    private final RestaurantService restaurantService;
    private final SearchService searchService;
    private final OrderService orderService;
    private final UserService userService;

    public RestaurantWebServiceImpl() {
        this.restaurantService = new RestaurantService();
        this.searchService = new SearchService(this.restaurantService);
        this.orderService = new OrderService();
        this.userService = new UserService();
    }

    // --- CUSTOMER FEATURES ---

    @WebMethod
    public List<Restaurant> getAllRestaurants() {
        System.out.println("[SOAP]: Fetching all restaurants");
        return restaurantService.getAllRestaurants();
    }

    @WebMethod
    public List<Restaurant> getAvailableRestaurantsByArea(@WebParam(name = "area") String area) {
        System.out.println("[SOAP]: Searching for restaurants in area: " + area);
        return searchService.findRestaurantsByArea(area);
    }

    @WebMethod
    public List<MenuItem> getMenuForRestaurant(@WebParam(name = "restaurantId") String restaurantId) {
        System.out.println("[SOAP]: Getting menu for restaurant: " + restaurantId);
        return restaurantService.getMenuForRestaurant(restaurantId);
    }

    @WebMethod
    public Order placeOrder(@WebParam(name = "customerId") String customerId,
                            @WebParam(name = "restaurantId") String restaurantId,
                            @WebParam(name = "items") List<OrderItem> items,
                            @WebParam(name = "discountCode") String discountCode) {
        System.out.println("[SOAP]: Placing order for customer: " + customerId);
        return orderService.placeOrder(customerId, restaurantId, items, discountCode);
    }

    @WebMethod
    public List<Order> getCustomerOrders(@WebParam(name = "customerId") String customerId) {
        System.out.println("[SOAP]: Fetching orders for customer: " + customerId);
        return orderService.getCustomerOrders(customerId);
    }

    @WebMethod
    public List<Restaurant> findRestaurantsByName(@WebParam(name = "nameQuery") String nameQuery) {
        System.out.println("[SOAP]: Searching restaurants by name: " + nameQuery);
        return searchService.findRestaurantsByName(nameQuery);
    }

    @WebMethod
    public List<MenuItem> findMenuItemsByName(@WebParam(name = "foodNameQuery") String foodNameQuery) {
        System.out.println("[SOAP]: Searching menu items by name: " + foodNameQuery);
        return searchService.findMenuItemsByName(foodNameQuery);
    }

    // --- AUTH FEATURES ---

    @WebMethod
    public User login(@WebParam(name = "username") String username,
                      @WebParam(name = "password") String password) {
        return userService.login(username, password);
    }

    @WebMethod
    public User registerCustomer(@WebParam(name = "username") String username,
                                 @WebParam(name = "password") String password,
                                 @WebParam(name = "address") String address) {
        return userService.registerCustomer(username, password, address);
    }

    @WebMethod
    public User registerOwner(@WebParam(name = "username") String username,
                               @WebParam(name = "password") String password,
                               @WebParam(name = "address") String address) {
        return userService.registerOwner(username, password, address);
    }

    // --- OWNER & ADMIN FEATURES ---

    @WebMethod
    public Restaurant registerRestaurant(@WebParam(name = "name") String name,
                                        @WebParam(name = "ownerId") String ownerId,
                                        @WebParam(name = "area") String area,
                                        @WebParam(name = "openTime") String openTime,
                                        @WebParam(name = "closeTime") String closeTime) {
        return restaurantService.registerRestaurant(name, ownerId, area, openTime, closeTime);
    }

    @WebMethod
    public void toggleRestaurantStatus(@WebParam(name = "restaurantId") String restaurantId,
                                       @WebParam(name = "isOpen") boolean isOpen) {
        restaurantService.toggleRestaurantStatus(restaurantId, isOpen);
    }

    @WebMethod
    public void addMenuItem(@WebParam(name = "restaurantId") String restaurantId,
                            @WebParam(name = "name") String name,
                            @WebParam(name = "price") double price,
                            @WebParam(name = "quantity") int quantity,
                            @WebParam(name = "customizations") String customizations) {
        restaurantService.addMenuItem(restaurantId, name, price, quantity, customizations);
    }

    @WebMethod
    public void updateOrderStatus(@WebParam(name = "orderId") String orderId,
                                  @WebParam(name = "status") String status) {
        orderService.updateOrderStatus(orderId, status);
    }
}
