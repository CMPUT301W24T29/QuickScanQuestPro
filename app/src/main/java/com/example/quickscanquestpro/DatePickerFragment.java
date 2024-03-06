package com.example.quickscanquestpro;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView dateText;
    private Event creatingEvent;
    private EventCreationFragment eventCreationFragment;

    public DatePickerFragment() {
        super();
    }

    public DatePickerFragment(TextView dateText, Event creatingEvent, EventCreationFragment eventCreationFragment) {
        super();
        this.dateText = dateText;
        this.creatingEvent = creatingEvent;
        this.eventCreationFragment = eventCreationFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year;
        int month;
        int day;
        LocalDate eventDate;

        if (dateText.getId() == R.id.text_event_start_date) {
            // we are passing in the start date
            eventDate = creatingEvent.getStartDate();
        } else {
            // otherwise its the end time
            eventDate = creatingEvent.getEndDate();
        }

        if (eventDate != null) {
            // the event has previously been passed a date
            year = eventDate.getYear();
            month = eventDate.getMonthValue();
            day = eventDate.getDayOfMonth();
        } else {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the time the user picks.
        LocalDate newDate = LocalDate.of(year, month, day);
        if (dateText.getId() == R.id.text_event_start_date) {
            // we are passing in the start time
            creatingEvent.setStartDate(newDate);
        } else {
            // otherwise its the end time
            creatingEvent.setEndDate(newDate);
        }

        dateText.setText(newDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        dateText.setError(null);
        eventCreationFragment.validateEntryFields();
    }
}
