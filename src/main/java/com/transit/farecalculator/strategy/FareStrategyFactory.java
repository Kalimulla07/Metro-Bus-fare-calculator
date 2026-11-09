package com.transit.farecalculator.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Strategy Factory to resolve and supply the appropriate FareCalculationStrategy
 * based on user selection or strategy key.
 */
@Component
public class FareStrategyFactory {

    private final Map<String, FareCalculationStrategy> strategyMap;

    @Autowired
    public FareStrategyFactory(Map<String, FareCalculationStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * Resolves strategy by key: "DISTANCE", "ZONE", "PEAK_OFF_PEAK".
     * Falls back to "DISTANCE" strategy if null or unrecognized.
     */
    public FareCalculationStrategy getStrategy(String strategyType) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            return strategyMap.getOrDefault("DISTANCE", new DistanceBasedFareStrategy());
        }

        String normalizedKey = strategyType.trim().toUpperCase();
        FareCalculationStrategy strategy = strategyMap.get(normalizedKey);

        if (strategy == null) {
            // Default fallback
            return strategyMap.getOrDefault("DISTANCE", new DistanceBasedFareStrategy());
        }

        return strategy;
    }
}
