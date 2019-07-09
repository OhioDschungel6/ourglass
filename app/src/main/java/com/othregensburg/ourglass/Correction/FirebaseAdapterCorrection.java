package com.othregensburg.ourglass.Correction;

import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.Entity.ProjectClassification;
import com.othregensburg.ourglass.Entity.Stamp;
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.R;

import java.util.Locale;

public class FirebaseAdapterCorrection extends FirebaseRecyclerAdapter<Stamp, FirebaseAdapterCorrection.ViewHolder> {

    private ConstraintLayout constraintLayout;
    private CheckBox holidayBox;
    private CheckBox illBox;

    public FirebaseAdapterCorrection(@NonNull FirebaseRecyclerOptions<Stamp> options, ConstraintLayout cl, CheckBox holidayBox, CheckBox illBox, DatabaseReference reference) {
        super(options);
        constraintLayout = cl;
        this.holidayBox = holidayBox;
        this.illBox = illBox;

        this.holidayBox.setVisibility(View.VISIBLE);
        this.illBox.setVisibility(View.VISIBLE);

        reference.child("holiday").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean checked = dataSnapshot.getValue(Boolean.class);
                    holidayBox.setChecked(checked);
                    illBox.setEnabled(!checked);
                } else {
                    holidayBox.setChecked(false);
                    illBox.setEnabled(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.child("ill").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean checked = dataSnapshot.getValue(Boolean.class);
                    illBox.setChecked(checked);
                    holidayBox.setEnabled(!checked);
                } else {
                    illBox.setChecked(false);
                    holidayBox.setEnabled(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        this.holidayBox.setOnClickListener(e -> {
            if (this.holidayBox.isChecked()) {
                this.illBox.setEnabled(false);
                reference.child("holiday").setValue(this.holidayBox.isChecked());
            } else {
                this.illBox.setEnabled(true);
                reference.setValue(null);
            }


        });
        this.illBox.setOnClickListener(e -> {
            if (this.illBox.isChecked()) {
                this.holidayBox.setEnabled(false);
                reference.child("ill").setValue(this.illBox.isChecked());
            } else {
                this.holidayBox.setEnabled(true);
                reference.setValue(null);
            }

        });



    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Stamp model) {
        holder.startTime.setText(model.start);
        holder.endTime.setText(model.end);
        holder.model = model;
        holder.itemRef = getRef(position);
        holidayBox.setVisibility(View.INVISIBLE);
        illBox.setVisibility(View.INVISIBLE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        View mItemView = mInflater.inflate(R.layout.correction_entry, viewGroup, false);
        return new FirebaseAdapterCorrection.ViewHolder(mItemView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Stamp model;
        final TextView startTime;
        final TextView endTime;
        final FloatingActionButton removeButton;
        DatabaseReference itemRef;

        private TimePickerDialog.OnTimeSetListener onTimeDialogStartTimeCallback = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (endTime.getText().toString().compareTo(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute)) >= 0) {
                    if (startTime.getText().toString().compareTo(String.format(Locale.GERMAN,"%02d:%02d", hourOfDay, minute))<0) {
                        DatabaseReference databaseReference = getRef(getLayoutPosition()).getParent().getParent().child("classification");
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int min=0;
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    ProjectClassification classification = d.getValue(ProjectClassification.class);
                                    min+= classification.minutes;
                                }
                                if (min == 0) {
                                    itemRef.child("start").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));
                                } else {
                                    DatabaseReference reference=databaseReference.getParent().child("timestamps");
                                    int finalMin = min;
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Time t = new Time();
                                            for (DataSnapshot d2 : dataSnapshot.getChildren()) {
                                                Stamp stamp = d2.getValue(Stamp.class);
                                                t.add(stamp);

                                            }
                                            Time t2 = new Time();
                                            t2.add(new Stamp(startTime.getText().toString(),String.format(Locale.GERMAN,"%02d:%02d", hourOfDay, minute)));
                                            if (finalMin <= t.getMinutes()-t2.getMinutes()) {
                                                itemRef.child("start").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));
                                            } else {
                                                //TODO Snackbar
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return;
                    }
                    itemRef.child("start").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));



