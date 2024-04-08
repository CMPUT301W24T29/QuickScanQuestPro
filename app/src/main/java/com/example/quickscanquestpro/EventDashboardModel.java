package com.example.quickscanquestpro;

import java.util.List;

/**
 * Model class for the expandable headers
 */

public class EventDashboardModel {

    private List<Event> eventList;
    private String eventType;
    private boolean isExpandable;

    /**
     * Default constructor for EventDashboardModel class.
     * It sets the value of isExpandable to false as default for all types of events except for checked in event which is set to true as default
     * @param eventList - The list of events to be displayed
     * @param eventType - The type of event being displayed
     */
    public EventDashboardModel(List<Event> eventList, String eventType) {
        this.eventList = eventList;
        this.eventType = eventType;
        if(this.eventType.equals("Checked In Event")){
            this.isExpandable = true;
        } else {
            this.isExpandable = false;
        }
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public int getEventListSize() {
        return eventList.size();
    }
}
