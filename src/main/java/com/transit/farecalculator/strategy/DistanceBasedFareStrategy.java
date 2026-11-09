package com.transit.farecalculator.strategy;

import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;
import org.springframework.stereotype.Component;

/**
 * Strategy 1: Distance-Based Fare Calculation.
 * Calculates fare primarily from distance traveled using tiered per-kilometer rates.
 */
@Component("DISTANCE")
public class DistanceBasedFareStrategy implements FareCalculationStrategy {

    @Override
    public String getStrategyName() {
        return "Distance-Based Fare Strategy";
    }

    @Override
    public FareResponse calculateFare(FareRequest request, int sourceZone, int destZone) {
        boolean isMetro = "METRO".equalsIgnoreCase(request.getTransportType());

        // Base fare
        double baseFare = isMetro ? 20.00 : 15.00;

        // Rate per kilometer
        double ratePerKm = isMetro ? 2.00 : 1.20;
        double distanceCharge = round(request.getDistanceKm() * ratePerKm);

        // Small standard peak adjustment if peak travel time is chosen (10%)
        double dynamicAdjustment = 0.0;
        if ("Peak".equalsIgnoreCase(request.getTravelTime())) {
            dynamicAdjustment = round((baseFare + distanceCharge) * 0.10);
        }

        double subtotal = baseFare + distanceCharge + dynamicAdjustment;

        // Passenger discount
        double discountRate = getDiscountRate(request.getPassengerType());
        double discountAmount = round(subtotal * discountRate);

        double finalFare = round(subtotal - discountAmount);

        String breakdown = String.format(
            "Distance-based formula: Base (₹%.2f) + (%.1f km × ₹%.2f/km = ₹%.2f) + Dynamic Peak Adjustment (₹%.2f) - Discount (₹%.2f [%.0f%%]) = ₹%.2f",
            baseFare, request.getDistanceKm(), ratePerKm, distanceCharge, dynamicAdjustment, discountAmount, discountRate * 100, finalFare
        );

        return new FareResponse(baseFare, distanceCharge, dynamicAdjustment, discountAmount, finalFare, getStrategyName(), breakdown);
    }

    private double getDiscountRate(String passengerType) {
        if ("Student".equalsIgnoreCase(passengerType)) {
            return 0.25; // 25% discount
        } else if ("Senior Citizen".equalsIgnoreCase(passengerType)) {
            return 0.40; // 40% discount
        }
        return 0.0; // Normal fare
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
