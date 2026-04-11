package com.foodapp.model;

public class Restaurant {
    private String id;
    private String name;
    private String ownerId;
    private String area;
    private String openingTime;
    private String closingTime;
    private boolean open;

    public Restaurant() {}

    public Restaurant(String id, String name, String ownerId, String area, String openingTime, String closingTime, boolean open) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.area = area;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.open = open;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getOpeningTime() { return openingTime; }
    public void setOpeningTime(String openingTime) { this.openingTime = openingTime; }

    public String getClosingTime() { return closingTime; }
    public void setClosingTime(String closingTime) { this.closingTime = closingTime; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public String toCsv() {
        return String.join(";", id, name, ownerId, area, openingTime, closingTime, String.valueOf(open));
    }

    public static Restaurant fromCsv(String csv) {
        String[] parts = csv.split(";", -1);
        if (parts.length >= 7) {
            return new Restaurant(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Boolean.parseBoolean(parts[6]));
        }
        return null;
    }
}
