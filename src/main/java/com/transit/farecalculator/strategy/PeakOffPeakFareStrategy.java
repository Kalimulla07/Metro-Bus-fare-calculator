package com.transit.farecalculator.strategy;

import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;
import org.springframework.stereotype.Component;

/**
 * Strategy 3: Peak vs Off-Peak Dynamic Fare Strategy.
 * Dynamically adjusts fares based on transit demand curves:
 * Peak hours attract a 30% demand-management surcharge.
 * Off-Peak hours receive a 10% incentive discount.
 */
@Component("PEAK_OFF_PEAK")
public class PeakOffPeakFareStrategy implements FareCalculationStrategy {

    @Override
    public String getStrategyName() {
        return "Peak/Off-Peak Dynamic Fare Strategy";
    }

    @Override
    public FareResponse calculateFare(FareRequest request, int sourceZone, int destZone) {
        boolean isMetro = "METRO".equalsIgnoreCase(request.getTransportType());

        double baseFare = isMetro ? 20.00 : 14.00;
        double ratePerKm = isMetro ? 1.80 : 1.10;
        double distanceCharge = round(request.getDistanceKm() * ratePerKm);

        double standardSubtotal = baseFare + distanceCharge;

        // Dynamic Pricing Factor
        double dynamicAdjustment;
        boolean isPeak = "Peak".equalsIgnoreCase(request.getTravelTime());

        if (isPeak) {
            // +30% Surge Pricing during peak hours to balance ridership
            dynamicAdjustment = round(standardSubtotal * 0.30);
        } else {
            // -10% Off-Peak Saver Discount to incentivize travel during quiet hours
            dynamicAdjustment = round(-1.0 * (standardSubtotal * 0.10));
        }

        double subtotalAfterDynamic = Math.max(10.0, standardSubtotal + dynamicAdjustment);

        // Passenger discount
        double discountRate = getDiscountRate(request.getPassengerType());
        double discountAmount = round(subtotalAfterDynamic * discountRate);

        double finalFare = round(subtotalAfterDynamic - discountAmount);

        String breakdown = String.format(
            "Dynamic Peak/Off-Peak: Base (₹%.2f) + Distance (₹%.2f) %s Dynamic Pricing Adjustment (₹%.2f) - Passenger Discount (₹%.2f [%.0f%%]) = Final Fare ₹%.2f",
            baseFare, distanceCharge, 
            (isPeak ? "+ Peak Surge" : "- Off-Peak Saver"), 
            Math.abs(dynamicAdjustment), discountAmount, discountRate * 100, finalFare
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
