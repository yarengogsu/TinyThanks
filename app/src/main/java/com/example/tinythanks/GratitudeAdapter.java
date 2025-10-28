package com.example.tinythanks;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GratitudeAdapter extends RecyclerView.Adapter<GratitudeAdapter.GratitudeViewHolder> {

    private List<GratitudeEntry> mEntries = Collections.emptyList();
    private final LayoutInflater mInflater;

    public GratitudeAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }


    static class GratitudeViewHolder extends RecyclerView.ViewHolder {
        private final TextView itemTextView;
        private final TextView itemTimestampView;
        private final ImageView itemPhotoView;

        private GratitudeViewHolder(View itemView) {
            super(itemView);

            itemTextView = itemView.findViewById(R.id.item_text);
            itemTimestampView = itemView.findViewById(R.id.item_timestamp);
            itemPhotoView = itemView.findViewById(R.id.item_photo);
        }
    }


    @NonNull
    @Override
    public GratitudeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = mInflater.inflate(R.layout.list_item_gratitude, parent, false);
        return new GratitudeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GratitudeViewHolder holder, int position) {
        GratitudeEntry current = mEntries.get(position);

        holder.itemTextView.setText(current.getGratitudeText());

        Date date = new Date(current.getTimestamp());
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        holder.itemTimestampView.setText(formattedDate);

        String photoPath = current.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            holder.itemPhotoView.setImageURI(Uri.parse(photoPath));
        }
        else {
            holder.itemPhotoView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {

        return mEntries.size();
    }

    public void setEntries(List<GratitudeEntry> entries) {
        this.mEntries = entries;
        notifyDataSetChanged();
    }
}