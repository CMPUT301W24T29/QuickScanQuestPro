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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quickscanquestpro.R;
import com.example.quickscanquestpro.User;

import java.util.List;

public class AdminUserImageAdapter extends RecyclerView.Adapter<AdminUserImageAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void onItemClicked(User user);
    }


    public AdminUserImageAdapter(Context context, List<User> userList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.userList = userList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_item_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.itemName.setText(user.getName());
        Glide.with(context).load(user.getProfilePictureUrl()).placeholder(R.drawable.default_profile).into(holder.miniPhoto);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClicked(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        ImageView miniPhoto;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            miniPhoto = itemView.findViewById(R.id.mini_photo);
            deleteButton = itemView.findViewById(R.id.delete_button);
            // Setup delete button click listener here if needed
        }
    }
}
