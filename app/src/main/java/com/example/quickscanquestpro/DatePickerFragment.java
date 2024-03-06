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

/**
 * A fragment that launches a date picker, either for the end date or start date of an event.
 * Stores this date in the event and validates the entry fields of the fragment it was called from.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextView dateText;
    private Event creatingEvent;
    private EventCreationFragment eventCreationFragment;

    public DatePickerFragment() {
        super();
    }

    /**
     * constructor for this fragment that stores the references to text, event, and creation fragment.
     * @param dateText TextView used to update the selected date
     * @param creatingEvent Event that is being updated with date for
     * @param eventCreationFragment Fragment that called this date picker to validate entry fields on
     */
    public DatePickerFragment(TextView dateText, Event creatingEvent, EventCreationFragment eventCreationFragment) {
        super();
        this.dateText = dateText;
        this.creatingEvent = creatingEvent;
        this.eventCreationFragment = eventCreationFragment;
    }

    /**
     * Called when the dialog is created, determines if this is an end date or start date, sets up default date for picker,
     * uses the date from the event if it had been set previously, and then creates the date picker.
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return instance of the date picker with values set
     */
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

    /**
     * Called when date is selected by the picker.
     * Sets the date in the event and validates the creation fragments entry fields and clears the error for that picker text.
     * @param view the DatePicker view used
     * @param year int year selected
     * @param month int month selected
     * @param day int day selected
     */
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
