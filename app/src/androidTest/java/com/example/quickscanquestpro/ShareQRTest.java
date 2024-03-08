package com.example.quickscanquestpro;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;

import org.junit.Test;

// TODO: This text needs to be completed
public class ShareQRTest extends MainActivityTest{

    @Test
    public void testUS01_04_01ShareEventQR() {

        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        createNewEvent();
        // Click on the event
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_dashboard_list)).atPosition(0).perform(click());
        // Click on the share button
        onView(withId(R.id.share_event_button)).perform(click());
        // Check if the share button is displayed
        onView(withId(R.id.share_promo_button)).perform(click());

        Intents.intending(hasAction(Intent.ACTION_SEND)).respondWith(result);

    }
}
