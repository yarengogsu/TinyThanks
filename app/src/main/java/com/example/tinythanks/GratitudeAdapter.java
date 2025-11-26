package com.example.tinythanks;

import android.content.Context;
import android.content.Intent;
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
        final TextView itemTextView;
        final TextView itemTimestampView;
        final ImageView itemPhotoView;
        final TextView itemPhotoBadgeView;

        GratitudeViewHolder(View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.item_text);
            itemTimestampView = itemView.findViewById(R.id.item_timestamp);
            itemPhotoView = itemView.findViewById(R.id.item_photo);
            itemPhotoBadgeView = itemView.findViewById(R.id.item_badge_photo);
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

        // text
        holder.itemTextView.setText(current.getGratitudeText());

        // date
        Date date = new Date(current.getTimestamp());
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
                Locale.getDefault()
        );
        holder.itemTimestampView.setText(dateFormat.format(date));

        // photo / placeholder + badge
        String photoPath = current.getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            holder.itemPhotoView.setImageURI(Uri.parse(photoPath));
            holder.itemPhotoBadgeView.setVisibility(View.VISIBLE);
        } else {
            holder.itemPhotoView.setImageResource(R.drawable.ic_placeholder);
            holder.itemPhotoBadgeView.setVisibility(View.GONE);
        }

        // detail screen on tap
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, EntryDetailActivity.class);

            intent.putExtra(EntryDetailActivity.EXTRA_ID, current.getId());
            intent.putExtra(EntryDetailActivity.EXTRA_TEXT, current.getGratitudeText());
            intent.putExtra(EntryDetailActivity.EXTRA_PHOTO_PATH, current.getPhotoPath());
            intent.putExtra(EntryDetailActivity.EXTRA_TIMESTAMP, current.getTimestamp());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    public void setEntries(List<GratitudeEntry> entries) {
        this.mEntries = (entries != null) ? entries : Collections.emptyList();
        notifyDataSetChanged();
    }
}
