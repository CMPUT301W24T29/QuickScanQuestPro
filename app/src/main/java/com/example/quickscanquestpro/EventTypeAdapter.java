package com.example.quickscanquestpro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventTypeAdapter extends RecyclerView.Adapter<EventTypeAdapter.EventTypeViewHolder>{

    private Context context;
    private List<EventDashboardModel> modelList;
    private List<Event> list = new ArrayList<>();

    public EventTypeAdapter(Context context, List<EventDashboardModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    public static class EventTypeViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout linearLayout;
        private RelativeLayout expandableLayout;
        private TextView textView;
        private ImageView arrowImage;
        private RecyclerView nestedRecyclerView;
        private TextView defaultEventText;


        public EventTypeViewHolder(@NonNull View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.events_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);
            textView = itemView.findViewById(R.id.event_header_title);
            arrowImage = itemView.findViewById(R.id.arrow_imageview);
            nestedRecyclerView = itemView.findViewById(R.id.events_rv);
            defaultEventText = itemView.findViewById(R.id.default_event_text);
        }
    }

    @NonNull
    @Override
    public EventTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.expandable_event_header, parent, false);
        return new EventTypeViewHolder(view);
    }

    public void onBindViewHolder(@NonNull EventTypeViewHolder holder, int position) {

        EventDashboardModel model = modelList.get(position);
        holder.textView.setText(model.getEventType());
/**
        if(model.getEventType().equals("Current Checked-In Event")) {
            model.setExpandable(true);
        }
*/
        boolean isExpandable = model.isExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

        if (isExpandable){
            holder.arrowImage.setImageResource(R.drawable.baseline_arrow_drop_up_24);
        }else{
            holder.arrowImage.setImageResource(R.drawable.baseline_arrow_drop_down_24);
        }
/**
        if(model.getEventListSize()==0) {
            holder.nestedRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.setExpandable(!model.isExpandable());
                    holder.nestedRecyclerView.setContentDescription();
                    notifyItemChanged(holder.getAbsoluteAdapterPosition());
                }
            });
        }
*/
        EventListAdapter adapter = new EventListAdapter(context, list);
        holder.nestedRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.nestedRecyclerView.setAdapter(adapter);
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setExpandable(!model.isExpandable());
                list = model.getEventList();
                notifyItemChanged(holder.getAbsoluteAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
