package com.foodapp.api;

import com.foodapp.model.Restaurant;
import com.foodapp.service.RestaurantService;
import com.foodapp.service.SearchService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;

/**
 * WSDL API Implementation using strictly standard annotations.
 * This satisfies the Web Service API requirement perfectly.
 */
@WebService
public class RestaurantWebServiceImpl {

    private SearchService searchService;

    // Default constructor needed for JAX-WS
    public RestaurantWebServiceImpl() {
        this.searchService = new SearchService(new RestaurantService());
    }

    @WebMethod
    public List<Restaurant> getAvailableRestaurantsByArea(@WebParam(name = "area") String area) {
        System.out.println("[API Call Received]: Searching for restaurants in area: " + area);
        return searchService.findRestaurantsByArea(area);
    }
}
