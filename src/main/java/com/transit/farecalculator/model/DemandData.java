package com.transit.farecalculator.model;

import java.util.List;

/**
 * Model representing 24-hour passenger demand distribution and dynamic pricing recommendations.
 */
public class DemandData {

    public static class HourlyDemand {
        private String timeSlot;
        private int hour;
        private int passengerCount;
        private boolean isPeak;
        private double surgeMultiplier;

        public HourlyDemand() {
        }

        public HourlyDemand(String timeSlot, int hour, int passengerCount, boolean isPeak, double surgeMultiplier) {
            this.timeSlot = timeSlot;
            this.hour = hour;
            this.passengerCount = passengerCount;
            this.isPeak = isPeak;
            this.surgeMultiplier = surgeMultiplier;
        }

        public String getTimeSlot() {
            return timeSlot;
        }

        public void setTimeSlot(String timeSlot) {
            this.timeSlot = timeSlot;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getPassengerCount() {
            return passengerCount;
        }

        public void setPassengerCount(int passengerCount) {
            this.passengerCount = passengerCount;
        }

        public boolean isPeak() {
            return isPeak;
        }

        public void setPeak(boolean peak) {
            isPeak = peak;
        }

        public double getSurgeMultiplier() {
            return surgeMultiplier;
        }

        public void setSurgeMultiplier(double surgeMultiplier) {
            this.surgeMultiplier = surgeMultiplier;
        }
    }

    private List<HourlyDemand> hourlyData;
    private int peakThreshold;
    private double currentDemandIndex;
    private double recommendedPeakMultiplier;
    private double recommendedOffPeakMultiplier;
    private String peakHoursDescription;
    private String pricingRecommendationSummary;

    public DemandData() {
    }

    public List<HourlyDemand> getHourlyData() {
        return hourlyData;
    }

    public void setHourlyData(List<HourlyDemand> hourlyData) {
        this.hourlyData = hourlyData;
    }

    public int getPeakThreshold() {
        return peakThreshold;
    }

    public void setPeakThreshold(int peakThreshold) {
        this.peakThreshold = peakThreshold;
    }

    public double getCurrentDemandIndex() {
        return currentDemandIndex;
    }

    public void setCurrentDemandIndex(double currentDemandIndex) {
        this.currentDemandIndex = currentDemandIndex;
    }

    public double getRecommendedPeakMultiplier() {
        return recommendedPeakMultiplier;
    }

    public void setRecommendedPeakMultiplier(double recommendedPeakMultiplier) {
        this.recommendedPeakMultiplier = recommendedPeakMultiplier;
    }

    public double getRecommendedOffPeakMultiplier() {
        return recommendedOffPeakMultiplier;
    }

    public void setRecommendedOffPeakMultiplier(double recommendedOffPeakMultiplier) {
        this.recommendedOffPeakMultiplier = recommendedOffPeakMultiplier;
    }

    public String getPeakHoursDescription() {
        return peakHoursDescription;
    }

    public void setPeakHoursDescription(String peakHoursDescription) {
        this.peakHoursDescription = peakHoursDescription;
    }

    public String getPricingRecommendationSummary() {
        return pricingRecommendationSummary;
    }

    public void setPricingRecommendationSummary(String pricingRecommendationSummary) {
        this.pricingRecommendationSummary = pricingRecommendationSummary;
    }
}
