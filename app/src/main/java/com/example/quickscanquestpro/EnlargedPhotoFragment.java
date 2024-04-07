package com.example.quickscanquestpro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

/**
 * A Fragment subclass for displaying an enlarged photo with an option to delete it.
 * This class is responsible for handling the presentation of a single photo in an enlarged view
 * and allows the user to delete the photo from the database.
 */

public class EnlargedPhotoFragment extends Fragment {

    private static final String ARG_PHOTO_ID = "photoId";
    private static final String ARG_PHOTO_URL = "photoUrl";
    private static final String ARG_PHOTO_TYPE = "photoType";

    private String photoId;
    private String photoUrl;
    private String photoType;

    public EnlargedPhotoFragment() {
    }

    /**
     * Creates a new instance of EnlargedPhotoFragment with necessary arguments.
     *
     * @param photoId Unique identifier for the photo to be displayed.
     * @param photoUrl URL of the photo to be displayed.
     * @param photoType The type of photo (user or event) to determine specific logic for deletion.
     * @return A new instance of fragment EnlargedPhotoFragment.
     */

    public static EnlargedPhotoFragment newInstance(String photoId, String photoUrl, String photoType) {
        EnlargedPhotoFragment fragment = new EnlargedPhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_ID, photoId);
        args.putString(ARG_PHOTO_URL, photoUrl);
        args.putString(ARG_PHOTO_TYPE, photoType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoId = getArguments().getString(ARG_PHOTO_ID);
            photoUrl = getArguments().getString(ARG_PHOTO_URL);
            photoType = getArguments().getString(ARG_PHOTO_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enlarged_photo, container, false);
    }

    /**
     * Sets up the enlarged photo view and delete button logic after the view is created.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView enlargedPhoto = view.findViewById(R.id.enlarged_photo);
        Button deleteButton = view.findViewById(R.id.delete_button);
        int placeholder = "user".equals(photoType) ? R.drawable.default_profile : R.drawable.default_event_profile;

        Glide.with(this).load(photoUrl).error(placeholder).into(enlargedPhoto);

        deleteButton.setOnClickListener(v -> {
            DatabaseService databaseService = new DatabaseService();
            if ("user".equals(photoType)) {
                databaseService.deleteUserProfilePicture(photoId, new DatabaseService.OnProfilePictureDelete() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "User photo deleted successfully", Toast.LENGTH_SHORT).show();
                        if (getFragmentManager() != null) {
                            Bundle result = new Bundle();
                            result.putString("photoId", photoId);
                            result.putString("photoType", photoType);
                            result.putBoolean("isDeleted", true);
                            getParentFragmentManager().setFragmentResult("updatePhotoResult", result);
                            getFragmentManager().popBackStack();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting user photo", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if ("event".equals(photoType)) {
                databaseService.deleteEventPhoto(photoId, new DatabaseService.OnProfilePictureDelete() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Event photo deleted successfully", Toast.LENGTH_SHORT).show();
                        if (getFragmentManager() != null) {
                            Bundle result = new Bundle();
                            result.putString("photoId", photoId);
                            result.putString("photoType", photoType);
                            result.putBoolean("isDeleted", true);
                            getParentFragmentManager().setFragmentResult("updatePhotoResult", result);
                            getFragmentManager().popBackStack();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Error deleting event photo", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    view.setOnTouchListener((v, event)->true);
    }
}
