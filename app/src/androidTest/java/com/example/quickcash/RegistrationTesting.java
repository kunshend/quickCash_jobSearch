package com.example.quickcash;

import static org.junit.Assert.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.quickcash.activities.RegistrationActivity;
import com.example.quickcash.entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for RegistrationActivity with Firebase authentication.
 * Tests user registration functionality, duplicate username/email validation,
 * and Firebase integration.
 * Uses CountDownLatch for synchronizing asynchronous Firebase operations.
 *
 * @author QuickCash Team
 * @version 1.0
 */
@RunWith(AndroidJUnit4.class)
public class RegistrationTesting {

    /**
     * ActivityScenario for controlling the test activity lifecycle
     */
    private ActivityScenario<RegistrationActivity> scenario;

    /**
     * Timeout value in milliseconds for Firebase operations
     */
    private static final int TIMEOUT = 10000;

    /**
     * Cleans up Firebase data before running tests.
     * Deletes test user accounts and data to ensure a clean test environment.
     */
    @Before
    public void cleanUpFirebase() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        CountDownLatch cleanupLatch = new CountDownLatch(1);

        //Sign in to each test account and delete the auth record
        mAuth.signInWithEmailAndPassword("testuser@gmail.com", "TestPass123!")
                .addOnCompleteListener(signIn1 -> {
                    if (signIn1.isSuccessful() && signIn1.getResult().getUser() != null) {
                        signIn1.getResult().getUser().delete();
                    }
                    mAuth.signInWithEmailAndPassword("testuser1@gmail.com", "TestPass123!")
                            .addOnCompleteListener(signIn2 -> {
                                if (signIn2.isSuccessful() && signIn2.getResult().getUser() != null) {
                                    signIn2.getResult().getUser().delete();
                                }
                                mAuth.signInWithEmailAndPassword("testuser2@gmail.com", "TestPass789!")
                                        .addOnCompleteListener(signIn3 -> {
                                            if (signIn3.isSuccessful() && signIn3.getResult().getUser() != null) {
                                                signIn3.getResult().getUser().delete();
                                            }
                                            //After auth cleanup, delete data from realtime database
                                            mDatabase.child("users").child("testuser").removeValue()
                                                    .addOnCompleteListener(task -> {
                                                        mDatabase.child("users").child("testuser1").removeValue()
                                                                .addOnCompleteListener(task2 -> {
                                                                    mDatabase.child("users").child("testuser2").removeValue()
                                                                            .addOnCompleteListener(task3 -> {
                                                                                mAuth.signOut(); // Sign out after all operations
                                                                                cleanupLatch.countDown();
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                });

        try {
            assertTrue("Database timed out", cleanupLatch.await(TIMEOUT, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            fail("Database cleanup was interrupted");
        }
    }

    /**
     * Sets up the test environment before each test.
     * Launches the RegistrationActivity.
     */
    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(RegistrationActivity.class);
    }

    /**
     * Tests successful user registration.
     * Verifies that a new user is properly added to the Firebase database.
     */
    @Test
    public void userAdded() {
        //thread synchronizer to force proper interaction with realtime database
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean userExists = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            //set user fields
            activity.setEmail("testuser@gmail.com");
            activity.setPassword("TestPass123!");
            activity.setUsername("testuser");
            activity.setFirstName("First");
            activity.setLastName("Last");
            activity.setRole(0);
            activity.clickRegisterButton();

            //wait a bit for registration to complete
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("users").child("testuser").get()
                        .addOnCompleteListener(task -> {
                            userExists.set(task.isSuccessful() && task.getResult().exists());
                            latch.countDown();
                        });
            }, 3000); //Wait 3 seconds before checking
        });

        try {
            assertTrue("Test timed out", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
            assertTrue("User should exist in database", userExists.get());
        } catch (InterruptedException e) {
            fail("Test was interrupted");
        }
    }

    /**
     * Tests username uniqueness validation.
     * Verifies that the system correctly prevents registering with a username
     * that is already taken.
     */
    @Test
    public void isUsernameTaken() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean firstUserExists = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            //Register first user
            activity.setEmail("testuser1@gmail.com");
            activity.setPassword("TestPass123!");
            activity.setUsername("testuser");
            activity.setFirstName("First");
            activity.setLastName("Last");
            activity.setRole(0);
            activity.clickRegisterButton();

            //wait for first registration to complete before checking
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                //register second user with same username
                activity.setEmail("testuser2@gmail.com");
                activity.setPassword("TestPass789!");
                activity.setUsername("testuser");
                activity.setFirstName("First");
                activity.setLastName("Last");
                activity.setRole(1);
                activity.clickRegisterButton();

                //check after both operations
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child("testuser").get()
                            .addOnCompleteListener(task -> {
                                firstUserExists.set(task.isSuccessful() && task.getResult().exists());
                                latch.countDown();
                            });
                }, 3000);
            }, 3000);
        });

        try {
            assertTrue("Test timed out", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
            assertTrue("First user should still exist", firstUserExists.get());
        } catch (InterruptedException e) {
            fail("Test was interrupted");
        }
    }

    /**
     * Tests email uniqueness validation.
     * Verifies that the system correctly prevents registering with an email
     * that is already associated with an account.
     */
    @Test
    public void emailInUse() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean secondUserExists = new AtomicBoolean(true);
        AtomicReference<User> firstUser = new AtomicReference<>();

        scenario.onActivity(activity -> {
            //register user
            activity.setEmail("testuser@gmail.com");
            activity.setPassword("TestPass123!");
            activity.setUsername("testuser1");
            activity.setFirstName("First");
            activity.setLastName("Last");
            activity.setRole(0);
            activity.clickRegisterButton();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                //attempt second registration with same email
                activity.setEmail("testuser@gmail.com");
                activity.setPassword("TestPass789!");
                activity.setUsername("testuser2");
                activity.setFirstName("First");
                activity.setLastName("Last");
                activity.setRole(1);
                activity.clickRegisterButton();

                //check if second user was created
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child("testuser2").get()
                            .addOnCompleteListener(task -> {
                                secondUserExists.set(task.getResult().exists());
                                mDatabase.child("users").child("testuser1").get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful() && task2.getResult().exists()) {
                                                firstUser.set(task2.getResult().getValue(User.class));
                                            }
                                            latch.countDown();
                                        });
                            });
                }, 3000);
            }, 3000);
        });

        try {
            assertTrue("Test timed out", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
            assertFalse("Second user should not exist", secondUserExists.get());
            assertNotNull("First user should exist", firstUser.get());
            assertEquals("Email should match", "testuser@gmail.com", firstUser.get().getEmail());
        } catch (InterruptedException e) {
            fail("Test was interrupted");
        }
    }
}