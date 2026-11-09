package com.transit.farecalculator.model;

/**
 * Model representing the 4 key dashboard metrics.
 */
public class DashboardStats {

    private long totalJourneys;
    private double averageFare;
    private double peakHourFare;
    private double offPeakFare;

    public DashboardStats() {
    }

    public DashboardStats(long totalJourneys, double averageFare, double peakHourFare, double offPeakFare) {
        this.totalJourneys = totalJourneys;
        this.averageFare = averageFare;
        this.peakHourFare = peakHourFare;
        this.offPeakFare = offPeakFare;
    }

    public long getTotalJourneys() {
        return totalJourneys;
    }

    public void setTotalJourneys(long totalJourneys) {
        this.totalJourneys = totalJourneys;
    }

    public double getAverageFare() {
        return averageFare;
    }

    public void setAverageFare(double averageFare) {
        this.averageFare = averageFare;
    }

    public double getPeakHourFare() {
        return peakHourFare;
    }

    public void setPeakHourFare(double peakHourFare) {
        this.peakHourFare = peakHourFare;
    }

    public double getOffPeakFare() {
        return offPeakFare;
    }

    public void setOffPeakFare(double offPeakFare) {
        this.offPeakFare = offPeakFare;
    }
}