                    notifyDataSetChanged();
                } else {

                    Snackbar.make(constraintLayout, "Startzeit kann nicht nach der Endzeit sein!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        };
        private TimePickerDialog.OnTimeSetListener onTimeDialogEndTimeCallback = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (startTime.getText().toString().compareTo(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute)) <= 0) {
                    if (endTime.getText().toString().compareTo(String.format(Locale.GERMAN,"%02d:%02d", hourOfDay, minute))>0) {
                        DatabaseReference databaseReference = getRef(getLayoutPosition()).getParent().getParent().child("classification");
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int min=0;
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    ProjectClassification classification = d.getValue(ProjectClassification.class);
                                    min+= classification.minutes;
                                }
                                if (min == 0) {
                                    itemRef.child("end").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));
                                } else {
                                    DatabaseReference reference=databaseReference.getParent().child("timestamps");
                                    int finalMin = min;
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Time t = new Time();
                                            for (DataSnapshot d2 : dataSnapshot.getChildren()) {
                                                Stamp stamp = d2.getValue(Stamp.class);
                                                t.add(stamp);

                                            }
                                            Time t2 = new Time();
                                            t2.add(new Stamp(String.format(Locale.GERMAN,"%02d:%02d", hourOfDay, minute), endTime.getText().toString()));
                                            if (finalMin <= t.getMinutes()-t2.getMinutes()) {
                                                itemRef.child("end").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));
                                            } else {
                                                //TODO Snackbar
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return;
                    }

                    itemRef.child("end").setValue(String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute));

                } else {
                    Snackbar.make(constraintLayout, "Endzeit kann nicht vor der Startzeit sein!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        };
        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);
            removeButton = itemView.findViewById(R.id.remove);
            startTime.setOnClickListener(h -> {
                Pair<Integer, Integer> pair = model.pairStarttime();
                TimePickerDialog tpd = new TimePickerDialog(itemView.getContext(), onTimeDialogStartTimeCallback, pair.first, pair.second, true);
                tpd.show();
            });
            endTime.setOnClickListener(h -> {
                Pair<Integer, Integer> pair = model.pairEndtime();
                TimePickerDialog tpd = new TimePickerDialog(itemView.getContext(), onTimeDialogEndTimeCallback, pair.first, pair.second, true);
                tpd.show();
            });
            removeButton.setOnClickListener(h -> {

                DatabaseReference databaseReference = getRef(getLayoutPosition()).getParent().getParent().child("classification");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int min=0;
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            ProjectClassification classification = d.getValue(ProjectClassification.class);
                            min+= classification.minutes;
                        }
                        if (min == 0) {
                            if (getItemCount() == 1) {
                                holidayBox.setVisibility(View.VISIBLE);
                                illBox.setVisibility(View.VISIBLE);
                            }
                            if ("".equals(endTime.getText().toString())) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getUid() + "/timeRunning");
                                ref.setValue(false);
                            }
                            itemRef.setValue(null);
                        } else {
                            DatabaseReference reference=databaseReference.getParent().child("timestamps");
                            int finalMin = min;
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Time t = new Time();
                                    for (DataSnapshot d2 : dataSnapshot.getChildren()) {
                                        Stamp stamp = d2.getValue(Stamp.class);
                                        t.add(stamp);

                                    }
                                    Time t2 = new Time();
                                    t2.add(new Stamp(startTime.getText().toString(), endTime.getText().toString()));
                                    if (finalMin <= t.getMinutes()-t2.getMinutes()) {
                                        if (getItemCount() == 1) {
                                            holidayBox.setVisibility(View.VISIBLE);
                                            illBox.setVisibility(View.VISIBLE);
                                        }
                                        if ("".equals(endTime.getText().toString())) {
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getUid() + "/timeRunning");
                                            ref.setValue(false);
                                        }
                                        itemRef.setValue(null);
                                    } else {
                                        //TODO Snackbar
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            });

        }
    }
}
