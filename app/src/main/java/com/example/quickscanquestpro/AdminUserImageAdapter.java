package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickscanquestpro.R;
import com.example.quickscanquestpro.User;

import java.util.List;

/**
 * Adapter class for managing user images in the admin panel.
 * This adapter binds user data to views displayed within a RecyclerView, handling the presentation of user images and interactions such as clicks.
 */

public class AdminUserImageAdapter extends RecyclerView.Adapter<AdminUserImageAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClicked(User user);
    }

    /**
     * Constructs an AdminUserImageAdapter.
     *
     * @param context The current context, used for various operations within the adapter.
     * @param userList The list of users whose information will be displayed.
     * @param onItemClickListener The callback that will be invoked when an item is clicked.
     */

    public AdminUserImageAdapter(Context context, List<User> userList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new view will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_admin, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.itemName.setText(user.getName());
        Glide.with(context)
                .load(user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : ContextCompat.getDrawable(context, R.drawable.default_profile))
                .placeholder(R.drawable.default_profile)
                .into(holder.miniPhoto);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClicked(user));
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for the admin user images. Holds the views for individual items in the list.
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        ImageView miniPhoto;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            miniPhoto = itemView.findViewById(R.id.mini_photo);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
