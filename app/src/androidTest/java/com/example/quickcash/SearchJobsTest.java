package com.example.quickcash;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.quickcash.entities.Job;
import com.example.quickcash.activities.SearchJobsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class SearchJobsTest {

    @Rule
    public ActivityScenarioRule<SearchJobsActivity> activityScenarioRule =
            new ActivityScenarioRule<>(SearchJobsActivity.class);

    private SearchJobsActivity activity;

    @Before
    public void setUp() {
        activityScenarioRule.getScenario().onActivity(act -> activity = act);
    }

    /**
     * @author Ethan Pancura
     * Test that the search bar is displayed
     */
    @Test
    public void testSearchBarDisplays() {
        onView(withId(R.id.searchBar)).check(matches(isDisplayed()));
    }

    /**
     * @author Ethan Pancura
     * Test that we can type text into the search bar
     */
    @Test
    public void testSearchBarFunctionality() {
        String searchExample = "Software Developer";

        onView(withId(R.id.searchBar))
                .perform(replaceText(searchExample));

        onView(withId(R.id.searchBar))
                .check(matches(withText(searchExample)));
    }

}






