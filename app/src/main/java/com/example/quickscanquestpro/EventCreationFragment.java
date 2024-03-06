package com.example.quickscanquestpro;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.MultiFormatWriter;

import org.w3c.dom.Text;

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
    private DatabaseService databaseService;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText locationEditText;
    private TextView startDateText;
    private TextView endDateText;
    private TextView startTimeText;
    private TextView endTimeText;
    private MainActivity mainActivity;
    private Button createButton;
    private ImageView posterImageView;

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
        // Initialize DatabaseService
        databaseService = new DatabaseService();
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
        mainActivity = (MainActivity) this.getActivity();

        posterImageView = view.findViewById(R.id.create_event_poster);
        // onclick listener for the button to upload a picture
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                Bitmap newBitmap = null;
                ContentResolver contentResolver = mainActivity.getContentResolver();
                try {
                    if(Build.VERSION.SDK_INT < 28) {
                        newBitmap = MediaStore.Images.Media.getBitmap(contentResolver, result);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, result);
                        newBitmap = ImageDecoder.decodeBitmap(source);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                creatingEvent.setEventBanner(newBitmap);
                posterImageView.setImageBitmap(newBitmap);
                posterImageView.setVisibility(View.VISIBLE);
            }
        });

        Button uploadImageButton = view.findViewById(R.id.banner_upload_button);
        uploadImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });

        // adds textwatchers that update the Event whenever text is changed
        titleEditText = view.findViewById(R.id.edit_text_event_title);
        descriptionEditText = view.findViewById(R.id.edit_text_event_description);
        locationEditText = view.findViewById(R.id.edit_text_event_address);
        
        titleEditText.addTextChangedListener(getTextWatcher(titleEditText));
        descriptionEditText.addTextChangedListener(getTextWatcher(descriptionEditText));
        locationEditText.addTextChangedListener(getTextWatcher(locationEditText));

        // setting time pickers for start / end times
        startTimeText = view.findViewById(R.id.text_event_start_time);
        startTimeText.setOnClickListener(v -> {
            new TimePickerFragment(startTimeText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "startTimePicker");
        });

        endTimeText = view.findViewById(R.id.text_event_end_time);
        endTimeText.setOnClickListener(v -> {
            new TimePickerFragment(endTimeText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "endTimePicker");
        });

        // setting date pickers for start / end dates
        startDateText = view.findViewById(R.id.text_event_start_date);
        startDateText.setOnClickListener(v -> {
            new DatePickerFragment(startDateText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "startDatePicker");
        });

        endDateText = view.findViewById(R.id.text_event_end_date);
        endDateText.setOnClickListener(v -> {
            new DatePickerFragment(endDateText, creatingEvent, this).show(mainActivity.getSupportFragmentManager(), "endDatePicker");
        });

        // final button that creates event and stores it
        createButton = view.findViewById(R.id.create_event_confirm_button);
        createButton.setOnClickListener(v -> {
            if (validateEntryFields()) {
//                mainActivity.setTestEvent(this.creatingEvent);
                String organizerId = mainActivity.getUser().getUserId();
                creatingEvent.setOrganizerId(organizerId);
                // create a new event in the database
                databaseService.addEvent(creatingEvent);
                Log.d("EventCreationFragment", "Event created: " + creatingEvent.toString() );
                // set active fragment to the event dashboard again
                EventDashboardFragment fragment = new EventDashboardFragment();
                FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, this.getString(R.string.title_dashboard));
                fragmentTransaction.commit();
            }
        });
        
        view.findViewById(R.id.reuse_checkin_button).setOnClickListener(v -> showReuseFragment("checkin"));
        // Reuse speaker button
        view.findViewById(R.id.reuse_promo_button).setOnClickListener(v -> showReuseFragment("promo"));

        // must do this at the end, last thing before showing user the fields
        validateEntryFields();
    }


    /**
     * Initiates the display of the ReuseQRFragment with specified content.
     * This method creates a bundle to carry the type of content (reuseType) to be displayed in the ReuseQRFragment.
     * It sets this bundle as arguments for the fragment, effectively informing the fragment about the content it needs to handle (e.g., check-in or promo).
     * After preparing the fragment with the necessary information, it performs a fragment transaction to replace the current view in the 'content' container with this fragment.
     * Optionally, the transaction is added to the back stack, allowing users to return to the previous state by pressing the back button.
     *
     * @param reuseType The specific content type the ReuseQRFragment should display or operate with, such as "CHECK_IN" or "PROMO".
     */

    private void showReuseFragment(String reuseType) {
        // Pass the reuse type to the ReuseFragment using arguments
        Bundle args = new Bundle();
        args.putString("REUSE_TYPE", reuseType);
        ReuseQRFragment reuseFragment = new ReuseQRFragment();
        reuseFragment.setArguments(args);

        // Perform the fragment transaction to display the ReuseFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.content, reuseFragment);
        transaction.addToBackStack(null); // Optional: Add transaction to back stack
        transaction.commit();
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
                validateEntryFields();
            }
        };
    }

    public Boolean validateEntryFields() {
        Boolean valid = true;

        if (titleEditText.getText().toString().length() <= 0) {
            titleEditText.setError("Must enter a title!");
            valid = false;
        }

        if (descriptionEditText.getText().toString().length() <= 0) {
            descriptionEditText.setError("Must enter a description!");
            valid = false;
        }

        if (locationEditText.getText().toString().length() <= 0) {
            locationEditText.setError("Must enter an address!");
            valid = false;
        }

        if (startDateText.getText().toString().length() <= 0) {
            startDateText.setError("Must enter a start date!");
            valid = false;
        } else {
            startDateText.setError(null);
        }

        if (endDateText.getText().toString().length() <= 0) {
            endDateText.setError("Must enter a end date!");
            valid = false;
        } else {
            endDateText.setError(null);
        }

        if (startTimeText.getText().toString().length() <= 0) {
            startTimeText.setError("Must enter a start time!");
            valid = false;
        } else {
            startTimeText.setError(null);
        }

        if (endTimeText.getText().toString().length() <= 0) {
            endTimeText.setError("Must enter a end time!");
            valid = false;
        } else {
            endTimeText.setError(null);
        }
        
        if (!valid) {
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(true);
        }
        
        return valid;
    }
}