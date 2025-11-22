package com.example.tinythanks;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for showing weekly summaries in the flower garden.
 * Each item shows:
 *  - week range
 *  - entries & active days
 *  - flower state text
 *  - a mini FlowerView on the left
 *  - clicking the item opens WeeklyFlowerActivity for that week
 */
public class WeekSummaryAdapter extends RecyclerView.Adapter<WeekSummaryAdapter.WeekViewHolder> {

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat;
    private List<WeekSummary> weekList = new ArrayList<>();

    public WeekSummaryAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    }

    public void setWeeks(List<WeekSummary> weeks) {
        this.weekList = (weeks != null) ? weeks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_week_summary, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        WeekSummary summary = weekList.get(position);

        String range = formatRange(summary.getWeekStartMillis(), summary.getWeekEndMillis());
        holder.weekRangeText.setText(range);

        String stats = "Entries: " + summary.getTotalEntries()
                + " • Active days: " + summary.getActiveDaysCount();
        holder.weekStatsText.setText(stats);

        holder.weekStateText.setText("State: " + summary.getFlowerState());

        // --- mini flower on the left ---
        if (holder.miniFlower != null) {
            holder.miniFlower.setFlowerState(summary.getFlowerState());
            holder.miniFlower.setTotalEntries(summary.getTotalEntries());
            holder.miniFlower.invalidate();
        }

        // --- clicking opens WeeklyFlowerActivity for that week ---
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, WeeklyFlowerActivity.class);
            intent.putExtra(WeeklyFlowerActivity.EXTRA_WEEK_START, summary.getWeekStartMillis());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return weekList.size();
    }

    private String formatRange(long startMillis, long endMillis) {
        Date start = new Date(startMillis);
        Date end = new Date(endMillis - 1); // endMillis is exclusive
        return dateFormat.format(start) + " - " + dateFormat.format(end);
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView weekRangeText;
        TextView weekStatsText;
        TextView weekStateText;
        FlowerView miniFlower;

        WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            weekRangeText = itemView.findViewById(R.id.text_week_range);
            weekStatsText = itemView.findViewById(R.id.text_week_stats);
            weekStateText = itemView.findViewById(R.id.text_week_state);
            miniFlower = itemView.findViewById(R.id.item_flower_view);
        }
    }
}
