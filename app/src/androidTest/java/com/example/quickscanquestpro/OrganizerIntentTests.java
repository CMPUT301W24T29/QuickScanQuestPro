package com.example.quickscanquestpro;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerIntentTests {

    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
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
        Intents.release();
    }

    // In the case where the organizer uploads the photo when creating the event
    @Test
    public void testUS01_04_01UploadEventPhoto() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());

        // Prepare the result data for the gallery intent
        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.example.quickscanquestpro/drawable/pork_ribs");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        onView(withId(R.id.navigation_dashboard)).perform(click());

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("My Event Title"));
        onView(withId(R.id.edit_text_event_description)).perform(ViewActions.typeText("My Event Description"));
        onView(withId(R.id.edit_text_event_address)).perform(ViewActions.typeText("My Event Location"));
        Espresso.closeSoftKeyboard();

        setDate(R.id.text_event_start_date, 2024, 8, 18);
        Espresso.closeSoftKeyboard();
        setDate(R.id.text_event_end_date, 2024, 8, 19);
        Espresso.closeSoftKeyboard();

        setTime(R.id.text_event_start_time, 12, 30);
        Espresso.closeSoftKeyboard();
        setTime(R.id.text_event_end_time, 19, 36);
        Espresso.closeSoftKeyboard();

        // Stub the intent that gets fired when picking an image from the gallery
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result);

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

        onView(withId(R.id.create_event_poster)).check(matches(isDisplayed()));

        onView(withId(R.id.create_Event_Fragment_Scrollview)).perform(ViewActions.swipeUp());

        // Confirm creation of event
        onView(withId(R.id.create_event_confirm_button)).perform(click());

    }

    @Test
    public void testUS01_06_01ShareEventQR() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        String eventTitle = UUID.randomUUID().toString();
        onView(isRoot()).perform(waitFor(4000));
        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText(eventTitle), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_event_description)).perform(ViewActions.typeText("My Event Description"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.edit_text_event_address)).perform(ViewActions.typeText("My Event Location"), ViewActions.closeSoftKeyboard());

        // Assuming setDate and setTime are correctly implemented to interact with your date/time pickers
        setDate(R.id.text_event_start_date, 2024, 8, 18);
        setDate(R.id.text_event_end_date, 2024, 8, 19);
        setTime(R.id.text_event_start_time, 12, 30);
        setTime(R.id.text_event_end_time, 19, 36);

        onView(withId(R.id.create_event_confirm_button)).perform(click());

        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.admin_button_manage_events)).perform(click());

        // Scroll to the event with the specific title and click on it
        // Replace this scrolling mechanism with a more reliable way if available
        onView(allOf(withText(eventTitle), isDescendantOfA(withId(R.id.browse_events_dashboard_list))))
                .perform(click());

        // Assert that the sharing intent is triggered
        onView(withId(R.id.share_event_button)).perform(click());

        // Stubbing the Intent to prevent the chooser from actually launching
        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        onView(withId(R.id.share_promo_button)).perform(click());

        // Verify the chooser intent was triggered
        intended(hasAction(Intent.ACTION_CHOOSER));
    }

    public static void setDate(int datePickerLaunchViewId, int year, int monthOfYear, int dayOfMonth) {
        onView(withId(datePickerLaunchViewId)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, monthOfYear, dayOfMonth));
        onView(withId(android.R.id.button1)).perform(click());
    }

    public static void setTime(int timePickerLaunchViewId, int hour, int minute) {
        onView(withId(timePickerLaunchViewId)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(hour, minute));
        onView(withId(android.R.id.button1)).perform(click());
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

