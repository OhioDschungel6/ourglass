package com.othregensburg.ourglass.RecyclerAdapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.entity.Arbeitstag;
import com.othregensburg.ourglass.entity.Stamp;
import com.othregensburg.ourglass.entity.Time;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class FirebaseAdapterStundenuebersicht extends FirebaseRecyclerAdapter<Arbeitstag,FirebaseAdapterStundenuebersicht.ViewHolder> {

    private DateFormat df = new SimpleDateFormat("EEEE, dd.MM.yy", Locale.GERMANY);
    private Context con;

    public FirebaseAdapterStundenuebersicht(@NonNull FirebaseRecyclerOptions<Arbeitstag> options, Context context) {
        super(options);
        this.con=context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull Arbeitstag model) {
        String key= getRef(position).getKey();
        viewHolder.tag.setText(df.format(new Date(Integer.parseInt(key.substring(4)),Integer.parseInt(key.substring(2,4)),Integer.parseInt(key.substring(0,2)))));
        Time t=new Time();


        for (Stamp p :model.timestamps.values()) {
            TextView v = new TextView(con);
            v.setText(String.format(Locale.GERMAN, "●  %s - %s", p.startzeit, p.endzeit));
            viewHolder.timesList.addView(v);
            t.add(p);
        }
        viewHolder.duration.setText(t.toString());

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mItemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.stundenuebersicht_entry, viewGroup, false);
        return new FirebaseAdapterStundenuebersicht.ViewHolder(mItemView);
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
