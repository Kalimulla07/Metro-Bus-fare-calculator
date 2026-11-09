package com.transit.farecalculator.model;

/**
 * Data Transfer Object representing the calculated fare response.
 */
public class FareResponse {

    private Double baseFare;
    private Double distanceCharge;
    private Double dynamicAdjustment;
    private Double discountAmount;
    private Double finalFare;
    private String strategyUsed;
    private String breakdownDescription;
    private Long historyId;
    private String calculationTimestamp;

    public FareResponse() {
    }

    public FareResponse(Double baseFare, Double distanceCharge, Double dynamicAdjustment, 
                        Double discountAmount, Double finalFare, String strategyUsed, 
                        String breakdownDescription) {
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.dynamicAdjustment = dynamicAdjustment;
        this.discountAmount = discountAmount;
        this.finalFare = finalFare;
        this.strategyUsed = strategyUsed;
        this.breakdownDescription = breakdownDescription;
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

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    public String getBreakdownDescription() {
        return breakdownDescription;
    }

    public void setBreakdownDescription(String breakdownDescription) {
        this.breakdownDescription = breakdownDescription;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getCalculationTimestamp() {
        return calculationTimestamp;
    }

    public void setCalculationTimestamp(String calculationTimestamp) {
        this.calculationTimestamp = calculationTimestamp;
    }
}
