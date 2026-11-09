package com.transit.farecalculator.strategy;

import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;
import org.springframework.stereotype.Component;

/**
 * Strategy 2: Zone-Based Fare Calculation.
 * Calculates transit fare based on zones crossed between source and destination stations.
 */
@Component("ZONE")
public class ZoneBasedFareStrategy implements FareCalculationStrategy {

    @Override
    public String getStrategyName() {
        return "Zone-Based Fare Strategy";
    }

    @Override
    public FareResponse calculateFare(FareRequest request, int sourceZone, int destZone) {
        boolean isMetro = "METRO".equalsIgnoreCase(request.getTransportType());

        // Base flat fare for single zone travel
        double baseFare = isMetro ? 25.00 : 18.00;

        // Zone transition charge
        int zonesCrossed = Math.abs(sourceZone - destZone);
        double chargePerZoneCrossed = isMetro ? 15.00 : 10.00;
        double zoneCharge = zonesCrossed * chargePerZoneCrossed;

        // Minor distance supplement (₹0.50/km for metro, ₹0.30/km for bus)
        double distanceRate = isMetro ? 0.50 : 0.30;
        double distanceCharge = round(zoneCharge + (request.getDistanceKm() * distanceRate));

        // Travel time adjustment
        double dynamicAdjustment = 0.0;
        if ("Peak".equalsIgnoreCase(request.getTravelTime())) {
            dynamicAdjustment = round((baseFare + distanceCharge) * 0.15);
        }

        double subtotal = baseFare + distanceCharge + dynamicAdjustment;

        // Passenger discount
        double discountRate = getDiscountRate(request.getPassengerType());
        double discountAmount = round(subtotal * discountRate);

        double finalFare = round(subtotal - discountAmount);

        String breakdown = String.format(
            "Zone-based formula: Base (₹%.2f) + Zone crossing penalty (%d zones crossed = ₹%.2f) + Distance supplement (₹%.2f) + Dynamic Adjustment (₹%.2f) - Discount (₹%.2f) = ₹%.2f",
            baseFare, zonesCrossed, zoneCharge, round(request.getDistanceKm() * distanceRate), dynamicAdjustment, discountAmount, finalFare
        );

        return new FareResponse(baseFare, distanceCharge, dynamicAdjustment, discountAmount, finalFare, getStrategyName(), breakdown);
    }

    private double getDiscountRate(String passengerType) {
        if ("Student".equalsIgnoreCase(passengerType)) {
            return 0.25;
        } else if ("Senior Citizen".equalsIgnoreCase(passengerType)) {
            return 0.40;
        }
        return 0.0;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
