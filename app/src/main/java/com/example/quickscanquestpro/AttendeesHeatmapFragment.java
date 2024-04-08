package com.example.quickscanquestpro;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AttendeesHeatmapFragment extends Fragment implements OnMapReadyCallback {
    GoogleMap map;
    Event event;

    public AttendeesHeatmapFragment() {
        // Required empty public constructor
    }

    public AttendeesHeatmapFragment(Event event) {
        this.event = event;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_attendees_heatmap, container, false);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        return v;

    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (map == null) {
            map = googleMap;

            Geocoder coder = new Geocoder(getContext());
            List<Address> address;
            LatLng p1;

            try {
                // May throw an IOException
                address = coder.getFromLocationName(event.getLocation(), 1);
                if (address == null || address.isEmpty()) {
                    p1 = new LatLng(Double.parseDouble("53.52676800331974"), Double.parseDouble("-113.52714795529633") );
                } else {
                    Address location = address.get(0);
                    p1 = new LatLng(location.getLatitude(), location.getLongitude() );
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ArrayList<CheckIn> checkinList = event.getCheckIns();
            List<LatLng> latLngList =  new ArrayList<>();
            for (CheckIn checkin : checkinList) {
                String locSting = checkin.getCheckInLocation();
                if (locSting == null || locSting.equals("")) {
                    continue;
                }
                String[] arrOfStr = locSting.split(",", 2);

                double lat = Double.parseDouble(arrOfStr[0]);
                double lng = Double.parseDouble(arrOfStr[1]);
                latLngList.add(new LatLng(lat, lng));
            }

            if (!latLngList.isEmpty()) {
                HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                        .data(latLngList)
                        .radius(50)
                        .build();

                TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

                LatLngBounds.Builder bounds = new LatLngBounds.Builder().include(latLngList.get(0));
                for (int i = 0; i < latLngList.size(); i++) {
                    bounds.include(latLngList.get(i));
                }

                if (areBoundsTooSmall(bounds.build(), 300)) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.build().getCenter(), 17), 1000, null);
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120), 1000, null);
                }
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(p1, 16), 1000, null);
            }

        }

    }

    private boolean areBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }

}