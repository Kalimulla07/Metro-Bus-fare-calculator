package com.transit.farecalculator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing stored fare calculation history in MySQL database.
 */
@Entity
@Table(name = "fare_history")
public class FareHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Column(name = "transport_type", nullable = false, length = 20)
    private String transportType;

    @Column(name = "source_station", nullable = false, length = 100)
    private String sourceStation;

    @Column(name = "destination_station", nullable = false, length = 100)
    private String destinationStation;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "passenger_type", nullable = false, length = 30)
    private String passengerType;

    @Column(name = "travel_time", nullable = false, length = 20)
    private String travelTime;

    @Column(name = "strategy_used", nullable = false, length = 50)
    private String strategyUsed;

    @Column(name = "base_fare", nullable = false)
    private Double baseFare;

    @Column(name = "distance_charge", nullable = false)
    private Double distanceCharge;

    @Column(name = "dynamic_adjustment", nullable = false)
    private Double dynamicAdjustment;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "final_fare", nullable = false)
    private Double finalFare;

    public FareHistory() {
        this.calculationDate = LocalDateTime.now();
    }

    public FareHistory(String transportType, String sourceStation, String destinationStation, 
                       Double distanceKm, String passengerType, String travelTime, 
                       String strategyUsed, Double baseFare, Double distanceCharge, 
                       Double dynamicAdjustment, Double discountAmount, Double finalFare) {
        this.calculationDate = LocalDateTime.now();
        this.transportType = transportType;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.distanceKm = distanceKm;
        this.passengerType = passengerType;
        this.travelTime = travelTime;
        this.strategyUsed = strategyUsed;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.dynamicAdjustment = dynamicAdjustment;
        this.discountAmount = discountAmount;
        this.finalFare = finalFare;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
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

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(Double distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public Double getDynamicAdjustment() {
        return dynamicAdjustment;
    }

    public void setDynamicAdjustment(Double dynamicAdjustment) {
        this.dynamicAdjustment = dynamicAdjustment;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(Double finalFare) {
        this.finalFare = finalFare;
    }
}
