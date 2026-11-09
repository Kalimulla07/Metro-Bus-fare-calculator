package com.transit.farecalculator.controller;

import com.transit.farecalculator.model.RouteInfo;
import com.transit.farecalculator.model.RoutePathResponse;
import com.transit.farecalculator.model.Station;
import com.transit.farecalculator.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing transit network topology, routes,
 * and Dijkstra's shortest path routing algorithm.
 */
@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Retrieve all available stations with zones.
     */
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(routeService.getAllStations());
    }

    /**
     * Retrieve all metro lines and bus routes.
     */
    @GetMapping("/all")
    public ResponseEntity<List<RouteInfo>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    /**
     * Calculate shortest route between two stations using Dijkstra's Algorithm (GET query parameters).
     */
    @GetMapping("/shortest-path")
    public ResponseEntity<?> getShortestPath(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false, defaultValue = "METRO") String transportType) {
        try {
            RoutePathResponse response = routeService.findShortestPath(source, destination, transportType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Calculate shortest route via POST body.
     */
    @PostMapping("/shortest-path")
    public ResponseEntity<?> computeShortestPath(@RequestBody Map<String, String> payload) {
        String source = payload.get("source");
        String destination = payload.get("destination");
        String transportType = payload.getOrDefault("transportType", "METRO");

        if (source == null || destination == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Source and destination are required"));
        }

        try {
            RoutePathResponse response = routeService.findShortestPath(source, destination, transportType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
