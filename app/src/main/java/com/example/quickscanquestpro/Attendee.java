package com.example.quickscanquestpro;

public class Attendee {

    private String name;

    private int checkins;

    public Attendee(String name, int checkins) {
        this.name = name;
        this.checkins = checkins;
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

}