package com.transit.farecalculator.repository;

import com.transit.farecalculator.model.FareHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for FareHistory entity.
 * Provides out-of-the-box CRUD operations and custom metric aggregation queries.
 */
@Repository
public interface FareHistoryRepository extends JpaRepository<FareHistory, Long> {

    /**
     * Retrieve all fare records ordered from newest to oldest.
     */
    List<FareHistory> findAllByOrderByCalculationDateDesc();

    /**
     * Compute average fare across all stored journey calculations.
     */
    @Query("SELECT AVG(f.finalFare) FROM FareHistory f")
    Double findAverageFare();

    /**
     * Compute average fare specifically for Peak hour journeys.
     */
    @Query("SELECT AVG(f.finalFare) FROM FareHistory f WHERE LOWER(f.travelTime) = 'peak'")
    Double findAveragePeakFare();

    /**
     * Compute average fare specifically for Off-Peak hour journeys.
     */
    @Query("SELECT AVG(f.finalFare) FROM FareHistory f WHERE LOWER(f.travelTime) = 'off-peak'")
    Double findAverageOffPeakFare();
}
