package com.example.quickscanquestpro;

/**
 * To keep track of the view type and its position in the respective Lists
 */
public class EventViewType {

    private int dataIndex;
    private int type;

    public EventViewType(int dataIndex, int type) {
        this.dataIndex = dataIndex;
        this.type = type;
    }

    public int getDataIndex() {
        return dataIndex;
    }

    public int getType() {
        return type;
    }
}
