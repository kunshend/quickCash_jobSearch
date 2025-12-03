package com.example.quickcash;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.action.ViewActions.replaceText;

import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static org.junit.Assert.assertTrue;

import com.example.quickcash.activities.RegistrationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UI tests for RegistrationActivity.
 * Tests registration UI elements, navigation flow, and form submission.
 * Uses Espresso for UI testing and includes cleanup for test user accounts.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class RegistrationUITesting {

    /**
     * Timeout value in milliseconds for Firebase operations
     */
    private static final int TIMEOUT = 10000;

    /**
     * Test rule that initializes the RegistrationActivity for each test
     */
    @Rule
    public ActivityScenarioRule<RegistrationActivity> activityScenarioRule =
            new ActivityScenarioRule<>(RegistrationActivity.class);

    /**
     * Sets up the test environment before each test.
     * Launches the RegistrationActivity.
     */
    @Before
    public void setUp() {
        ActivityScenario.launch(RegistrationActivity.class);
    }

    /**
     * Tests toolbar back navigation functionality.
     * Verifies that clicking the back button in the toolbar returns to the login screen.
     */
    @Test
    public void testToolbarBackNavigation() {
        //Espresso uses default "Navigate up" content description to find back button
        onView(withContentDescription("Navigate up")).perform(click());
        onView(withId(R.id.activity_login_layout)).check(matches(isDisplayed()));
    }

    /**
     * Tests successful registration navigation to login screen.
     * Verifies that after a successful registration, the user is redirected to the login screen.
     *
     * @throws InterruptedException If the thread is interrupted during sleep
     */
    @Test
    public void testSuccessfulRegistrationNavigatesToLogin() throws InterruptedException {
        onView(withId(R.id.etFirstName)).perform(replaceText("Test"));
        onView(withId(R.id.etLastName)).perform(replaceText("Tom"));
        onView(withId(R.id.etEmail)).perform(replaceText("newtestemail@test.com"));
        onView(withId(R.id.etUsername)).perform(replaceText("TestingTom"));
        onView(withId(R.id.etPassword)).perform(replaceText("Pass123!"));

        //Click the Register button
        onView(withId(R.id.btnRegister)).perform(click());

        Thread.sleep(2000);

        //Verify that LoginActivity is displayed after successful registration
        onView(withId(R.id.activity_login_layout)).check(matches(isDisplayed()));
    }

    /**
     * Cleans up test user data after test completion.
     * Removes the test user from Firebase authentication and database.
     * Prevents test data pollution and ensures test isolation.
     */
    @After
    public void cleanUpTestUser() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean cleanupSuccess = new AtomicBoolean(false);

        //Sign in with the test user, then delete the user and database record
        mAuth.signInWithEmailAndPassword("newtestemail@test.com", "Pass123!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        //Delete user authentication
                        task.getResult().getUser().delete()
                                .addOnCompleteListener(deleteTask -> {
                                    //Delete user data from realtime database
                                    mDatabase.child("users").child("TestingTom").removeValue()
                                            .addOnCompleteListener(dbTask -> {
                                                cleanupSuccess.set(dbTask.isSuccessful());
                                                latch.countDown();
                                            });
                                });
                    } else {
                        mDatabase.child("users").child("TestingTom").removeValue()
                                .addOnCompleteListener(dbTask -> {
                                    cleanupSuccess.set(dbTask.isSuccessful());
                                    latch.countDown();
                                });
                    }
                });

        try {
            if (!latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
                Log.e("RegistrationTest", "Cleanup timed out!");
            }
            assertTrue("User cleanup failed", cleanupSuccess.get());
        } catch (InterruptedException e) {
            Log.e("RegistrationTest", "Cleanup was interrupted", e);
        }
    }
}