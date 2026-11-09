package com.transit.farecalculator.model;

import java.util.List;

/**
 * Model representing an existing metro or bus route line.
 */
public class RouteInfo {
    private String routeId;
    private String routeName;
    private String transportType; // METRO or BUS
    private String color; // Hex color code for UI badge
    private double totalDistanceKm;
    private List<String> stationNames;

    public RouteInfo() {
    }

    public RouteInfo(String routeId, String routeName, String transportType, String color, 
                     double totalDistanceKm, List<String> stationNames) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.transportType = transportType;
        this.color = color;
        this.totalDistanceKm = totalDistanceKm;
        this.stationNames = stationNames;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public List<String> getStationNames() {
        return stationNames;
    }

    public void setStationNames(List<String> stationNames) {
        this.stationNames = stationNames;
    }
}
