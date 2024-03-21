package com.example.quickscanquestpro;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
public class EventHeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView sectionTitle;
    private final HeaderViewHolderCallback callback;

    Drawable arrowUp;
    Drawable arrowDown;

    public EventHeaderViewHolder(View itemView, HeaderViewHolderCallback callback) {
        super(itemView);
        sectionTitle = itemView.findViewById(R.id.header_event);
        this.callback = callback;

        arrowUp = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.arrow_up_float);
        arrowDown = ContextCompat.getDrawable(itemView.getContext(), android.R.drawable.arrow_down_float);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int position = getAbsoluteAdapterPosition();
        callback.onHeaderClick(position);
        if (callback.isExpanded(position)) {
            sectionTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowUp, null);
        } else {
            sectionTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDown, null);
        }
    }

    public interface HeaderViewHolderCallback {
        void onHeaderClick(int position);

        boolean isExpanded(int position);
    }
}
