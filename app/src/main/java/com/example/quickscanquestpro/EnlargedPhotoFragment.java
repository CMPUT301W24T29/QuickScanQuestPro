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

public class EnlargedPhotoFragment extends Fragment {

    private static final String ARG_PHOTO_ID = "photoId";
    private static final String ARG_PHOTO_URL = "photoUrl";
    private static final String ARG_PHOTO_TYPE = "photoType"; // New argument to specify if it's a user or event photo

    private String photoId;
    private String photoUrl;
    private String photoType;

    public EnlargedPhotoFragment() {
        // Required empty public constructor
    }

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


    }
}
