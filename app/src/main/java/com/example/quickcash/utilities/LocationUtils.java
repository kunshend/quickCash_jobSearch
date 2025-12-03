package com.example.quickcash.utilities;

import com.example.quickcash.entities.Job;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for location-based operations
 */
public class LocationUtils {

    // Earth radius in kilometers
    private static final double EARTH_RADIUS = 6371.0;

    /**
     * Calculate distance between two points using the Haversine formula
     * Returns distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;
        double a = Math.pow(Math.sin(dlat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        return EARTH_RADIUS * c;
    }

    /**
     * Filter jobs by distance from a specific location
     * @param jobs List of all jobs
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param maxDistanceKm Maximum distance in kilometers
     * @return List of jobs within the specified distance
     */
    public static List<Job> filterJobsByDistance(List<Job> jobs, double latitude,
                                                 double longitude, double maxDistanceKm) {
        List<Job> nearbyJobs = new ArrayList<>();

        for (Job job : jobs) {
            double distance = calculateDistance(latitude, longitude, job.getLatitude(), job.getLongitude());
            if (distance <= maxDistanceKm) {
                nearbyJobs.add(job);
            }
        }

        return nearbyJobs;
    }
}