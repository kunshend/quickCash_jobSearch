package com.example.quickcash;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.example.quickcash.R;
import com.example.quickcash.activities.ApplicationActivity;
import com.example.quickcash.activities.DashboardActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation tests for the ApplicationActivity.
 * Tests the functionality of job application submission process.
 * Verifies proper validation of application messages and navigation
 * to the Dashboard after successful submission.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationActivityTest {
    /**
     * Rule to initialize Espresso-Intents before each test and release
     * after each test in order to track and validate intents between activities.
     */
    @Rule
    public IntentsTestRule<ApplicationActivity> activityRule =
            new IntentsTestRule<ApplicationActivity>(ApplicationActivity.class) {
                @Override
                protected Intent getActivityIntent() {
                    Intent intent = new Intent();
                    intent.putExtra("jobId", "fakeJobId");
                    return intent;
                }
            };

    @Rule
    // grant permission to use location
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);




    /**
     * Tests that the application stays on the same activity when trying to submit
     * an empty application message.
     * Verifies that validation prevents empty submissions.
     */

    @Before
    public void signInDummyUser() throws Exception {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Only sign in if not already signed in
        if (auth.getCurrentUser() == null) {
            // Sign in
            Task<AuthResult> task = auth.signInWithEmailAndPassword("thomas@watchmans.com", "hellohello");
            Tasks.await(task);

            if (!task.isSuccessful()) {
                throw new RuntimeException("Firebase test sign-in failed.");
            }
        }
    }
    @Test
    public void emptyMessageStaysOnApplication() {
        // clear the field or ensure it's empty
        onView(withId(R.id.message_field)).perform(clearText());

        // click the submit button
        onView(withId(R.id.submit_button)).perform(click());

        // expect to stay on ApplicationActivity
        // by checking if the message field still exists
        onView(withId(R.id.message_field)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the application correctly navigates to the DashboardActivity
     * after submitting a valid, non-empty application message.
     * Verifies the navigation flow upon successful form submission.
     */
    @Test
    public void validApplicationLaunchesDashboard() {
        // initialize intents
        onView(withId(R.id.message_field)).perform(replaceText("Some message"));

        // submit
        onView(withId(R.id.submit_button)).perform(click());

        // verify redirection to dashboard
        intended(hasComponent(DashboardActivity.class.getName()));

    }

}