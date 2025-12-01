package com.example.tinythanks;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.ViewHolder> {

    private List<GratitudeEntry> entryList = new ArrayList<>();

    // --- TIKLAMA OLAYI İÇİN ARABİRİM ---
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GratitudeEntry entry);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // -----------------------------------

    public void setEntries(List<GratitudeEntry> entries) {
        this.entryList = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journey_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GratitudeEntry currentEntry = entryList.get(position);

        holder.tvPreview.setText(currentEntry.getGratitudeText());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateString = sdf.format(new Date(currentEntry.getTimestamp()));
        holder.tvDate.setText(dateString);

        String path = currentEntry.getPhotoPath();
        if (path != null && !path.isEmpty()) {
            holder.imgThumbnail.setImageURI(Uri.fromFile(new File(path)));
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.img_morning);
        }

        // --- KARTA TIKLANINCA ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentEntry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvPreview;
        ShapeableImageView imgThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvEntryDate);
            tvPreview = itemView.findViewById(R.id.tvEntryPreview);
            imgThumbnail = itemView.findViewById(R.id.imgEntryThumbnail);
        }
    }
}