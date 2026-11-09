package com.transit.farecalculator.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for incoming fare calculation requests.
 */
public class FareRequest {

    @NotBlank(message = "Transport type is required (METRO or BUS)")
    private String transportType;

    @NotBlank(message = "Source station is required")
    private String sourceStation;

    @NotBlank(message = "Destination station is required")
    private String destinationStation;

    @NotNull(message = "Distance in km is required")
    @DecimalMin(value = "0.1", message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotBlank(message = "Passenger type is required (Normal, Student, Senior Citizen)")
    private String passengerType;

    @NotBlank(message = "Travel time is required (Peak, Off-Peak)")
    private String travelTime;

    private String strategyType; // "DISTANCE", "ZONE", "PEAK_OFF_PEAK"

    public FareRequest() {
    }

    public FareRequest(String transportType, String sourceStation, String destinationStation, 
                       Double distanceKm, String passengerType, String travelTime, String strategyType) {
        this.transportType = transportType;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.distanceKm = distanceKm;
        this.passengerType = passengerType;
        this.travelTime = travelTime;
        this.strategyType = strategyType;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(String passengerType) {
        this.passengerType = passengerType;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }
}
