package com.example.quickscanquestpro;

import java.util.List;

public class EventDashboardModel {

    private List<Event> eventList;
    private String eventType;
    private boolean isExpandable;

    public EventDashboardModel(List<Event> eventList, String eventType) {
        this.eventList = eventList;
        this.eventType = eventType;
        this.isExpandable = false;
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
