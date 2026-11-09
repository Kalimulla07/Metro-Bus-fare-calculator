package com.transit.farecalculator.model;

/**
 * Model representing a transit station with zone and transport compatibility.
 */
public class Station {
    private String id;
    private String name;
    private int zone; // Zone 1: Downtown/Central, Zone 2: Suburbs, Zone 3: Outer
    private String line;
    private String transportType; // METRO, BUS, or BOTH

    public Station() {
    }

    public Station(String id, String name, int zone, String line, String transportType) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.line = line;
        this.transportType = transportType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }
}
