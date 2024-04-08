package com.example.quickscanquestpro;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Checks;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule permissionCoarse = GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionFine = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

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
    }

    @Test
    public void testUS02_02_03ChangeInfo() {
        onView(isRoot()).perform(waitFor(7000));
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        // Go to Manage Users
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
        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText(eventTitle));
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

        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event creatingEvent = fragment.creatingEvent;

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(2000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(2000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("testUS02_04_01ViewEventDetails")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.event_title)).check(matches(withText("testUS02_04_01ViewEventDetails")));
            onView(withId(R.id.event_description)).check(matches(withText("My Event Description")));
            onView(withId(R.id.event_location)).check(matches(withText("My Event Location")));
            onView(withId(R.id.event_date)).check(matches(withText("2024-08-18 at 12:30 until 2024-08-19 at 19:36")));
        } finally {
            DatabaseService databaseService = new DatabaseService();
            databaseService.deleteEvent(creatingEvent);
        }
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


        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_profile_user_search_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load

        String firstItemIdentifier = "unique_text_of_first_item";

        onView(withId(R.id.admin_profile_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, MyViewActions.clickChildViewWithId(R.id.delete_button)));

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.admin_profile_dashboard_list))
                .check(matches(not(hasDescendant(withText(firstItemIdentifier)))));

    }


    public static class MyViewActions {
        public static ViewAction clickChildViewWithId(final int id) {
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return null;
                }

                @Override
                public String getDescription() {
                    return "Click on a child view with specified ID.";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    View v = view.findViewById(id);
                    v.performClick();
                }
            };
        }
    }


    @Test
    public void testUS_04_06_01AdminBrowseProfile () {
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_profile_user_search_button)).perform(click());

        onView(withId(R.id.admin_profile_dashboard_list)).check(matches(isDisplayed()));


    }

    @Test
    public void testUS_04_04_01AdminBrowseEvent() {
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_event_search_button)).perform(click());

        onView(withId(R.id.browse_events_dashboard_list)).check(matches(isDisplayed()));


    }


    @Test
    public void testUS_04_05_01AdminRemoveEvent() {
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_event_search_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load

        String firstItemIdentifier = "unique_text_of_first_item";

        onView(withId(R.id.browse_events_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, MyViewActions.clickChildViewWithId(R.id.delete_button)));

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.browse_events_dashboard_list))
                .check(matches(not(hasDescendant(withText(firstItemIdentifier)))));

    }


    @Test
    public void testUS_04_03_01AdminRemoveUserImage(){
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_image_search_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load


        onView(isRoot()).perform(waitFor(5000)); // Wait for the user list to load
        onView(withId(R.id.profiles_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.enlarged_photo)).check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.delete_button)).perform(click());



    }


    @Test
    public void testUS_04_03_01AdminRemoveEventImage(){
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_image_search_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load


        onView(isRoot()).perform(waitFor(5000)); // Wait for the user list to load
        onView(withId(R.id.events_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.enlarged_photo)).check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.delete_button)).perform(click());



    }

    @Test
    public void testUS_04_05_01AdminBrowseImage(){
        onView(isRoot()).perform(waitFor(10000)); // Wait to ensure the app is ready
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        DatabaseService databaseService = new DatabaseService();
        databaseService.enableAdmin(user.getUserId());
        user = mainActivity.getUser();
        user.setAdmin(true);
        mainActivity.setUser(user);

        onView(isRoot()).perform(waitFor(4000)); // wait for database update

        // Navigate to the Admin Dashboard
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        onView(withId(R.id.event_dashboard_admin_expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));

        // Go to Manage Users
        onView(withId(R.id.event_dashboard_admin_image_search_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for the user list to load
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

    @Test
    public void testUS_01_10_01EventSignedUpList(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.event_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("My Test Event Title"));
        onView(withId(R.id.edit_text_event_description)).perform(ViewActions.typeText("My Test Event Description"));
        onView(withId(R.id.edit_text_event_address)).perform(ViewActions.typeText("My Test Event Location"));
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
        onView(isRoot()).perform(waitFor(3000));

        // Click on the third item in the RecyclerView
        onView(withId(R.id.event_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(isRoot()).perform(waitFor(3000));

        onView(withText("My Test Event Title")).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.signup_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.signup_list)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.signup_list_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testUS_01_11_01LimitSignups(){

        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.event_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("My Test Event Title"));
        onView(withId(R.id.edit_text_event_description)).perform(ViewActions.typeText("My Test Event Description"));
        onView(withId(R.id.edit_text_event_address)).perform(ViewActions.typeText("My Test Event Location"));
        onView(withId(R.id.edit_text_signups)).perform(ViewActions.typeText("10"));
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
        onView(isRoot()).perform(waitFor(3000));

        onView(withId(R.id.event_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(isRoot()).perform(waitFor(5000));

        onView(withText("My Test Event Title")).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withText("10"))
                .check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000));
    }

    @Test
    public void testUS_02_07_01Signup(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.event_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_dashboard_browse_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        // click first item in browse events
        onView(withId(R.id.browse_events_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.signup_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));
    }

    @Test
    public void testUS_02_09_01UserSignupList(){
        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.event_dashboard_title)).check(matches(isDisplayed()));

        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("My Test Event Title"));
        onView(withId(R.id.edit_text_event_description)).perform(ViewActions.typeText("My Test Event Description"));
        onView(withId(R.id.edit_text_event_address)).perform(ViewActions.typeText("My Test Event Location"));
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
        onView(isRoot()).perform(waitFor(3000));

        // Click on the third item in the RecyclerView
        onView(withId(R.id.event_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));
        onView(isRoot()).perform(waitFor(3000));

        onView(withText("My Test Event Title")).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.expand_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.signup_button)).perform(click());
        onView(isRoot()).perform(waitFor(5000));

        onView(withId(R.id.back_button)).perform(click());
        onView(isRoot()).perform(waitFor(2000));


        onView(withId(R.id.event_dashboard_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(isRoot()).perform(waitFor(5000));
        onView(withText("My Test Event Title"))
                .check(matches(isDisplayed()));
    }
    /**
     * This test requires a valid CHECKIN qr loaded into the scanner
     * This test also requires android location be enabled in the settings before running.
     */
    @Test
    public void testUS03_02_01Geolocation(){
        // test that when checking in with User bool disabled, event stored checkin but no location
        // default for a test is a new user, which has location disabled in user
        onView(isRoot()).perform(waitFor(7000)); // Wait for scanning of checkin
        // check that we are in event details fragment
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));

        // get the id of the user, the displayed event from eventdetails, and check the database has a checkin from them
        MainActivity mainActivity = getActivityFromScenario(scenario);
        User user = mainActivity.getUser();
        String userId = user.getUserId();

        EventDetailsFragment fragment = (EventDetailsFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventDetailsFragment");
        String eventId = fragment.event.getId();

        DatabaseService databaseService = new DatabaseService();
        final boolean[] continueTest = {false};
        String finalUserId = userId;
        EventDetailsFragment finalFragment = fragment;
        String finalEventId = eventId;
        DatabaseService finalDatabaseService = databaseService;
        databaseService.getEvent(eventId, event -> {
            try {
                assertNotNull(event);
                ArrayList<CheckIn> checkins = event.getCheckIns();
                assertNotNull(checkins);
                CheckIn foundCheckin = null;
                for (CheckIn checkin: checkins) {
                    if (Objects.equals(finalUserId, checkin.getUserId())) {
                        foundCheckin = checkin;
                        break;
                    }
                }
                assertNotNull(foundCheckin);
                continueTest[0] = true;
                // test that when checking in with User bool enabled, event stored checkin WITH a location
            } finally {
                Log.d("geotest",String.valueOf(finalFragment.event.getCheckIns()));
                finalDatabaseService.clearEventCheckins(finalEventId);
            }
        });

        while(!continueTest[0]) {
            // wait until the previous test is done :/
        }

        onView(withId(R.id.navigation_profile)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.geolocationSwitch)).perform(scrollTo(), click());
        onView(isRoot()).perform(waitFor(10000)); // Wait for location grab
        try {
            onView(withId(R.id.geolocationSwitch)).check(matches(isEnabled()));
        } catch (Exception e) {
            onView(isRoot()).perform(waitFor(10000)); // Wait for location longer
            onView(withId(R.id.geolocationSwitch)).check(matches(isEnabled()));
        }

        onView(withId(R.id.navigation_qr_scanner)).perform(click());
        onView(isRoot()).perform(waitFor(7000)); // Wait for navigation and scan
        // check that we are in event details fragment
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
        // get the id of the user, the displayed event from eventdetails, and check the database has a checkin from them
        mainActivity = getActivityFromScenario(scenario);
        user = mainActivity.getUser();
        userId = user.getUserId();

        fragment = (EventDetailsFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventDetailsFragment");
        eventId = fragment.event.getId();

        databaseService = new DatabaseService();
        String finalUserId2 = userId;
        EventDetailsFragment finalFragment2 = fragment;
        String finalEventId2 = eventId;
        DatabaseService finalDatabaseService2 = databaseService;
        databaseService.getEvent(eventId, event -> {
            try {
                assertNotNull(event);
                ArrayList<CheckIn> checkins = event.getCheckIns();
                assertNotNull(checkins);
                CheckIn foundCheckin = null;
                for (CheckIn checkin: checkins) {
                    if (Objects.equals(finalUserId2, checkin.getUserId())) {
                        foundCheckin = checkin;
                        break;
                    }
                }
                assertNotNull(foundCheckin);
                assertNotNull(foundCheckin.getCheckInLocation());
                assertNotEquals(foundCheckin.getCheckInLocation(), "");
            } finally {
                Log.d("geotest",String.valueOf(finalFragment2.event.getCheckIns()));
                finalDatabaseService2.clearEventCheckins(finalEventId2);
            }
        });

    }

    @Test
    public void testUS01_02_01ViewAttendees() {

        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        // create new event
        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("Test US01_02_01ViewAttendees"));
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

        DatabaseService databaseService = new DatabaseService();
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event event = fragment.creatingEvent;
        String eventId = event.getId();

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(2000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US01_02_01ViewAttendees")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check in an attendee
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));
            // click on view attendees
            onView(withId(R.id.expand_button)).perform(click());
            onView(withId(R.id.view_attendees_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // test to see if attendees are shown correctly
            onView(withText("Attendee test user")).check(matches(isDisplayed()));
            onView(withText("Check-ins: 1")).check(matches(isDisplayed()));

        } finally {
            // delete event
            databaseService.deleteEvent(event);
        }
    }

    @Test
    public void testUS01_09_01ViewAttendeeCheckInCount() {

        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(2000)); // Wait for navigation

        // create new event
        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("Test US01_09_01AttendeeCount"));
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

        DatabaseService databaseService = new DatabaseService();
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event event = fragment.creatingEvent;
        String eventId = event.getId();

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(2000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US01_09_01AttendeeCount")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check in an attendee
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(1000));
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));
            // click on view attendees
            onView(withId(R.id.expand_button)).perform(click());
            onView(withId(R.id.view_attendees_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // test to see if attendees are shown correctly
            onView(withText("Attendee test user")).check(matches(isDisplayed()));
            onView(withText("Check-ins: 2")).check(matches(isDisplayed()));

        } finally {
            // delete new event
            databaseService.deleteEvent(event);
        }
    }

    @Test
    public void testUS_01_03_01SendNotificationsToAttendees(){
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation

        // create new event
        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("Test US01_03_01Notification"));
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

        DatabaseService databaseService = new DatabaseService();
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event event = fragment.creatingEvent;
        String eventId = event.getId();

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(4000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US01_03_01Notification")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check in an attendee
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(1000));
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));

            // click on view attendees
            onView(withId(R.id.expand_button)).perform(click());
            onView(withId(R.id.view_attendees_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // click on view attendees
            onView(withId(R.id.expand_attendee_button)).perform(click());
            onView(withId(R.id.announcement_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // send a title and body
            onView(withId(R.id.edit_notification_title)).perform(ViewActions.typeText("Test Title"));
            onView(withId(R.id.edit_notification_body)).perform(ViewActions.typeText("Test Body"));
            Espresso.closeSoftKeyboard();
            onView(isRoot()).perform(waitFor(4000));


            // send the notification
            onView(withId(R.id.create_notification_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check if it went back to attendeeFragment
            onView(withText("Attendee test user")).check(matches(isDisplayed()));
            onView(withText("Check-ins: 2")).check(matches(isDisplayed()));

            // go back to event dashboard
            onView(withId(R.id.navigation_dashboard)).perform(click());
            onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
            onView(withId(R.id.navigation_dashboard)).perform(click());
            onView(isRoot()).perform(waitFor(4000)); // Wait for navigation

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(4000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US01_03_01Notification")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            onView(withId(R.id.event_details_fragment)).perform(ViewActions.swipeUp());
            onView(isRoot()).perform(waitFor(4000));

            //check if text matches
            onView(withText("Test Title: Test Body")).check(matches(isDisplayed()));

    }
        finally {
            onView(isRoot()).perform(waitFor(4000));
            // delete new event
            databaseService.deleteEvent(event);
        }
    }

    @Test
    public void testUS_02_03_01ReceiveAlert(){
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation

        // create new event
        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("Test US02_03_01ReceiveAlert"));
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

        DatabaseService databaseService = new DatabaseService();
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event event = fragment.creatingEvent;
        String eventId = event.getId();

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(4000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US02_03_01ReceiveAlert")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check in an attendee
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(1000));
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));

            // click on view attendees
            onView(withId(R.id.expand_button)).perform(click());
            onView(withId(R.id.view_attendees_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // click on view attendees
            onView(withId(R.id.expand_attendee_button)).perform(click());
            onView(withId(R.id.announcement_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // send a title and body
            onView(withId(R.id.edit_notification_title)).perform(ViewActions.typeText("Test Title"));
            onView(withId(R.id.edit_notification_body)).perform(ViewActions.typeText("Test Body"));
            Espresso.closeSoftKeyboard();
            onView(isRoot()).perform(waitFor(4000));


            // send the notification
            onView(withId(R.id.create_notification_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check if it went back to attendeeFragment
            onView(withText("Attendee test user")).check(matches(isDisplayed()));
            onView(withText("Check-ins: 2")).check(matches(isDisplayed()));

            // go back to event dashboard
            onView(withId(R.id.navigation_dashboard)).perform(click());
            onView(isRoot()).perform(waitFor(2000)); // Wait for navigation
            onView(withId(R.id.navigation_dashboard)).perform(click());
            onView(isRoot()).perform(waitFor(4000)); // Wait for navigation

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(4000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US02_03_01ReceiveAlert")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            onView(withId(R.id.event_details_fragment)).perform(ViewActions.swipeUp());
            onView(isRoot()).perform(waitFor(4000));

            //check if text matches
            onView(withText("Test Title: Test Body")).check(matches(isDisplayed()));

        }
        finally {
            onView(isRoot()).perform(waitFor(4000));
            // delete new event
            databaseService.deleteEvent(event);
        }
    }

    @Test
    public void testUS01_05_01LiveCount() {
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(4000)); // Wait for navigation

        // create new event
        onView(withId(R.id.event_dashboard_create_button)).perform(click());

        onView(withId(R.id.edit_text_event_title)).perform(ViewActions.typeText("Test US01_05_01LiveCount"));
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

        DatabaseService databaseService = new DatabaseService();
        MainActivity mainActivity = getActivityFromScenario(scenario);
        EventCreationFragment fragment = (EventCreationFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("EventCreation");
        Event event = fragment.creatingEvent;
        String eventId = event.getId();

        try {
            onView(withId(R.id.create_event_confirm_button)).perform(click());
            onView(isRoot()).perform(waitFor(3000));

            // look at organized events to find newly created event
            onView(withId(R.id.event_dashboard_list)).perform(actionOnItemAtPosition(2, click()));
            onView(isRoot()).perform(waitFor(2000));

            // click on newly made event
            onView(allOf(withId(R.id.events_rv), hasDescendant(withText("Test US01_05_01LiveCount")))).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            // check in an attendee
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(2000));
            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));
            // click on view attendees
            onView(withId(R.id.expand_button)).perform(click());
            onView(withId(R.id.view_attendees_button)).perform(click());
            onView(isRoot()).perform(waitFor(4000));

            databaseService.recordCheckIn(eventId, "9b4dd42c-7de3-4d76-a96d-fde4958e7104", "location");
            onView(isRoot()).perform(waitFor(4000));

            // test to see if attendees are shown correctly
            onView(withText("Attendee test user")).check(matches(isDisplayed()));
            onView(withText("Check-ins: 3")).check(matches(isDisplayed()));

        } finally {
            onView(isRoot()).perform(waitFor(4000));
            // delete new event
            databaseService.deleteEvent(event);
        }
    }

}