package com.example.quickcash;

import android.view.View;
import android.widget.EditText;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import com.example.quickcash.activities.LoginActivity;
import com.example.quickcash.activities.RegistrationActivity;
import com.example.quickcash.activities.ResetPasswordActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Instrumentation tests for LoginActivity with focus on settings and validation.
 * Tests login functionality, form validation, navigation, and error handling.
 * Uses Espresso for UI interaction and validation.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginSettingsTest {

    /**
     * Test rule that initializes the LoginActivity for each test
     */
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Test email constant for login tests
     */
    private static final String TEST_EMAIL = "test@example.com";

    /**
     * Test password constant for login tests
     */
    private static final String TEST_PASSWORD = "testpassword";

    /**
     * Custom matcher to check for error messages on EditText fields.
     * Helps verify validation error messages are correctly displayed.
     *
     * @param expectedError The expected error message to match
     * @return A matcher that checks if an EditText has the expected error
     */
    public static Matcher<View> hasError(final String expectedError) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof EditText)) {
                    return false;
                }
                EditText editText = (EditText) item;
                CharSequence error = editText.getError();
                if (error == null) {
                    return expectedError == null;
                }
                return expectedError.equals(error.toString());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with error: " + expectedError);
            }
        };
    }

    /**
     * Tests successful login and navigation to the dashboard.
     * Verifies that valid credentials allow navigation to the dashboard screen.
     */
    @Test
    public void testLoginToDashboard() {
        // Use valid test credentials
        String TEST_EMAIL = "Zaza@gmail.com";
        String TEST_PASSWORD = "Zaza@gmail.com";

        // Enter login credentials
        onView(withId(R.id.etEmail))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.btnLogin)).perform(click());

        // Add a wait for the network call and activity transition
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // First verify we're not still on login screen
        boolean stillOnLoginScreen = true;
        try {
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        } catch (NoMatchingViewException e) {
            stillOnLoginScreen = false;
        }

        if (stillOnLoginScreen) {
            // If we're still on login screen, the test should fail with a meaningful message
            throw new AssertionError("Login failed: Still on login screen after clicking login button. " +
                    "Please verify TEST_EMAIL and TEST_PASSWORD are valid credentials.");
        }

        // If we made it here, we're not on login screen, so check for dashboard
        onView(withId(R.id.btnSettings))
                .withFailureHandler((error, viewMatcher) -> {
                    throw new AssertionError("Navigation to settings failed: btnChangeRole not found. " +
                            "Please verify navigation logic after successful login.");
                })
                .check(matches(isDisplayed()));
    }

    /**
     * Tests validation for empty email field.
     * Verifies that attempting to login with an empty email shows the appropriate error.
     */
    @Test
    public void testEmptyEmailValidation() {
        // Leave email empty and try to login
        onView(withId(R.id.etPassword))
                .perform(typeText(TEST_PASSWORD), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // Verify error message
        onView(withId(R.id.etEmail))
                .check(matches(hasError("Please enter your email")));
    }

    /**
     * Tests validation for empty password field.
     * Verifies that attempting to login with an empty password shows the appropriate error.
     */
    @Test
    public void testEmptyPasswordValidation() {
        // Leave password empty and try to login
        onView(withId(R.id.etEmail))
                .perform(typeText(TEST_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // Verify error message
        onView(withId(R.id.etPassword))
                .check(matches(hasError("Please enter your password")));
    }

    /**
     * Tests navigation to registration screen from login.
     * Verifies that clicking the register button navigates to the registration activity.
     */
    @Test
    public void testRegisterButtonToRegistration() {
        // Initialize Intents
        Intents.init();
        onView(withId(R.id.btnRegister)).perform(click());
        // Check if the correct intent was fired
        intended(hasComponent(RegistrationActivity.class.getName()));

        Intents.release();
    }

    /**
     * Tests login behavior with invalid credentials.
     * Verifies that using invalid login credentials keeps the user on the login screen.
     */
    @Test
    public void testInvalidCredentialsRemainsOnLoginScreen() {
        // Enter invalid credentials
        onView(withId(R.id.etEmail)).perform(typeText("invalid@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("wrongPassword123"), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

        try {
            Thread.sleep(2000); // Give Firebase time to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that we are still on the LoginActivity
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }
}