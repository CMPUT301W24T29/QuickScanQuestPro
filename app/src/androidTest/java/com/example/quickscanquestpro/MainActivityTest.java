package com.example.quickscanquestpro;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testUS01_01_01CreateEventAndQR(){
        createNewEvent();

        onView(withId(R.id.create_event_confirm_button)).perform(click());

        // if it successfully returns to the event list, the createQR function has run and generated a qr code for the event
        onView(withId(R.id.event_dashboard_list)).check(matches(isDisplayed()));
        // may fail once the event dashboard is properly created
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).check(matches(withSubstring("My Event Title")));
    }

    @Test
    public void testUS01_07_01ScanPromoQRDetails(){
        onView(isRoot()).perform(waitFor(7000));
        // the homescreen should appear, be granted camera privileges, scan the virtual QR inserted in %LocalAppData%\Android\Sdk\emulator\resources
        // and then finally transition to the event details page (of the test event with id 0)
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testUS02_06_01NoLogin(){
        onView(withId(R.id.navigation_profile)).perform(click());
    }

    @Test
    public void testUS02_02_03ChangeInfo(){
        onView(isRoot()).perform(waitFor(7000));
        onView(withId(R.id.navigation_profile)).perform(click());
        for(int i=0; i<20;i++) {
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
        createNewEvent();
        Espresso.closeSoftKeyboard();

        onView(withId(R.id.create_event_confirm_button)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).perform(click());
        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.event_title)).check(matches(withText("My Event Title")));
        onView(withId(R.id.event_description)).check(matches(withText("My Event Description")));
        onView(withId(R.id.event_location)).check(matches(withText("My Event Location")));
        onView(withId(R.id.event_date)).check(matches(withText("2024-07-18 at 12:30 until 2024-07-19 at 19:36")));

    }

    public static ViewAction waitFor(long delay) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override public String getDescription() {
                return "wait for " + delay + "milliseconds";
            }

            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delay);
            }
        };
    }

    public static void createNewEvent() {

        onView(isRoot()).perform(waitFor(2000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(1000));
        onView(withId(R.id.navigation_dashboard)).perform(click());
        onView(isRoot()).perform(waitFor(1000));

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
    }

}