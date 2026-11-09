package com.transit.farecalculator.service;

import com.transit.farecalculator.model.DashboardStats;
import com.transit.farecalculator.model.FareHistory;
import com.transit.farecalculator.model.FareRequest;
import com.transit.farecalculator.model.FareResponse;
import com.transit.farecalculator.repository.FareHistoryRepository;
import com.transit.farecalculator.strategy.FareCalculationStrategy;
import com.transit.farecalculator.strategy.FareStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service orchestrating fare calculations, strategy delegation, 
 * database persistence, and aggregate metrics.
 */
@Service
public class FareCalculationService {

    private final FareStrategyFactory strategyFactory;
    private final RouteService routeService;
    private final FareHistoryRepository fareHistoryRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public FareCalculationService(FareStrategyFactory strategyFactory, 
                                  RouteService routeService, 
                                  FareHistoryRepository fareHistoryRepository) {
        this.strategyFactory = strategyFactory;
        this.routeService = routeService;
        this.fareHistoryRepository = fareHistoryRepository;
    }

    /**
     * Calculates the transit fare using the Strategy Design Pattern and saves record to MySQL.
     */
    @Transactional
    public FareResponse calculateAndSaveFare(FareRequest request) {
        // Resolve station zones
        int sourceZone = routeService.getStationZone(request.getSourceStation());
        int destZone = routeService.getStationZone(request.getDestinationStation());

        // Select strategy dynamically
        FareCalculationStrategy strategy = strategyFactory.getStrategy(request.getStrategyType());

        // Perform calculation
        FareResponse response = strategy.calculateFare(request, sourceZone, destZone);

        // Persist to MySQL database
        FareHistory history = new FareHistory(
            request.getTransportType().toUpperCase(),
            request.getSourceStation(),
            request.getDestinationStation(),
            request.getDistanceKm(),
            request.getPassengerType(),
            request.getTravelTime(),
            response.getStrategyUsed(),
            response.getBaseFare(),
            response.getDistanceCharge(),
            response.getDynamicAdjustment(),
            response.getDiscountAmount(),
            response.getFinalFare()
        );

        FareHistory saved = fareHistoryRepository.save(history);

        // Return enriched response with database ID and timestamp
        response.setHistoryId(saved.getId());
        response.setCalculationTimestamp(saved.getCalculationDate().format(FORMATTER));

        return response;
    }

    /**
     * Retrieve complete fare history ordered from newest to oldest.
     */
    public List<FareHistory> getFareHistory() {
        return fareHistoryRepository.findAllByOrderByCalculationDateDesc();
    }

    /**
     * Delete a fare history record by ID.
     */
    @Transactional
    public boolean deleteFareHistory(Long id) {
        if (fareHistoryRepository.existsById(id)) {
            fareHistoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Clear all fare history records.
     */
    @Transactional
    public void clearAllHistory() {
        fareHistoryRepository.deleteAll();
    }

    /**
     * Compute statistics for the 4 dashboard summary cards.
     */
    public DashboardStats getDashboardStats() {
        long totalJourneys = fareHistoryRepository.count();

        Double avgFare = fareHistoryRepository.findAverageFare();
        Double peakFare = fareHistoryRepository.findAveragePeakFare();
        Double offPeakFare = fareHistoryRepository.findAverageOffPeakFare();

        double formattedAvg = (avgFare != null) ? round(avgFare) : 48.50;
        double formattedPeak = (peakFare != null) ? round(peakFare) : 62.00;
        double formattedOffPeak = (offPeakFare != null) ? round(offPeakFare) : 34.00;

        return new DashboardStats(totalJourneys, formattedAvg, formattedPeak, formattedOffPeak);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
