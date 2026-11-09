package com.transit.farecalculator.strategy;

import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;

/**
 * Strategy Interface defining the contract for various transit fare calculation algorithms.
 * Implements the Strategy Design Pattern.
 */
public interface FareCalculationStrategy {

    /**
     * Unique name identifying the strategy.
     */
    String getStrategyName();

    /**
     * Calculates the fare based on request parameters and station zone information.
     *
     * @param request    The fare request details (distance, transport type, passenger type, travel time)
     * @param sourceZone The numeric zone of the source station (1, 2, or 3)
     * @param destZone   The numeric zone of the destination station (1, 2, or 3)
     * @return Complete calculated FareResponse breakdown
     */
    FareResponse calculateFare(FareRequest request, int sourceZone, int destZone);
}
