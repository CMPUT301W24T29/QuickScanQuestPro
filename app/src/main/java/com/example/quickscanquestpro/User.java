package com.example.quickscanquestpro;

public class User {

    private boolean isAdmin;

    // Constructor to set isAdmin
    public User(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    // Getter for isAdmin
    public boolean isAdmin() {
        return isAdmin;
    }

    // Setter for isAdmin (optional, if you need to change the role dynamically)
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
