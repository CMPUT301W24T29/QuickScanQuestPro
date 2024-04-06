package com.example.quickscanquestpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class GeolocationService {
    private Fragment fragment;
    private GeolocationRegisteredFragment fragmentResultGetter;
    GeolocationRequestComplete callback;

    /**
     * Callback for when location is found / for error because permissions not granted or location not enabled
     */
    public interface GeolocationRequestComplete {
        void geolocationRequestComplete(boolean success, String result);
    }

    public interface GeolocationRegisteredFragment {
        ActivityResultLauncher<String[]> getLocPermLauncher();
        ActivityResultLauncher<IntentSenderRequest> getLocResolutionIntentSender();
    }

    public GeolocationService(Fragment fragment, GeolocationRequestComplete callback, GeolocationRegisteredFragment fragmentResultGetter) {
        // these are all the same thing, but java SUCKS!!!! and you have to pretend theyre all different idk man
        this.fragment = fragment;
        this.callback = callback;
        this.fragmentResultGetter = fragmentResultGetter;
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        // check if permissions are granted, if not the permissions launcher will either trigger the callback with an error (if theyre denied explicitly) or call getLocation() again when they ARE granted
        if (!locationGranted()) {
            return;
        }

        // check if location is enabled, if not then the settings client will either trigger the callback with an error or call getLocation() again when it IS enabled
        if (!locationEnabled()) {
            return;
        }

        // permissions granted and location enabled, so let's get the location
        // fused location client will use either GPS (when available) or nearby networks (when indoors) to get device's last known location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.getContext());
        // builder for requesting just the current location, once
        CurrentLocationRequest.Builder reqBuilder = new CurrentLocationRequest.Builder();
        // request high power location be grabbed
        reqBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        // 30 seconds max age for historical locations allowed
        reqBuilder.setMaxUpdateAgeMillis(30000);
        Toast.makeText(fragment.getContext(), "Checking location...", Toast.LENGTH_SHORT).show();
        fusedLocationClient.getCurrentLocation(reqBuilder.build(), null).addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    callback.geolocationRequestComplete(false, "Failed to find current location.");
                } else {
                    Log.d("GeolocationService","Successfully retrieved lat and long: " + location.getLatitude() + "," + location.getLongitude());
                    callback.geolocationRequestComplete(true, location.getLatitude() + "," + location.getLongitude());
                }
            }
        });
    }

    private boolean locationGranted() {

        if (ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permList = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            fragmentResultGetter.getLocPermLauncher().launch(permList);
            return false;
        }

//        if (ActivityCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            locPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
//            return false;
//        }

        return true;

    }

    private boolean locationEnabled() {
        LocationManager locationManager = (LocationManager) fragment.getActivity().getSystemService(Context.LOCATION_SERVICE);
        // if either GPS or network location providers are disabled, we request the user allow GPS be enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            SettingsClient mSettingsClient;
            LocationSettingsRequest mLocationSettingsRequest;

            final int REQUEST_CHECK_SETTINGS = 214;

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            LocationRequest.Builder locBuilder = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0);
            builder.addLocationRequest(locBuilder.build());
            builder.setAlwaysShow(true);

            mLocationSettingsRequest = builder.build();
            mSettingsClient = LocationServices.getSettingsClient(fragment.getActivity());

            mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // location should be enabled in some form now.
                        getLocation();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    IntentSenderRequest request = new IntentSenderRequest.Builder(rae.getResolution()).setFillInIntent(new Intent()).setFlags(0, 0).build();
                                    fragmentResultGetter.getLocResolutionIntentSender().launch(request);
                                } catch (Exception sie) {
                                    Log.e("GeolocationService","Failure executing settings request.");
                                    callback.geolocationRequestComplete(false, "Please enable location in settings.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.e("GeolocationService","Location must be enabled in settings, can't be changed via app.");
                                callback.geolocationRequestComplete(false, "Please enable location in settings.");
                        }
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.e("GeolocationService","User cancelled request to enable location setting.");
                        callback.geolocationRequestComplete(false, "Please enable location in settings.");
                    }
                });
            // return false, we'll call getLocation() again or callback with error with the listeners
            return false;
        }
        // either gps or network provider is enabled
        return true;
    }

    public void locationPermissionResultHandler(Map<String, Boolean> result) {
        if (Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION)) || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
            // granted one of the permissions, so this will call locationEnabled() next
            getLocation();
        } else {
            // permissions were denied
            // callback with success = false and result = "Location permissions denied: cannot record checkin location."
            callback.geolocationRequestComplete(false, "Location permissions were denied. You must grant permission via app settings.");
        }
    }

    public void locationEnabledResolutionHandler(ActivityResult result){
        if (result.getResultCode() == fragment.getActivity().RESULT_OK) {
            // call again to ensure the
            getLocation();
        } else {
            // permissions were denied
            // callback with success = false and result = "Location permissions denied: cannot record checkin location."
            callback.geolocationRequestComplete(false, "Please enable location via system settings.");
        }
    }

}
