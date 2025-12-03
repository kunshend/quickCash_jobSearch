package com.example.quickcash;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.quickcash.activities.DashboardActivity;
import com.example.quickcash.activities.SettingsActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation tests for SettingsActivity.
 * Tests settings UI elements, dialogs, role change functionality, and toolbar navigation.
 * Uses both Espresso for UI testing and UiAutomator for system-level interactions.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsActivityTest {

    /**
     * UiDevice instance for UiAutomator tests
     */
    private UiDevice device;

    /**
     * Test rule that initializes the SettingsActivity for each test
     */
    @Rule
    public ActivityScenarioRule<SettingsActivity> activityRule =
            new ActivityScenarioRule<>(SettingsActivity.class);

    /**
     * Tests that clicking the role change button displays the role selection dialog.
     * Verifies the dialog appears with the expected title.
     */
    @Test
    public void testRoleChangeDialog() {
        // Click change role button
        onView(withId(R.id.btnChangeRole)).perform(click());

        // Verify role selection dialog appears
        onView(withText("Select Role"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the logout button displays the logout confirmation dialog.
     * Verifies the dialog appears with the expected title.
     */
    @Test
    public void testLogoutDialog() {
        // Click logout button
        onView(withId(R.id.btnLogout)).perform(click());

        // Verify logout confirmation dialog appears
        onView(withText("Confirm Logout"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that selecting a role in the role change dialog displays the email confirmation dialog.
     * Verifies that the email field is displayed for confirmation.
     */
    @Test
    public void testEmailConfirmationDialog() {
        // Click change role button
        onView(withId(R.id.btnChangeRole)).perform(click());

        // Select "Employee" role
        onView(withText("Employee"))
                .inRoot(isDialog())
                .perform(click());

        // Verify email confirmation dialog appears
        onView(withId(R.id.emailConfirmationEmail))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests that entering an invalid email in the confirmation dialog
     * keeps the user on the dialog screen.
     * Verifies proper validation for email confirmation.
     */
    @Test
    public void testInvalidEmailRoleChange() {
        // Start role change process
        onView(withId(R.id.btnChangeRole)).perform(click());

        // Wait for and click "Employee" in role selection dialog
        onView(withText("Employee"))
                .inRoot(isDialog())
                .perform(click());

        // First verify the dialog is showing with the message
        onView(withId(R.id.messageText))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        // Enter invalid email
        onView(withId(R.id.emailConfirmationEmail))
                .inRoot(isDialog())
                .perform(typeText("wrong@email.com"), closeSoftKeyboard());

        // Click confirm and verify dialog stays visible (since email was invalid)
        onView(withId(R.id.confirmButton))
                .inRoot(isDialog())
                .perform(click());

        // Verify dialog is still visible (it shouldn't dismiss with invalid email)
        onView(withId(R.id.messageText))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Sets up the UiDevice for UiAutomator tests before each test.
     */
    @Before
    public void setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Tests the settings toolbar navigation functionality using UiAutomator.
     * Verifies clicking the toolbar navigates back to the dashboard.
     *
     * @throws UiObjectNotFoundException If UI objects cannot be found
     */
    @Test
    public void testSettingsToolbar() throws UiObjectNotFoundException {
        UiObject settingsButton = device.findObject(new UiSelector().clickable(true));
        assertTrue("Settings toolbar should be visible", settingsButton.exists());

        settingsButton.click();

        UiObject welcomeText = device.findObject(new UiSelector().text("Settings").clickable(true));
        assertTrue("Should be in dashboard", welcomeText.exists());
    }
}