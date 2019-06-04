package com.othregensburg.ourglass.RecyclerAdapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.othregensburg.ourglass.TagesuebersichtActivity;
import com.othregensburg.ourglass.entity.Arbeitstag;
import com.othregensburg.ourglass.entity.Stamp;
import com.othregensburg.ourglass.entity.Time;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
        String key = getRef(position).getKey();
        viewHolder.tag.setText(df.format(new Date(Integer.parseInt(key.substring(0,2)),Integer.parseInt(key.substring(2,4))-1,Integer.parseInt(key.substring(4)))));
        if (model.krank) {
            viewHolder.duration.setText("Krank");
        } else if (model.urlaub) {
            viewHolder.duration.setText("Urlaub");
        } else {
            Time t=new Time();

            List<Stamp> iter = new ArrayList<>(model.timestamps.values());
            Collections.sort(iter, new Comparator<Stamp>() {
                @Override
                public int compare(Stamp o1, Stamp o2) {
                    return o1.startzeit.compareTo(o2.startzeit);
                }
            });

            viewHolder.timesList.removeAllViews();
            for (Stamp p :iter) {
                TextView v = new TextView(con);
                if (p.endzeit == null) {
                    p.endzeit="";
                }
                v.setText(String.format(Locale.GERMAN, "●  %s - %s", p.startzeit, p.endzeit));
                viewHolder.timesList.addView(v);
                t.add(p);
            }
            viewHolder.duration.setText(t.toString());
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Tag übergeben, ViewModel?
                Intent intent = new Intent(con , TagesuebersichtActivity.class);
                con.startActivity(intent);
            }
        });
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
