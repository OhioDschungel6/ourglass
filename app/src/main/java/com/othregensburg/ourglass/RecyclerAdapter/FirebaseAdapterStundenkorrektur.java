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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.entity.Stamp;

import java.util.Locale;

public class FirebaseAdapterStundenkorrektur extends FirebaseRecyclerAdapter<Stamp,FirebaseAdapterStundenkorrektur.ViewHolder> {


    public FirebaseAdapterStundenkorrektur(@NonNull FirebaseRecyclerOptions<Stamp> options) {
        super(options);

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Stamp model) {
        holder.startTime.setText(model.startzeit);
        holder.endTime.setText(model.endzeit);
        holder.model=model;
        holder.itemRef= getRef(position);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater mInflater=LayoutInflater.from(viewGroup.getContext());
        View mItemView = mInflater.inflate(R.layout.entry, viewGroup, false);
        return new FirebaseAdapterStundenkorrektur.ViewHolder(mItemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Stamp model;
        final TextView startTime;
        final TextView endTime;
        final FloatingActionButton removeButton;
        DatabaseReference itemRef;
        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            startTime=itemView.findViewById(R.id.startTime);
            endTime=itemView.findViewById(R.id.endTime);
            removeButton = itemView.findViewById(R.id.remove);
            startTime.setOnClickListener(h->{
                Pair<Integer,Integer> pair= model.pairStartzeit();
                TimePickerDialog tpd = new TimePickerDialog(itemView.getContext(), onTimeDialogStartZeitCallback, pair.first,pair.second, true);
                tpd.show();
            });
            endTime.setOnClickListener(h->{
                Pair<Integer,Integer> pair= model.pairEndzeit();
                TimePickerDialog tpd = new TimePickerDialog(itemView.getContext(), onTimeDialogEndZeitCallback, pair.first,pair.second, true);
                tpd.show();
            });
            removeButton.setOnClickListener(h->{
                itemRef.setValue(null);
            });
        }
        private TimePickerDialog.OnTimeSetListener onTimeDialogStartZeitCallback= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                itemRef.child("startzeit").setValue(String.format(Locale.GERMAN, "%d:%02d", hourOfDay, minute));
            }
        };
        private TimePickerDialog.OnTimeSetListener onTimeDialogEndZeitCallback= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                itemRef.child("endzeit").setValue(String.format(Locale.GERMAN, "%d:%02d", hourOfDay, minute));
            }
        };
    }
}
