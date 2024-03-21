package com.example.quickscanquestpro;
import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;

import com.bumptech.glide.Glide;

import java.util.List;
public class EventListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements EventHeaderViewHolder.HeaderViewHolderCallback{
    private static final int EVENT_TYPE = 1;
    private static final int EVENT_HEADER_TYPE = 2;
    private Context context;
    private List<Event> eventDataList;
    private List<String> eventHeaderList;
    private DatabaseService databaseService = new DatabaseService();
    private SparseArray<EventViewType> viewTypes;
    private SparseIntArray headerExpandTracker;

    public EventListAdapter(Context context, List<Event> eventDataList, List<String> eventHeaderList) {
        this.context = context;
        this.eventDataList = eventDataList;
        this.eventHeaderList = eventHeaderList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case EVENT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
                return new EventViewHolder(view);
            case EVENT_HEADER_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_header, parent, false);
                return new EventHeaderViewHolder(view, this);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
                return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        EventViewType viewType = viewTypes.get(position);
        if (itemViewType == EVENT_TYPE) {
            bindEventViewHolder(holder, viewType);
        } else {
            bindEventHeaderViewHolder(holder, position, viewType);
        }
    }

    private void bindEventHeaderViewHolder(RecyclerView.ViewHolder holder, int position, EventViewType viewType) {
        int dataIndex = viewType.getDataIndex();
        EventHeaderViewHolder headerViewHolder = (EventHeaderViewHolder) holder;
        headerViewHolder.sectionTitle.setText(eventHeaderList.get(dataIndex));
        if (isExpanded(position)) {
            headerViewHolder.sectionTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, headerViewHolder.arrowUp, null);
        } else {
            headerViewHolder.sectionTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, headerViewHolder.arrowDown, null);
        }
    }

    public void bindEventViewHolder(RecyclerView.ViewHolder holder, EventViewType viewType) {
        int dataIndex = viewType.getDataIndex();
        Event event = eventDataList.get(dataIndex);

        // Truncate title and location if longer than 30 characters
        String title = event.getTitle().length() > 30 ? event.getTitle().substring(0, 27) + "..." : event.getTitle();
        String location = event.getLocation().length() > 30 ? event.getLocation().substring(0, 27) + "..." : event.getLocation();

        // Format the dates and times
        String dates = event.getStartDate().toString() + " - " + event.getEndDate().toString();
        String times = event.getStartTime().toString() + " - " + event.getEndTime().toString();

        ((EventViewHolder) holder).eventTitle.setText(title);
        ((EventViewHolder) holder).eventLocation.setText(location);
        ((EventViewHolder) holder).eventDates.setText(dates);
        ((EventViewHolder) holder).eventTimes.setText(times);


        String imageUrl = event.getEventBannerUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.color.white)
                    .fitCenter()
                    .into(((EventViewHolder) holder).eventImage);
        } else {
            ((EventViewHolder) holder).eventImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                databaseService.getEvent(event.getId(), event1 -> {
                    if (event1 != null) {
                        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment(event1);

                        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();

                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        fragmentTransaction.replace(R.id.content, eventDetailsFragment);

                        fragmentTransaction.addToBackStack(null);

                        fragmentTransaction.commit();
                    } else {
                        Log.e("EventListAdapter", "Event is null. Cannot display details.");
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (eventHeaderList != null && eventDataList != null) {
            viewTypes.clear();
            int collapsedCount = 0;
            for (int i = 0; i < eventHeaderList.size(); i++) {
                viewTypes.put(count, new EventViewType(i, EVENT_HEADER_TYPE));
                count += 1;
                String eventHeader = eventHeaderList.get(i);
                int childCount = getChildCount(eventHeader);
                if (headerExpandTracker.get(i) != 0) {
                    // Expanded State
                    for (int j = 0; j < childCount; j++) {
                        viewTypes.put(count, new EventViewType(count - (i + 1) + collapsedCount, EVENT_TYPE));
                        count += 1;
                    }
                } else {
                    // Collapsed
                    collapsedCount += childCount;
                }
            }
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (viewTypes.get(position).getType() == EVENT_HEADER_TYPE) {
            return EVENT_HEADER_TYPE;
        } else {
            return EVENT_TYPE;
        }
    }

    private int getChildCount(String type) {
        switch (type) {
            case "Checked-In Events":
                return 6;
            //case "Signed-Up Events":
            //return 6;
            //case "Organized Events":
            //return 6;
            default:
                return 0;
        }
    }

    public void setEventListAndHeader(List<Event> eventList, List<String> headerList){
        if (eventList != null && headerList != null) {
            this.eventDataList = eventList;
            this.eventHeaderList = headerList;
            viewTypes = new SparseArray<>(eventList.size() + headerList.size());
            headerExpandTracker = new SparseIntArray(headerList.size());
            notifyDataSetChanged();
        }
    }

    @Override
    public void onHeaderClick(int position) {
        EventViewType viewType = viewTypes.get(position);
        int dataIndex = viewType.getDataIndex();
        String eventType = eventHeaderList.get(dataIndex);
        int childCount = getChildCount(eventType);
        if (headerExpandTracker.get(dataIndex) == 0) {
            // Collapsed. Now expand it
            headerExpandTracker.put(dataIndex, 1);
            notifyItemRangeInserted(position + 1, childCount);
        } else {
            // Expanded. Now collapse it
            headerExpandTracker.put(dataIndex, 0);
            notifyItemRangeRemoved(position + 1, childCount);
        }
    }

    @Override
    public boolean isExpanded(int position) {
        int dataIndex = viewTypes.get(position).getDataIndex();
        return headerExpandTracker.get(dataIndex) == 1;
    }

    public void updateEvents(List<Event> events) {
        this.eventDataList.clear();
        this.eventDataList.addAll(events);
        notifyDataSetChanged();
    }
}
