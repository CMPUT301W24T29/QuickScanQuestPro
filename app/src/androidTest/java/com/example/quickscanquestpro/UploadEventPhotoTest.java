package com.example.quickscanquestpro;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class UploadEventPhotoTest extends MainActivityTest{

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        init();
        FirebaseAuth.getInstance().signInAnonymously();
        // Allow time for authentication to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
        release();
    }

    // In the case where the organizer uploads the photo when creating the event
    @Test
    public void testUS01_04_01UploadEventPhoto_1() {
        // Prepare the result data for the gallery intent
        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.example.quickscanquestpro/drawable/pork_ribs");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        createNewEvent();

        // Stub the intent that gets fired when picking an image from the gallery
        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        // Assume there's a button with ID banner_upload_button that the user clicks to upload a picture
        onView(withId(R.id.banner_upload_button)).perform(click());

        // Check if the action to pick an image was fired
        intended(hasAction(Intent.ACTION_GET_CONTENT));

        // Assuming there's a delay while the image is loaded
        try {
            Thread.sleep(2000); // It's better to use IdlingResource for synchronization
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.create_Event_Fragment_Scrollview)).perform(ViewActions.swipeUp());

        // Confirm creation of event
        onView(withId(R.id.create_event_confirm_button)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        // Check if the image is displayed in the event details
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        // Check if the image is displayed in the event details
        onView(withId(R.id.event_banner)).check(matches(isDisplayed()));
    }

    // In the case where the organizer uploads the photo after the event has already been created
    @Test
    public void testUS01_04_01UploadEventPhoto_2() {
        // Prepare the result data for the gallery intent
        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.example.quickscanquestpro/drawable/pork_ribs");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        createNewEvent();

        // Confirm creation of event
        onView(withId(R.id.create_event_confirm_button)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        // Stub the intent that gets fired when picking an image from the gallery
        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        // Assume there's a button with ID edit_banner_button that the user clicks to upload a picture
        onView(withId(R.id.edit_banner_button)).perform(click());

        // Check if the action to pick an image was fired
        intended(hasAction(Intent.ACTION_GET_CONTENT));

        // Assuming there's a delay while the image is loaded
        try {
            Thread.sleep(2000); // It's better to use IdlingResource for synchronization
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(isRoot()).perform(waitFor(1000));

        // Click the back button to return to the event dashboard
        onView(withId(R.id.back_button)).perform(click());

        onView(isRoot()).perform(waitFor(1000));

        // Check if the image is displayed in the event details
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        // Check if the image is displayed in the event details
        onView(withId(R.id.event_banner)).check(matches(isDisplayed()));
    }

}

