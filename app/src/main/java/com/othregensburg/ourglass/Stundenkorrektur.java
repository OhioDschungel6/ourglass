package com.othregensburg.ourglass;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.RecyclerAdapter.FirebaseAdapterStundenkorrektur;
import com.othregensburg.ourglass.entity.Arbeitstag;
import com.othregensburg.ourglass.entity.Stamp;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Stundenkorrektur extends AppDrawerBase {

    int mYear, mMonth, mDay;
    final Calendar cal = Calendar.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAdapterStundenkorrektur mAdapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stundenkorrektur);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Section RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
        Query query = database
                .getReference("arbeitstage/"+user.getUid()+String.format(Locale.GERMAN,"/%02d%02d%02d",mYear,mMonth,mDay))
                .child("timestamps");

        FirebaseRecyclerOptions<Stamp> options =
                new FirebaseRecyclerOptions.Builder<Stamp>()
                        .setQuery(query, Stamp.class)
                        .build();
        mAdapter= new FirebaseAdapterStundenkorrektur(options);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter.startListening();




        //Plus Button
        FloatingActionButton addTime = findViewById(R.id.addTime);
        addTime.setOnClickListener(e->{
            DatabaseReference ref =database.getReference(String.format(Locale.GERMAN,"arbeitstage/%s/%02d%02d%02d",user.getUid(),mYear-2000,mMonth,mDay))
                    .child("timestamps");
            ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Stamp stamp=null;
                        for (DataSnapshot d :dataSnapshot.getChildren()) {
                            stamp= d.getValue(Stamp.class);
                        }
                        if (stamp != null) {
                            stamp.startzeit=stamp.endzeit;
                            DatabaseReference newref=ref.push();
                            newref.setValue(stamp);
                        }

                    }else{
                        DatabaseReference newref=ref.push();
                        //newref.setValue(new Stamp("8:00","8:00"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        });




        //DrawerLayout
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_korrektur);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //Datepicker
        TextView date = findViewById(R.id.date);
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH) + 1;
        mDay = cal.get(Calendar.DAY_OF_MONTH);
        date.setText(String.format(Locale.GERMAN, "%d.%d.%d", mDay, mMonth, mYear));
        LinearLayout datepickerbar = findViewById(R.id.datepicker_bar);
        datepickerbar.setOnClickListener(e -> {
            DatePickerDialog dpd = new DatePickerDialog(this, dl, mYear, mMonth - 1, mDay);
            dpd.show();
        });


    }



    private DatePickerDialog.OnDateSetListener dl = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mYear = year;
            mMonth = month + 1;
            mDay = dayOfMonth;
            TextView date = findViewById(R.id.date);
            date.setText(String.format(Locale.GERMAN, "%d.%d.%d", mDay, mMonth, mYear));
            //TODO year-2000 disgusting and may clean up old adapter

            Query query = database
                    .getReference("arbeitstage/"+user.getUid()+String.format(Locale.GERMAN,"/%02d%02d%02d",mYear-2000,mMonth,mDay))
                    .child("timestamps");

            FirebaseRecyclerOptions<Stamp> options =
                    new FirebaseRecyclerOptions.Builder<Stamp>()
                            .setQuery(query, Stamp.class)
                            .build();


            mAdapter=new FirebaseAdapterStundenkorrektur(options);
            RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
            recyclerView.setAdapter(mAdapter);
            mAdapter.startListening();
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
