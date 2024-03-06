package com.example.quickscanquestpro;

public class User {

    private String userId;

    private String name;

    private int checkins;

    private String mobileNum;

    private String email;

    private boolean geolocation;

    private String homepage;
    private boolean admin;

    /*
    * user preference
    * text file storing ID
    * */

    public User(String userId){
        this.userId = userId;
    }

    public void UpdateUser(String name, int checkins, String mobileNum, String email, String homepage, boolean geolocation) {
        this.name = name;
        this.checkins = checkins;
        this.mobileNum = mobileNum;
        this.email = email;
        this.homepage = homepage;
        this.geolocation = geolocation;
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
        return true;
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

    public int getCheckIns() {
        return checkins;
    }

}
