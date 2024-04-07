package com.example.quickscanquestpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SignupListAdapter extends ArrayAdapter<User> {
    private Context context;
    private int resource;
    private List<User> users;

    public SignupListAdapter(@NonNull Context context, int resource, @NonNull List<User> users) {
        super(context, resource, users);
        this.context = context;
        this.resource = resource;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView userNameTextView = convertView.findViewById(R.id.signup_name);

        userNameTextView.setText(user.getName());



        return convertView;
    }
}
