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

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClicked(CalendarDay day);
    }

    private final LayoutInflater inflater;
    private List<CalendarDay> days = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarAdapter(Context context, OnDayClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setDays(List<CalendarDay> days) {
        this.days = (days != null) ? days : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_day_cell, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        // Her bind'de ölçekleri sıfırla (recycle bug'ı olmasın)
        holder.itemView.setScaleX(1f);
        holder.itemView.setScaleY(1f);

        if (day.getDayOfMonth() <= 0) {
            // Boş hücre
            holder.textDayNumber.setText("");
            holder.imageThumb.setVisibility(View.GONE);
            holder.textDayNumber.setBackground(null);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.textDayNumber.setText(String.valueOf(day.getDayOfMonth()));

        if (day.hasEntries()) {
            // Günün entry'si var
            holder.textDayNumber.setBackgroundResource(R.drawable.bg_day_has_entry);
            holder.textDayNumber.setTextColor(0xFF333333);

            String thumb = day.getThumbnailPhotoPath();
            if (thumb != null && !thumb.isEmpty()) {
                holder.imageThumb.setVisibility(View.VISIBLE);
                holder.imageThumb.setImageURI(Uri.parse(thumb));
            } else {
                holder.imageThumb.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener == null) return;

                // Küçük tap animasyonu
                v.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(80)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .start();

                            // Günü açan kod: FlowerGardenActivity içinde listener'da DayEntriesActivity'yi açıyorsun
                            listener.onDayClicked(day);
                        })
                        .start();
            });

        } else {
            // Entry olmayan normal gün
            holder.textDayNumber.setBackground(null);
            holder.imageThumb.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView textDayNumber;
        ImageView imageThumb;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDayNumber = itemView.findViewById(R.id.text_day_number);
            imageThumb = itemView.findViewById(R.id.image_thumb);
        }
    }
}
