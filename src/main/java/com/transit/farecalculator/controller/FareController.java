package com.transit.farecalculator.controller;

import com.transit.farecalculator.model.DashboardStats;
import com.transit.farecalculator.model.FareHistory;
import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;
import com.transit.farecalculator.service.FareCalculationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing fare calculation, dashboard statistics,
 * and fare calculation history endpoints.
 */
@RestController
@RequestMapping("/api/fare")
@CrossOrigin(origins = "*")
public class FareController {

    private final FareCalculationService fareCalculationService;

    @Autowired
    public FareController(FareCalculationService fareCalculationService) {
        this.fareCalculationService = fareCalculationService;
    }

    /**
     * Calculate fare using selected pricing strategy and persist record.
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateFare(@Valid @RequestBody FareRequest request) {
        try {
            FareResponse response = fareCalculationService.calculateAndSaveFare(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate fare: " + ex.getMessage()));
        }
    }

    /**
     * Retrieve all previous fare calculations from MySQL database.
     */
    @GetMapping("/history")
    public ResponseEntity<List<FareHistory>> getFareHistory() {
        return ResponseEntity.ok(fareCalculationService.getFareHistory());
    }

    /**
     * Delete an individual history item by ID.
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteFareHistory(@PathVariable Long id) {
        boolean deleted = fareCalculationService.deleteFareHistory(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "History record deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Record not found"));
        }
    }

    /**
     * Clear all history records.
     */
    @DeleteMapping("/history")
    public ResponseEntity<?> clearAllHistory() {
        fareCalculationService.clearAllHistory();
        return ResponseEntity.ok(Map.of("message", "All history records cleared"));
    }

    /**
     * Retrieve dashboard stats for the 4 top summary cards.
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(fareCalculationService.getDashboardStats());
    }
}
