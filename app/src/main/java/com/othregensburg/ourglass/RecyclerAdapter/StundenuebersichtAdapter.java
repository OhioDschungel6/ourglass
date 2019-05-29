package com.othregensburg.ourglass.RecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.othregensburg.ourglass.R;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StundenuebersichtAdapter extends RecyclerView.Adapter<StundenuebersichtAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Date> dates;
    private Map<Date, List<Pair<Time, Time>>> times;
    private final Context context;
    private DateFormat df = new SimpleDateFormat("EEEE, dd.MM.yy", Locale.GERMANY);


    public StundenuebersichtAdapter(Context context, List<Date> dates, Map<Date, List<Pair<Time, Time>>> times) {
        mInflater = LayoutInflater.from(context);
        this.dates = dates;
        this.times = times;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View mItemView = mInflater.inflate(R.layout.stundenuebersicht_entry, parent, false);
        return new ViewHolder(mItemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        Date date = dates.get(position);
        viewHolder.tag.setText(df.format(date));
        viewHolder.duration.setText("8:23");
        for (Pair<Time, Time> p : times.getOrDefault(date, Collections.emptyList())) {
            TextView v = new TextView(context);
            v.setText(String.format(Locale.GERMAN, "●  %d.%02d - %d.%02d", p.first.getHours(), p.first.getMinutes(), p.second.getHours(), p.second.getMinutes()));
            viewHolder.timesList.addView(v);
        }

    }


    @Override
    public int getItemCount() {
        return dates.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tag;
        final TextView duration;
        final LinearLayout timesList;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);
            duration = itemView.findViewById(R.id.duration);
            timesList = itemView.findViewById(R.id.timesList);

        }
    }
}