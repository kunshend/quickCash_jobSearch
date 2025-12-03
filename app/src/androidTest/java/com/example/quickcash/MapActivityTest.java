package com.example.quickcash;

import android.Manifest;
import android.location.Location;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.quickcash.entities.Job;
import com.example.quickcash.activities.MapActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Instrumentation tests for MapActivity functionality.
 * Tests the map display, location permissions, user location updates,
 * job marker display, and Firebase integration.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class MapActivityTest {

    /**
     * Rule to automatically grant location permissions required for map functionality.
     * This eliminates the need for manual permission handling during test execution.
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);

    /**
     * Tests whether the Google Map view is properly displayed in the activity.
     * Verifies that the map view is visible to the user.
     */
    @Test
    public void testMapIsDisplayed() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    /**
     * Tests if location permissions are properly requested and handled.
     * Since permissions are granted by the test rule, this should return true.
     */
    @Test
    public void testLocationPermissionRequested() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        scenario.onActivity(activity -> {
            boolean result = activity.getLocationPermission();
            // If permissions are already granted (which they should be with our rule)
            // this should return true
            assertEquals(true, result);
        });
    }

    /**
     * Tests if user location is correctly displayed on the map.
     * Provides a mock location (Halifax coordinates) and verifies
     * that the map is centered on this location.
     */
    @Test
    public void testUserLocationDisplayed() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        scenario.onActivity(activity -> {
            // Mock location data
            Location mockLocation = new Location("gps");
            mockLocation.setLatitude(44.6488);
            mockLocation.setLongitude(-63.5752); // Halifax coordinates

            activity.updateLocationUI(mockLocation);

            // Verify map is centered on correct location
            LatLng currentPosition = activity.getCurrentPosition();
            assertNotNull(currentPosition);
            assertEquals(44.6488, currentPosition.latitude, 0.0001);
            assertEquals(-63.5752, currentPosition.longitude, 0.0001);
        });
    }

    /**
     * Tests if job markers are correctly displayed on the map.
     * Creates mock job data and verifies markers are added to the map.
     */
    @Test
    public void testNearbyJobsDisplayed() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        scenario.onActivity(activity -> {
            // Create mock job data
            List<Job> mockJobs = new ArrayList<>();
            mockJobs.add(new Job("Job1", "Description1", "user1", -63.5755, 44.6490));
            mockJobs.add(new Job("Job2", "Description2", "user2", -63.5770, 44.6500));

            // Call the method that would display jobs
            activity.displayJobMarkers(mockJobs);

            // Verify markers were added to the map
            List<Marker> markers = activity.getJobMarkers();
            assertEquals(2, markers.size());
        });
    }

    /**
     * Tests the integration with Firebase to load nearby jobs.
     * Sets a mock location and verifies that jobs are successfully
     * loaded from Firebase within the specified radius.
     * Uses a CountDownLatch to manage the asynchronous operation.
     */
    @Test
    public void testLoadNearbyJobsFromFirebase() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        scenario.onActivity(activity -> {
            // Mock the current location
            Location mockLocation = new Location("gps");
            mockLocation.setLatitude(44.6488);
            mockLocation.setLongitude(-63.5752);

            // Use a CountDownLatch to wait for async operation
            CountDownLatch latch = new CountDownLatch(1);

            activity.loadNearbyJobs(mockLocation, jobs -> {
                // Verify jobs were loaded
                assertNotNull(jobs);
                // Don't assert specific count as it depends on Firebase data
                latch.countDown();
            });

            try {
                // Wait for async operation with timeout
                latch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail("Test timed out");
            }
        });
    }
}