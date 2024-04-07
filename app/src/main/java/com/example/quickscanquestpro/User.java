package com.example.quickscanquestpro;

import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents a user of the application.
 */
public class User {

    private String userId;

    private String name;

    private int checkins;

    private String mobileNum;

    private String email;

    private boolean geolocation;

    private String homepage;

    private boolean admin;

    private String profilePictureUrl;

    private String profilePicturePath;
    private String lastCheckIn;

    /*
     * user preference
     * text file storing ID
     * */

    public User(String userId) {
        this.userId = userId;
    }

    public void updateUser(String name, int checkins, String mobileNum, String email, String homepage, boolean geolocation, String profilePictureUrl, String lastCheckIn) {
        this.name = name;
        this.checkins = checkins;
        this.mobileNum = mobileNum;
        this.email = email;
        this.homepage = homepage;
        this.geolocation = geolocation;
        this.profilePictureUrl = profilePictureUrl;
        this.lastCheckIn = lastCheckIn;
    }

    public String getName() {
        return name;
    }

    public int getCheckins() {
        return checkins;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCheckins(int checkins) {
        this.checkins = checkins;
    }

    // return name and checkins as a string
    public String toString() {
        return "NAME: " + name + "  " + "CHECK-INS: " + checkins;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isGeolocation() {
        return geolocation;
    }

    public void setGeolocation(boolean geolocation) {
        this.geolocation = geolocation;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getUserId() {
        return userId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }


    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public String getLastCheckIn() {
        return lastCheckIn;
    }

    public void setLastCheckIn(String lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
    }

    public void setAdmin(boolean admin){
        this.admin = admin;
    }

    /**
     * Gets the initials of the user's name.
     * @return A string containing the initials of the user.
     */
    public String getInitials() {
        if (this.name == null || this.name.isEmpty()) {
            return ""; // Return an empty string if name is not set
        }

        StringBuilder initials = new StringBuilder();
        for (String part : this.name.split(" ")) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

}



