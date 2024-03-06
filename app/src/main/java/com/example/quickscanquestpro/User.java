package com.example.quickscanquestpro;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

    /*
     * user preference
     * text file storing ID
     * */

    public User(String userId){
        this.userId = userId;
    }

    public void updateUser(String name, int checkins, String mobileNum, String email, String homepage, boolean geolocation, String profilePictureUrl) {
        this.name = name;
        this.checkins = checkins;
        this.mobileNum = mobileNum;
        this.email = email;
        this.homepage = homepage;
        this.geolocation = geolocation;
        this.profilePictureUrl = profilePictureUrl;
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
        return "NAME: "+ name + "  " + "CHECK-INS: " +checkins;
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

    public String getProfilePictureUrl()
    {
        return profilePictureUrl;
    }


    public void setProfilePictureUrl(String profilePictureUrl)
    {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setProfilePicturePath(String profilePicturePath)
    {
        this.profilePicturePath = profilePicturePath;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void saveToFirestore() {
        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map to hold user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", this.userId);
        userData.put("name", this.name);
        userData.put("checkins", this.checkins);
        userData.put("mobileNum", this.mobileNum);
        userData.put("email", this.email);
        userData.put("geolocation", this.geolocation);
        userData.put("homepage", this.homepage);
        userData.put("admin", this.admin);
        userData.put("profilePictureUrl", this.profilePictureUrl);

    public void setAdmin(boolean admin){
        this.admin = admin;
    }


}
