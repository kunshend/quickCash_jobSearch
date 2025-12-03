package com.example.quickcash;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.quickcash.activities.EmailVerifyActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertTrue;

/**
 * Instrumentation tests for the EmailVerifyActivity.
 * Tests email verification functionality including UI elements
 * and validation logic.
 * Uses UIAutomator for UI interaction and validation.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EmailVerifyTest {

    /**
     * UiDevice instance for UIAutomator tests.
     * Used to interact with UI components.
     */
    private UiDevice device;

    /**
     * Rule to launch the EmailVerifyActivity before each test.
     */
    @Rule
    public ActivityScenarioRule<EmailVerifyActivity> activityScenarioRule =
            new ActivityScenarioRule<>(EmailVerifyActivity.class);

    /**
     * Sets up the test environment before each test.
     * Launches the activity and initializes the UiDevice.
     */
    @Before
    public void setUp() {
        ActivityScenario.launch(EmailVerifyActivity.class);
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Tests the toolbar navigation functionality.
     * Verifies that clicking the toolbar button navigates to settings.
     *
     * @throws UiObjectNotFoundException if UI element is not found
     */
    @Test
    public void testEmailVerifyToolbar() throws UiObjectNotFoundException {
        UiObject toolbarButton = device.findObject(new UiSelector().clickable(true));
        assertTrue("Toolbar should be visible", toolbarButton.exists());

        toolbarButton.click();

        UiObject welcomeText = device.findObject(new UiSelector().textContains("Settings"));
        assertTrue("Should be in settings page", welcomeText.exists());

    }

    /**
     * Tests validation for invalid email input.
     * Verifies that the app displays an error message when an invalid
     * email format is submitted.
     *
     * @throws UiObjectNotFoundException if UI element is not found
     */
    @Test
    public void testEmailVerifyBadEmail() throws UiObjectNotFoundException {
        UiObject typeEmailField = device.findObject(new UiSelector().text("Please enter registered email"));
        UiObject submit = device.findObject(new UiSelector().text("Submit"));

        typeEmailField.setText("Not an email");
        submit.click();

        UiObject welcomeText = device.findObject(new UiSelector().textContains("Email fields cannot be empty or Email not exist"));
        assertTrue("Should remain on reset password page", welcomeText.exists());

    }

    /**
     * Tests verification of existing email addresses.
     * Verifies that the app correctly handles valid, registered email addresses
     * and navigates to the password reset page.
     *
     * @throws UiObjectNotFoundException if UI element is not found
     */
    @Test
    public void testEmailVerifyEmailExists() throws UiObjectNotFoundException {
        UiObject typeEmailField = device.findObject(new UiSelector().text("Please enter registered email"));
        UiObject submit = device.findObject(new UiSelector().text("Submit"));

        //tester@dal.ca should be in database
        typeEmailField.setText("tester@dal.ca");
        submit.click();

        UiObject welcomeText = device.findObject(new UiSelector().textContains("Setting new password"));
        assertTrue("Should move to reset password page", welcomeText.exists());

    }

}