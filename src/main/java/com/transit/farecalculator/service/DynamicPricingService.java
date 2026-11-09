package com.transit.farecalculator.service;

import com.transit.farecalculator.model.DemandData;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing transit demand profiling, dynamic pricing calculations,
 * and passenger surge pricing recommendations.
 */
@Service
public class DynamicPricingService {

    private static final int PEAK_DEMAND_THRESHOLD = 6000;

    /**
     * Retrieves the 24-hour demand data profile and peak/off-peak pricing metrics.
     */
    public DemandData getDemandAnalytics() {
        DemandData data = new DemandData();
        data.setPeakThreshold(PEAK_DEMAND_THRESHOLD);
        data.setRecommendedPeakMultiplier(1.30); // +30% surge
        data.setRecommendedOffPeakMultiplier(0.90); // 10% saver discount
        data.setPeakHoursDescription("Morning Peak: 08:00 AM - 10:30 AM | Evening Peak: 05:00 PM - 07:30 PM");
        data.setPricingRecommendationSummary(
            "Shift discretionary trips to 11:00 AM - 04:00 PM or after 08:00 PM to save up to 40% on overall transit fares and help alleviate peak congestion."
        );

        List<DemandData.HourlyDemand> list = new ArrayList<>();

        // Hourly distribution model (24 hours)
        int[] hourlyPassengers = {
            650,   // 00:00
            400,   // 01:00
            350,   // 02:00
            500,   // 03:00
            1100,  // 04:00
            2800,  // 05:00
            5200,  // 06:00
            7400,  // 07:00 (Peak begins)
            9600,  // 08:00 (Morning Peak)
            10400, // 09:00 (Morning Super Peak)
            8100,  // 10:00 (Peak tapering)
            4600,  // 11:00 (Off-Peak)
            4200,  // 12:00 (Off-Peak)
            4100,  // 13:00 (Off-Peak)
            4800,  // 14:00 (Off-Peak)
            5400,  // 15:00 (Off-Peak)
            6800,  // 16:00 (Rising)
            9900,  // 17:00 (Evening Peak)
            11500, // 18:00 (Evening Super Peak)
            8900,  // 19:00 (Evening Peak)
            5300,  // 20:00 (Off-Peak)
            3800,  // 21:00 (Off-Peak)
            2400,  // 22:00 (Night)
            1300   // 23:00 (Night)
        };

        for (int h = 0; h < 24; h++) {
            String label = String.format("%02d:00", h);
            int count = hourlyPassengers[h];
            boolean isPeak = count >= PEAK_DEMAND_THRESHOLD;
            double surge = isPeak ? 1.30 : (count < 2500 ? 0.85 : 0.90);

            list.add(new DemandData.HourlyDemand(label, h, count, isPeak, surge));
        }

        data.setHourlyData(list);

        // Current real-time demand index based on server clock
        int currentHour = LocalTime.now().getHour();
        int currentCount = hourlyPassengers[currentHour];
        data.setCurrentDemandIndex(Math.round(((double) currentCount / 11500.0 * 100.0) * 10.0) / 10.0);

        return data;
    }

    /**
     * Determines whether a given hour is currently classified as peak demand.
     */
    public boolean isPeakHour(int hour) {
        return (hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 19);
    }
}
