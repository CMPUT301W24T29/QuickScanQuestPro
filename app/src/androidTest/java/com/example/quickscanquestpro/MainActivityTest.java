package com.example.quickscanquestpro;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
<<<<<<< HEAD
=======
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
>>>>>>> 76b712d9276fb433355952910e2ebf955be1679a
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

<<<<<<< HEAD
=======
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
>>>>>>> 76b712d9276fb433355952910e2ebf955be1679a
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testUS01_01_01CreateEventAndQR() {
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_notification_title)).perform(ViewActions.typeText("My Event Title"));
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

        onView(withId(R.id.create_event_confirm_button)).perform(click());

        // if it successfully returns to the event list, the createQR function has run and generated a qr code for the event
        onView(withId(R.id.event_dashboard_list)).check(matches(isDisplayed()));
        // may fail once the event dashboard is properly created
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).check(matches(withSubstring("My Event Title")));
    }

    @Test
    public void testUS01_07_01ScanPromoQRDetails() {
        onView(isRoot()).perform(waitFor(7000));
        // the homescreen should appear, be granted camera privileges, scan the virtual QR inserted in %LocalAppData%\Android\Sdk\emulator\resources
        // and then finally transition to the event details page (of the test event with id 0)
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testUS02_06_01NoLogin() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_dashboard)).perform(click());
    }

    @Test
    public void testUS02_02_03ChangeInfo() {
        onView(isRoot()).perform(waitFor(7000));
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Users
        onView(withId(R.id.button_profile)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for the user list to load

        for (int i = 0; i < 20; i++) {
            onView(withId(R.id.fullNameInput)).perform(click()).perform(pressKey(KeyEvent.KEYCODE_DEL));
        }
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.fullNameInput)).perform(ViewActions.typeText("John Doe"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.homepageInput)).perform(ViewActions.typeText("www.johndoe.com"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.mobileNumberInput)).perform(ViewActions.typeText("123-456-7890"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.emailAddressInput)).perform(ViewActions.typeText("john@example.com"));
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Users
        onView(withId(R.id.button_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load

        onView(withText("John Doe")).check(matches(isDisplayed()));
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

    @Test
    public void testUS02_04_01ViewEventDetails() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        String eventTitle = UUID.randomUUID().toString();
        onView(withId(R.id.edit_notification_title)).perform(ViewActions.typeText(eventTitle));
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

        onView(withId(R.id.create_event_confirm_button)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.admin_button_manage_events)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        while (true) {
            onView(isRoot()).perform(waitFor(3000));
            try {
                onView(allOf(withText(eventTitle), isDescendantOfA(withId(R.id.admin_event_dashboard_list))))
                        .perform(click());
                break;
            } catch (Exception e) {
                onView(withId(R.id.admin_event_dashboard_list)).perform(ViewActions.swipeUp());
            }
        }


        onView(isRoot()).perform(waitFor(4000));
        onView(withId(R.id.event_title)).check(matches(withText(eventTitle)));
        onView(withId(R.id.event_description)).check(matches(withText("My Event Description")));
        onView(withId(R.id.event_location)).check(matches(withText("My Event Location")));
        onView(withId(R.id.event_date)).check(matches(withText("2024-08-18 at 12:30 until 2024-08-19 at 19:36")));

    }

    @Test
    public void testUS_02_08_01AttendeeBrowseEvents() {
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.event_dashboard_browse_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for navigation
        onView(withId(R.id.browse_events_dashboard_title)).check(matches(isDisplayed()));
        onView(withId(R.id.browse_events_dashboard_list)).check(matches(isDisplayed()));
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


    @Test
    public void testUS_04_02_01AdminRemoveProfile() {
        onView(isRoot()).perform(waitFor(7000)); // Wait to ensure the app is ready

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Users
        onView(withId(R.id.admin_button_manage_users)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load

        String firstItemIdentifier = "unique_text_of_first_item";

        onData(anything()).inAdapterView(withId(R.id.admin_profile_dashboard_list)).atPosition(0).onChildView(withId(R.id.admin_delete_button)).perform(click());

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.admin_profile_dashboard_list))
                .check(matches(not(hasDescendant(withText(firstItemIdentifier)))));

    }

    @Test
    public void testUS_04_06_01AdminBrowseProfile () {
        onView(isRoot()).perform(waitFor(5000)); // Wait to ensure the app is ready

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Users
        onView(withId(R.id.admin_button_manage_users)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load
        onView(withId(R.id.admin_profile_dashboard_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testUS_04_04_01AdminBrowseEvent() {
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Events
        onView(withId(R.id.admin_button_manage_events)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for the event list to load
        onView(withId(R.id.admin_event_dashboard_list)).check(matches(isDisplayed()));
    }

    @Test
    public void testUS_04_05_01AdminRemoveEvent() {
        onView(isRoot()).perform(waitFor(7000)); // Wait to ensure the app is ready

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        // Go to Manage Events
        onView(withId(R.id.admin_button_manage_events)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the event list to load

        String firstItemIdentifier = "unique_text_of_first_item";

        onData(anything()).inAdapterView(withId(R.id.admin_event_dashboard_list)).atPosition(0).onChildView(withId(R.id.admin_delete_button)).perform(click());

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.admin_event_dashboard_list))
                .check(matches(not(hasDescendant(withText(firstItemIdentifier)))));
    }


    @Test
    public void testUS_04_03_01AdminRemoveUserImage(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_button_view_images)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for the user list to load
        onView(withId(R.id.profiles_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.enlarged_photo)).check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.delete_button)).perform(click());



    }


    @Test
    public void testUS_04_03_01AdminRemoveEventImage(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_button_view_images)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for the user list to load
        onView(withId(R.id.events_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.enlarged_photo)).check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.delete_button)).perform(click());



    }

    @Test
    public void testUS_04_05_01AdminBrowseImage(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.admin_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_button_view_images)).perform(click());
        onView(isRoot()).perform(waitFor(5000)); // Wait for the user list to load
        onView(withId(R.id.events_recycler_view)).check(matches(isDisplayed()));
        onView(withId(R.id.profiles_recycler_view)).check(matches(isDisplayed()));
    }

    /**
     * This gets an activity reference from a running test, but you should not hold onto this reference as it may change
     * or be recreated. Try to call this every time you need something out of the activity.
     * @param activityScenarioRule the scenario from the scenarioTestRule
     * @return returns the Activity from the scenario, which can then be cast to (MainActivity) if needed
     */
    private <T extends Activity> T getActivityFromScenario(ActivityScenarioRule<T> activityScenarioRule) {
        AtomicReference<T> activityRef = new AtomicReference<>();
        activityScenarioRule.getScenario().onActivity(activityRef::set);
        return activityRef.get();
    }

    /**
     * This test requires a custom QR code that is loaded into the virtual camera that isnt used by an event yet, or it will fail.
     */
    @Test
    public void testUS01_01_02ReuseQR(){
        // begin event creation
        // fill all the boxes
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

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

        // click reuse qr checkin code
        onView(withId(R.id.reuse_checkin_button)).perform(click());
        onView(isRoot()).perform(waitFor(7000)); // Wait for scanning
        // verify that the checkin code actually got set, then overwrite it manually with "CUSTOMCHECKINQRTESTCODE"
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event creatingEvent = fragment.creatingEvent;
        assertNotNull(creatingEvent.getCustomCheckin());
        Log.d("testUS01_01_02", "custom checkin QR was originally set to " + creatingEvent.getCustomCheckin());
        creatingEvent.setCustomCheckin("CUSTOMCHECKINQRTESTCODE");
        // click reuse qr promo code
        onView(withId(R.id.reuse_promo_button)).perform(click());
        onView(isRoot()).perform(waitFor(7000)); // Wait for scanning
        // verify that the promo code actually got set, then overwrite it manually with "CUSTOMPROMOQRTESTCODE"
        mainActivity = getActivityFromScenario(scenario);
        fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        creatingEvent = fragment.creatingEvent;
        assertNotNull(creatingEvent.getCustomPromo());
        Log.d("testUS01_01_02", "custom promo QR was originally set to " + creatingEvent.getCustomPromo());
        creatingEvent.setCustomPromo("CUSTOMPROMOQRTESTCODE");
        // store the creating event id
        String creatingId = creatingEvent.getId();
        try {
            // create event
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000)); // Wait for creation
            // call database service function to get event with custom qr
            // verify that the ID matches the event that was being created and the two custom qr codes are set correctly
            DatabaseService databaseService = new DatabaseService();
            databaseService.getEventWithCustomQR("CUSTOMCHECKINQRTESTCODE", event -> {
                assertNotNull(event);
                assertEquals(event.getId(), creatingId);
                assertEquals(event.getCustomCheckin(), "CUSTOMCHECKINQRTESTCODE");
                assertEquals(event.getCustomPromo(), "CUSTOMPROMOQRTESTCODE");
            });
        } finally {
            // this code will make sure the event is deleted from the database after, even if the test fails
            DatabaseService databaseService = new DatabaseService();
            databaseService.getEventWithCustomQR("CUSTOMCHECKINQRTESTCODE", event -> {
                if (event != null) {
                    databaseService.deleteEvent(event);
                }
            });
        }




    }

}