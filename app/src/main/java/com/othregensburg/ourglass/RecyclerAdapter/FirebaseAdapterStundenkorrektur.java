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
        holder.startTime.setTag(position);
        holder.endTime.setTag(position);
        holder.removeButton.setTag(position);
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
        private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        private final FirebaseDatabase database = FirebaseDatabase.getInstance();
        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            startTime=itemView.findViewById(R.id.startTime);
            endTime=itemView.findViewById(R.id.endTime);
            removeButton = itemView.findViewById(R.id.remove);
            startTime.setOnClickListener(h->{

            });
            endTime.setOnClickListener(h->{

            });
            removeButton.setOnClickListener(h->{

            });
        }
        private TimePickerDialog.OnTimeSetListener onTimeDialogCallback= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            }
        };
    }
}
