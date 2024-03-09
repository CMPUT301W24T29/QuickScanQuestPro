package com.example.quickscanquestpro;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.example.quickscanquestpro.MainActivityTest.waitFor;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.anything;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES);

    @Before
    public void setUp() {
        init();
        FirebaseAuth.getInstance().signInAnonymously();
        // Wait at least 6 seconds for the app to initialize or load data
        onView(isRoot()).perform(waitFor(6000));
    }

    @After
    public void tearDown() {
        FirebaseAuth.getInstance().signOut();
        release();
    }

    @Test
    public void US020201uploadProfilePictureTest() {


        // Wait for EventDetails to fully load
        onView(isRoot()).perform(waitFor(5000));

        // Navigate to the profile section
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(4000));
        onView(withId(R.id.button_profile)).perform(click());

        // Prepare the result data for the gallery intent
        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.example.quickscanquestpro/drawable/testprofilepicture");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        // Click the button to upload a profile picture
        onView(withId(R.id.uploadProfilePictureButton)).perform(click());

        // Verify the ACTION_PICK intent was triggered
        intended(hasAction(Intent.ACTION_GET_CONTENT));

        // Wait for the image to be uploaded and processed
        onView(isRoot()).perform(waitFor(10000));

        // Check if the delete button is now displayed
        onView(withId(R.id.deleteProfilePictureButton)).check(matches(isDisplayed()));
    }


    @Test
    public void US020202deleteProfilePictureTest() {

        onView(isRoot()).perform(waitFor(5000));


        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(4000));
        onView(withId(R.id.button_profile)).perform(click());

        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.example.quickscanquestpro/drawable/testprofilepicture");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Intents.intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

        onView(withId(R.id.uploadProfilePictureButton)).perform(click());

        intended(hasAction(Intent.ACTION_GET_CONTENT));

        onView(isRoot()).perform(waitFor(7000));

        onView(withId(R.id.deleteProfilePictureButton)).perform(click());

        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.deleteProfilePictureButton)).check(matches(not(isDisplayed())));
    }

    public static ViewAction waitFor(long delay) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + delay + "milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delay);
            }
        };
    }

}

