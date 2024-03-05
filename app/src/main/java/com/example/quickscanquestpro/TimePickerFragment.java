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

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private TextView timeText;
    private Event creatingEvent;
    private EventCreationFragment eventCreationFragment;

    public TimePickerFragment() {
        super();
    }

    public TimePickerFragment(TextView timeText, Event creatingEvent, EventCreationFragment eventCreationFragment) {
        super();
        this.timeText = timeText;
        this.creatingEvent = creatingEvent;
        this.eventCreationFragment = eventCreationFragment;
    }

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
