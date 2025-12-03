package com.example.quickcash;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.quickcash.activities.DashboardActivity;
import com.example.quickcash.entities.Job;
import com.example.quickcash.utilities.LocationUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the Nearby Jobs feature.
 * Tests the functionality of location-based job filtering.
 * Verifies distance calculation and job filtering by proximity.
 * Created by Ross - March 2025
 *
 * @author Ross
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NearbyJobsTest {

    /**
     * Rule to launch the dashboard activity for testing.
     */
    @Rule
    public ActivityScenarioRule<DashboardActivity> activityRule =
            new ActivityScenarioRule<>(DashboardActivity.class);

    /**
     * List of mock job objects for testing.
     */
    private List<Job> mockJobs;

    /**
     * Sets up the test environment before each test.
     * Initializes mock objects and creates test job data.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Create mock jobs for testing
        mockJobs = new ArrayList<>();
        mockJobs.add(new Job("Job 1", "Description 1", "Technology", 44.6356, -63.5957));
        mockJobs.add(new Job("Job 2", "Description 2", "Education", 44.6358, -63.5959));
        mockJobs.add(new Job("Job 3", "Description 3", "Services", 45.5017, -73.5673)); // Montreal - far away
    }

    /**
     * Tests the distance calculation functionality.
     * Verifies that nearby locations have small distances,
     * and distant locations have large distances.
     */
    @Test
    public void testCalculateDistance() {
        // Halifax coordinates (near Course Professor job)
        double lat1 = 44.6356;
        double lon1 = -63.5957;

        // Another Halifax location (nearby)
        double lat2 = 44.6358;
        double lon2 = -63.5959;

        // Montreal coordinates (far)
        double lat3 = 45.5017;
        double lon3 = -73.5673;

        double nearby = LocationUtils.calculateDistance(lat1, lon1, lat2, lon2);
        double far = LocationUtils.calculateDistance(lat1, lon1, lat3, lon3);

        // Distance within Halifax should be less than 1 km
        assertTrue("Nearby locations should be < 1km apart", nearby < 1.0);

        // Distance to Montreal should be > 500 km
        assertTrue("Far locations should be > 500km apart", far > 500.0);
    }

    /**
     * Tests the job filtering by distance functionality.
     * Verifies that only jobs within the specified radius are included
     * in the filtered list, and distant jobs are excluded.
     */
    @Test
    public void testFilterNearbyJobs() {
        // Set user location to Halifax
        double userLat = 44.6356;
        double userLon = -63.5957;

        // Set a 10km radius
        double radius = 10.0;

        List<Job> nearbyJobs = LocationUtils.filterJobsByDistance(mockJobs, userLat, userLon, radius);

        // Should have 2 nearby jobs (in Halifax)
        assertEquals("Should have 2 nearby jobs", 2, nearbyJobs.size());

        // Ensure Montreal job is not included
        boolean containsMontrealJob = false;
        for (Job job : nearbyJobs) {
            if (job.getName().equals("Job 3")) {
                containsMontrealJob = true;
                break;
            }
        }
        assertFalse("Far job should not be in nearby list", containsMontrealJob);
    }
}