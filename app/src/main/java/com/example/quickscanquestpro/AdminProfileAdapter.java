package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * An adapter for rendering a list of {@link User} objects within an admin interface. This adapter
 * converts each {@link User} object into a View that can be inserted into a RecyclerView and
 * provides functionality for navigating to the user's profile details and deleting a user.
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {
    private Context context;
    private List<User> usersList;
    private DatabaseService databaseService = new DatabaseService();
    private boolean isAdmin;

    /**
     * Constructs an AdminProfileAdapter.
     *
     * @param context   The context where the adapter is being used, necessary for layout inflation and accessing resources.
     * @param usersList A list of User objects to be managed and displayed.
     * @param isAdmin   A boolean flag to indicate if the current user has administrative privileges.
     */
    public AdminProfileAdapter(Context context, List<User> usersList, boolean isAdmin) {
        this.context = context;
        this.usersList = usersList;
        this.isAdmin = isAdmin;
    }

    /**
     * Provides a reference to the views for each data item. Complex data items may need more than one view per item,
     * and you provide access to all the views for a data item in a view holder.
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture, deleteButton;
        TextView profileName;

        /**
         * Constructor for the ProfileViewHolder.
         * @param itemView The container view which holds the layout for individual list items.
         */
        public ProfileViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            profileName = itemView.findViewById(R.id.profile_name);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }


    /**
     * Called when RecyclerView needs a new {@link ProfileViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View. This is useful for providing different
     *                 layouts based on the view type.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_admin_manage_profile, parent, false);
        return new ProfileViewHolder(itemView);
    }


    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ProfileViewHolder#itemView} to reflect the item at the given
     * position.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AdminProfileAdapter.ProfileViewHolder holder, int position) {
        User user = usersList.get(position);
        String name = user.getName().length() > 25 ? user.getName().substring(0, 20) + "..." : user.getName();
        holder.profileName.setText(name);
        holder.deleteButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : ContextCompat.getDrawable(context, R.drawable.default_profile))
                .placeholder(R.drawable.default_profile)
                .into(holder.profilePicture);

        holder.deleteButton.setOnClickListener(v -> {
            databaseService.deleteUser(user);
            usersList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, usersList.size());
        });

        // Set the click listener for changing the fragment
        holder.itemView.setOnClickListener(v -> {
            ProfileFragment profileFragment = new ProfileFragment(user);
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content, profileFragment); // Make sure R.id.content is the ID of your fragment container
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return usersList.size();
    }

}
