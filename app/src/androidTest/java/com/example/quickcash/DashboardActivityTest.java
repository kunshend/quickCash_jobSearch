package com.example.quickcash;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.example.quickcash.activities.DashboardActivity;
import com.example.quickcash.activities.SearchJobsActivity;
import com.example.quickcash.utilities.DashboardState;
import com.example.quickcash.utilities.EmployeeDashboardState;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation tests for the DashboardActivity.
 * Tests the functionality of the dashboard interface and navigation.
 * Uses both Espresso for UI testing and UIAutomator for system-level interactions.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DashboardActivityTest {

    /**
     * UiDevice instance for UIAutomator tests.
     * Used to interact with system-level UI components.
     */
    private UiDevice device;

    /**
     * Rule to launch the DashboardActivity before each test.
     */
    @Rule
    public ActivityScenarioRule<DashboardActivity> activityRule =
            new ActivityScenarioRule<>(DashboardActivity.class);

    /**
     * Sets up the UiDevice instance for UIAutomator tests.
     * This is required for tests that interact with system UI components.
     */
    @Before
    public void setUpAll() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Intents.init();
    }

    /**
     * Releases Espresso Intents after each test to prevent memory leaks.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Tests navigation from Dashboard to Settings.
     * Verifies the Settings button click navigates to the settings screen.
     */
    @Test
    public void testSettingsNavigation() {
            onView(withId(R.id.btnSettings)).perform(click());
            onView(withId(R.id.btnLogout)).check(matches(isDisplayed()));
    }

    /**
     * Tests system-level navigation using UIAutomator.
     * Verifies that system UI components related to settings navigation
     * are present and functional.
     *
     * @throws Exception if UIAutomator encounters an error
     */
    @Test
    public void testUIAutomatorNavigation() throws Exception {
        // Using UIAutomator to verify system-level interactions
        UiObject settingsButton = device.findObject(new UiSelector().text("Settings"));
        assertTrue("Settings button should be visible", settingsButton.exists());

        settingsButton.click();

        UiObject logoutButton = device.findObject(new UiSelector().text("Logout"));
        assertTrue("Should be in Settings activity", logoutButton.exists());
    }

    /**
     * Tests that the EmployeeDashboardState executes without error.
     * This test shows that the state design pattern classes function.
     */
    @Test
    public void testEmployeeDashboardStateMethods() {
        DashboardState state = new EmployeeDashboardState();

        //use the activity, make sure it doesn't crash!
        activityRule.getScenario().onActivity(activity -> {
            state.setupUI(activity, "TestUser");
            state.loadJobs(activity);
        });
    }
}