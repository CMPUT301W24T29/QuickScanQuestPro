package com.example.quickscanquestpro;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.MultiFormatWriter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventCreationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventCreationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Event creatingEvent;

    public EventCreationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventCreationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventCreationFragment newInstance(String param1, String param2) {
        EventCreationFragment fragment = new EventCreationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        MainActivity mainActivity = (MainActivity) this.getActivity();
        this.creatingEvent = new Event(mainActivity.getNewEventID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) this.getActivity();

        // adds textwatchers that update the Event whenever text is changed
        EditText titleEditText = view.findViewById(R.id.edit_text_event_title);
        EditText descriptionEditText = view.findViewById(R.id.edit_text_event_description);
        EditText locationEditText = view.findViewById(R.id.edit_text_event_address);
        titleEditText.addTextChangedListener(getTextWatcher(titleEditText));
        descriptionEditText.addTextChangedListener(getTextWatcher(descriptionEditText));
        locationEditText.addTextChangedListener(getTextWatcher(locationEditText));

        // setting time pickers for start / end times
        TextView startTimeText = view.findViewById(R.id.text_event_start_time);
        startTimeText.setOnClickListener(v -> {
            new TimePickerFragment(startTimeText, creatingEvent).show(mainActivity.getSupportFragmentManager(), "startTimePicker");
        });

        TextView endTimeText = view.findViewById(R.id.text_event_end_time);
        endTimeText.setOnClickListener(v -> {
            new TimePickerFragment(endTimeText, creatingEvent).show(mainActivity.getSupportFragmentManager(), "endTimePicker");
        });

        // setting date pickers for start / end dates
        TextView startDateText = view.findViewById(R.id.text_event_start_date);
        startDateText.setOnClickListener(v -> {
            new DatePickerFragment(startDateText, creatingEvent).show(mainActivity.getSupportFragmentManager(), "startDatePicker");
        });

        TextView endDateText = view.findViewById(R.id.text_event_end_date);
        endDateText.setOnClickListener(v -> {
            new DatePickerFragment(endDateText, creatingEvent).show(mainActivity.getSupportFragmentManager(), "endDatePicker");
        });

        Button createButton = view.findViewById(R.id.create_event_confirm_button);
        createButton.setOnClickListener(v -> {
            mainActivity.setTestEvent(this.creatingEvent);

            // set active fragment to the event dashboard again
            EventDashboardFragment fragment = new EventDashboardFragment();
            FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.title_dashboard));
            fragmentTransaction.commit();
        });
    }

    private TextWatcher getTextWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int editId = editText.getId();
                if (editId == R.id.edit_text_event_title) {
                    creatingEvent.setTitle(editable.toString());
                } else if (editId == R.id.edit_text_event_description) {
                    creatingEvent.setDescription(editable.toString());
                } else if (editId == R.id.edit_text_event_address) {
                    creatingEvent.setLocation(editable.toString());
                }
            }
        };
    }
}