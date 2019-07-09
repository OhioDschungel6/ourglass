package com.othregensburg.ourglass.TimeOverview;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.othregensburg.ourglass.Entity.Stamp;
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.Entity.Workday;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.TimeOverview.DailyOverview.DailyOverviewActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseAdapterTimeOverview extends FirebaseRecyclerAdapter<Workday, FirebaseAdapterTimeOverview.ViewHolder> {

    private DateFormat df = new SimpleDateFormat("EEEE, dd.MM.yy", Locale.GERMANY);
    private Context con;

    public FirebaseAdapterTimeOverview(@NonNull FirebaseRecyclerOptions<Workday> options, Context context) {
        super(options);
        this.con=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull Workday model) {
        String key = getRef(position).getKey();
        viewHolder.tag.setText(df.format(new Date(Integer.parseInt(key.substring(0,2))+100,Integer.parseInt(key.substring(2,4))-1,Integer.parseInt(key.substring(4)))));
        Time t=new Time();

        if (model.ill) {
            viewHolder.duration.setText("Krank");
        } else if (model.holiday) {
            viewHolder.duration.setText("Urlaub");
        } else {
            List<Stamp> iter = new ArrayList<>(model.timestamps.values());
            Collections.sort(iter, new Comparator<Stamp>() {
                @Override
                public int compare(Stamp o1, Stamp o2) {
                    return o1.start.compareTo(o2.start);
                }
            });

            viewHolder.timesList.removeAllViews();
            for (Stamp p :iter) {
                TextView v = new TextView(con);
                if (p.end == null) {
                    p.end = "";
                }
                v.setText(String.format(Locale.GERMAN, "●  %s - %s", p.start, p.end));
                viewHolder.timesList.addView(v);
                t.add(p);
            }
            viewHolder.duration.setText(t.toString());
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!model.ill && !model.holiday) {
                    Intent intent = new Intent(con, DailyOverviewActivity.class);
                    // Extra z.B.: https://ourglass-84f4d.firebaseio.com/workdays/DV8i9rsyXUdXtWA30SCTmiEnfib2/190222
                    intent.putExtra("DatabaseRef", getRef(position).toString());
                    intent.putExtra("minutesWorked", t.getMinutes());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    con.startActivity(intent);
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mItemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.time_overview_entry, viewGroup, false);
        return new FirebaseAdapterTimeOverview.ViewHolder(mItemView);
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
