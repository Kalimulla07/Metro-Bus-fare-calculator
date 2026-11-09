package com.transit.farecalculator.model;

import java.util.List;

/**
 * Result DTO of Dijkstra's algorithm containing the shortest path and details.
 */
public class RoutePathResponse {

    private String source;
    private String destination;
    private String transportType;
    private List<String> route;
    private double totalDistance;
    private double estimatedFare;
    private int estimatedMinutes;
    private int totalStops;

    public RoutePathResponse() {
    }

    public RoutePathResponse(String source, String destination, String transportType, 
                             List<String> route, double totalDistance, double estimatedFare, 
                             int estimatedMinutes, int totalStops) {
        this.source = source;
        this.destination = destination;
        this.transportType = transportType;
        this.route = route;
        this.totalDistance = totalDistance;
        this.estimatedFare = estimatedFare;
        this.estimatedMinutes = estimatedMinutes;
        this.totalStops = totalStops;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public List<String> getRoute() {
        return route;
    }

    public void setRoute(List<String> route) {
        this.route = route;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public int getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(int totalStops) {
        this.totalStops = totalStops;
    }
}
