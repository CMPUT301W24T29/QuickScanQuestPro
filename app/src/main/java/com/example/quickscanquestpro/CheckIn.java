package com.example.quickscanquestpro;

public class CheckIn {
    private String userId;
    private String checkInLocation;

    public CheckIn(String userId, String checkInLocation) {
        this.userId = userId;
        this.checkInLocation = checkInLocation;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCheckInLocation() {
        return checkInLocation;
    }

    public void setCheckInLocation(String checkInLocation) {
        this.checkInLocation = checkInLocation;
    }
}
