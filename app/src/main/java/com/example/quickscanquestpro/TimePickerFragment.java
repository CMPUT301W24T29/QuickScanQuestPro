package com.example.quickscanquestpro;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Calendar;

/**
 * A fragment that launches a time picker, either for the end time or start time of an event.
 * Stores this time in the event and validates the entry fields of the fragment it was called from.
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TextView timeText;
    private Event creatingEvent;
    private EventCreationFragment eventCreationFragment;

    public TimePickerFragment() {
        super();
    }

    /**
     * constructor for this fragment that stores the references to text, event, and creation fragment.
     * @param timeText TextView used to update the selected time
     * @param creatingEvent Event that is being updated with time for
     * @param eventCreationFragment Fragment that called this time picker to validate entry fields on
     */
    public TimePickerFragment(TextView timeText, Event creatingEvent, EventCreationFragment eventCreationFragment) {
        super();
        this.timeText = timeText;
        this.creatingEvent = creatingEvent;
        this.eventCreationFragment = eventCreationFragment;
    }

    /**
     * Called when the dialog is created, determines if this is an end time or start time, sets up default time for picker,
     * uses the time from the event if it had been set previously, and then creates the time picker.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return instance of the time picker with values set
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour;
        int minute;
        LocalTime eventTime;

        if (timeText.getId() == R.id.text_event_start_time) {
            // we are passing in the start time
            eventTime = creatingEvent.getStartTime();
        } else {
            // otherwise its the end time
            eventTime = creatingEvent.getEndTime();
        }

        if (eventTime != null) {
            // the event has previously been passed a time
            hour = eventTime.getHour();
            minute = eventTime.getMinute();
        } else {
            // Use the current time as the default values for the picker because its new
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    /**
     * Called when time is selected by the picker.
     * Sets the time in the event and validates the creation fragments entry fields and clears the error for that picker text.
     * @param view the TimePicker view used
     * @param hourOfDay int hour selected
     * @param minute int minutes selected
     */
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time the user picks.
        LocalTime newTime = LocalTime.of(hourOfDay, minute);
        if (timeText.getId() == R.id.text_event_start_time) {
            // we are passing in the start time
            creatingEvent.setStartTime(newTime);
        } else {
            // otherwise its the end time
            creatingEvent.setEndTime(newTime);
        }

        timeText.setText(newTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        timeText.setError(null);
        eventCreationFragment.validateEntryFields();
    }
}
