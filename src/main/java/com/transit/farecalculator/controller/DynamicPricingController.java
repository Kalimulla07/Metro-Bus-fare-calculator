package com.transit.farecalculator.controller;

import com.transit.farecalculator.model.DemandData;
import com.transit.farecalculator.service.DynamicPricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing passenger demand trends and dynamic pricing analytics.
 */
@RestController
@RequestMapping("/api/dynamic-pricing")
@CrossOrigin(origins = "*")
public class DynamicPricingController {

    private final DynamicPricingService dynamicPricingService;

    @Autowired
    public DynamicPricingController(DynamicPricingService dynamicPricingService) {
        this.dynamicPricingService = dynamicPricingService;
    }

    /**
     * Retrieve 24-hour demand curve, peak thresholds, and pricing recommendations.
     */
    @GetMapping("/demand-data")
    public ResponseEntity<DemandData> getDemandData() {
        return ResponseEntity.ok(dynamicPricingService.getDemandAnalytics());
    }
}
