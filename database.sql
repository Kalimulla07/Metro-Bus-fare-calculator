-- ========================================================
-- Database Initialization Script for Metro & Bus Fare Calculator
-- ========================================================

CREATE DATABASE IF NOT EXISTS transit_fare_db;
USE transit_fare_db;

-- Table to store fare calculation history
CREATE TABLE IF NOT EXISTS fare_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    calculation_date DATETIME NOT NULL,
    transport_type VARCHAR(20) NOT NULL,
    source_station VARCHAR(100) NOT NULL,
    destination_station VARCHAR(100) NOT NULL,
    distance_km DOUBLE NOT NULL,
    passenger_type VARCHAR(30) NOT NULL,
    travel_time VARCHAR(20) NOT NULL,
    strategy_used VARCHAR(50) NOT NULL,
    base_fare DOUBLE NOT NULL,
    distance_charge DOUBLE NOT NULL,
    dynamic_adjustment DOUBLE NOT NULL,
    discount_amount DOUBLE NOT NULL,
    final_fare DOUBLE NOT NULL
);

-- Sample Initial Data for Dashboard Demo
INSERT INTO fare_history 
(calculation_date, transport_type, source_station, destination_station, distance_km, passenger_type, travel_time, strategy_used, base_fare, distance_charge, dynamic_adjustment, discount_amount, final_fare)
VALUES
(NOW() - INTERVAL 3 HOUR, 'METRO', 'Central Station', 'Tech Hub', 18.5, 'Normal', 'Peak', 'Distance-Based Strategy', 20.00, 37.00, 11.40, 0.00, 68.40),
(NOW() - INTERVAL 2 HOUR, 'BUS', 'Airport Terminal', 'City Center', 25.0, 'Student', 'Off-Peak', 'Zone-Based Strategy', 15.00, 30.00, 0.00, 11.25, 33.75),
(NOW() - INTERVAL 90 MINUTE, 'METRO', 'Downtown', 'University', 8.2, 'Senior Citizen', 'Peak', 'Peak/Off-Peak Strategy', 20.00, 16.40, 9.10, 18.20, 27.30),
(NOW() - INTERVAL 45 MINUTE, 'BUS', 'West End', 'Harbor Point', 12.0, 'Normal', 'Off-Peak', 'Distance-Based Strategy', 15.00, 14.40, 0.00, 0.00, 29.40),
(NOW() - INTERVAL 15 MINUTE, 'METRO', 'East Gate', 'Central Station', 14.5, 'Normal', 'Peak', 'Peak/Off-Peak Strategy', 20.00, 29.00, 14.70, 0.00, 63.70);
