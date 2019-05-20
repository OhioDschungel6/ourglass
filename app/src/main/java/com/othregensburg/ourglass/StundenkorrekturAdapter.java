package com.othregensburg.ourglass;

import android.app.TimePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class StundenkorrekturAdapter extends RecyclerView.Adapter<StundenkorrekturAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Pair<Time, Time>> dates;
    private final Context context;
    Time changedTime;


    StundenkorrekturAdapter(Context context, List<Pair<Time,Time>> dates) {
        mInflater = LayoutInflater.from(context);
        this.dates = dates;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View mItemView = mInflater.inflate(R.layout.entry, parent, false);
        return new ViewHolder(mItemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        viewHolder.startTime.setText(String.format(Locale.GERMAN, "%2d.%02d", dates.get(position).first.getHours(), dates.get(position).first.getMinutes()));
        viewHolder.endTime.setText(String.format(Locale.GERMAN, "%2d.%02d", dates.get(position).second.getHours(), dates.get(position).second.getMinutes()));
        viewHolder.startTime.setTag(position);
        viewHolder.endTime.setTag(position);
        viewHolder.removeButton.setTag(position);
    }


    @Override
    public int getItemCount() {
        return dates.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView startTime;
        final TextView endTime;
        final FloatingActionButton removeButton;
        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            startTime=itemView.findViewById(R.id.startTime);
            endTime=itemView.findViewById(R.id.endTime);
            removeButton = itemView.findViewById(R.id.remove);
            startTime.setOnClickListener(h->{
                LinearLayout r=(LinearLayout)h.getParent();
                RecyclerView parent = (RecyclerView)r.getParent();
                int nr = (int)startTime.getTag();// parent.indexOfChild(r);
                changedTime= dates.get(nr).first;
                TimePickerDialog tpd = new TimePickerDialog(context, onTimeDialogCallback, dates.get(nr).first.getHours(), dates.get(nr).second.getMinutes(), true);
                tpd.show();
            });
            endTime.setOnClickListener(h->{
                LinearLayout r=(LinearLayout)h.getParent();
                RecyclerView parent = (RecyclerView)r.getParent();
                int nr = (int)endTime.getTag();//parent.indexOfChild(r);
                changedTime= dates.get(nr).second;
                TimePickerDialog tpd = new TimePickerDialog(context, onTimeDialogCallback, dates.get(nr).first.getHours(), dates.get(nr).second.getMinutes(), true);
                tpd.show();
            });
            removeButton.setOnClickListener(h->{
                LinearLayout r=(LinearLayout)h.getParent();
                RecyclerView parent = (RecyclerView)r.getParent();
                int nr = (int)removeButton.getTag();//parent.indexOfChild(r);
                dates.remove(nr);
                notifyDataSetChanged();
            });
        }
        private TimePickerDialog.OnTimeSetListener onTimeDialogCallback= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                changedTime.setHours(hourOfDay);
                changedTime.setMinutes(minute);
                notifyDataSetChanged();
            }
        };
    }
}
