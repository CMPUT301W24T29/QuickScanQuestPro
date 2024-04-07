package com.example.quickscanquestpro;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;


/**
 * An adapter for rendering a list of {@link Event} objects within an admin interface. This adapter
 * converts each {@link Event} object into a View that can be inserted into a RecyclerView and
 * provides functionality for navigating to the user's profile details and deleting a event.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventsList;
    private DatabaseService databaseService = new DatabaseService();
    private boolean isAdmin;

    /**
     * Constructs an adapter for displaying a list of {@link Event} objects in a RecyclerView.
     * The adapter supports functionalities like navigating to event details and event deletion
     * for administrators.
     *
     * @param context The current context, used for inflating layouts and accessing resources.
     * @param eventsList The list of events to be displayed.
     * @param isAdmin A boolean indicating whether the user is an administrator. This affects
     *                visibility of admin-specific functionalities like deleting events.
     */
    public AdminEventAdapter(Context context, List<Event> eventsList, boolean isAdmin) {
        this.context = context;
        this.eventsList = eventsList;
        this.isAdmin = isAdmin;

    }

    /**
     * A ViewHolder class for the RecyclerView in AdminEventAdapter. It holds the UI elements
     * that represent a single event item within the RecyclerView. This includes views for
     * displaying the event's image, title, location, dates, times, and a delete button.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage, deleteButton;
        TextView eventTitle, eventLocation, eventDates, eventTimes;



        /**
         * Constructor that initializes the ViewHolder with references to the UI components
         * based on the itemView provided. It uses the itemView to find each component by its
         * ID, facilitating the setting of event details in onBindViewHolder of the adapter.
         *
         * @param itemView The View for a single item row in the RecyclerView. It contains
         *                 the event image, title, location, dates, times, and delete button.
         */
        public EventViewHolder(View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.event_image);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventLocation = itemView.findViewById(R.id.event_location);
            eventDates = itemView.findViewById(R.id.event_dates);
            eventTimes = itemView.findViewById(R.id.event_times);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }


    /**
     * Provides a new ViewHolder instance for the RecyclerView to display. This method inflates
     * the layout for individual event items.
     *
     * @param parent The parent ViewGroup into which the new View will be added after it is
     *               bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new instance of EventViewHolder that holds the View for each event item.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }


    /**
     * Binds the data at the specified position in the eventsList to the ViewHolder. This method
     * sets up the event details, formats date and time strings, and initializes click listeners
     * for navigating to event details and deleting events (if the user is an admin).
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *               item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventsList.get(position);

        // Truncate title and location if longer than 30 characters
        String title = event.getTitle().length() > 30 ? event.getTitle().substring(0, 20) + "..." : event.getTitle();
        String location = event.getLocation().length() > 30 ? event.getLocation().substring(0, 20) + "..." : event.getLocation();

        // Format the dates and times
        String dates = event.getStartDate().toString() + " - " + event.getEndDate().toString();
        String times = event.getStartTime().toString() + " - " + event.getEndTime().toString();

        holder.eventTitle.setText(title);
        holder.eventLocation.setText(location);
        holder.eventDates.setText(dates);
        holder.eventTimes.setText(times);
        holder.deleteButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);


        String imageUrl = event.getEventBannerUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.color.white)
                    .fitCenter()
                    .into(holder.eventImage);
        } else {
            holder.eventImage.setImageResource(R.drawable.ic_launcher_background);
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

        holder.deleteButton.setOnClickListener(v ->{
            databaseService.deleteEvent(event);
            eventsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, eventsList.size());
        });
    }


    /**
     * Returns the total number of items in the eventsList (the data set held by the adapter).
     *
     * @return The size of the eventsList.
     */
    @Override
    public int getItemCount() {
        return eventsList.size();
    }


    /**
     * Updates the events list within the adapter and refreshes the RecyclerView to display
     * the new data. This method is useful for dynamically adding, removing, or updating the
     * list of events.
     *
     * @param events The new list of events to be displayed.
     */
    public void updateEvents(List<Event> events) {
        this.eventsList.clear();
        this.eventsList.addAll(events);
        notifyDataSetChanged();
    }
}
