package com.othregensburg.ourglass;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.othregensburg.ourglass.entity.Stamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Stundenkorrektur extends AppDrawerBase {

    final Calendar cal = Calendar.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAdapterStundenkorrektur mAdapter;
    private DateFormat df = new SimpleDateFormat("yyMMdd", Locale.GERMANY);
    private DateFormat textFormatter = new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stundenkorrektur);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Plus Button
        FloatingActionButton addTime = findViewById(R.id.addTime);
        addTime.setOnClickListener(e -> {


            DatabaseReference ref = database.getReference(String.format(Locale.GERMAN, "arbeitstage/%s/%s", user.getUid(), df.format(cal.getTime())))
                    .child("timestamps");
            ref.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //todo only use getItemCount
                    CheckBox urlaubsbox = findViewById(R.id.checkBox_urlaub);
                    CheckBox krankbox = findViewById(R.id.checkBox_krank);

                    if (mAdapter.getItemCount() == 0) {
                        if (krankbox.isChecked() || urlaubsbox.isChecked()) {
                            //todo Text ändern
                            Snackbar.make(findViewById(R.id.constraintStundenkorrektur), R.string.stundenkorrektur_checkbox_snackbar, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            return;
                        } else {
                            krankbox.setVisibility(View.INVISIBLE);
                            urlaubsbox.setVisibility(View.INVISIBLE);
                        }
                    }

                    if (dataSnapshot.getValue() != null) {
                        Stamp stamp = null;
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            stamp = d.getValue(Stamp.class);
                        }
                        if (stamp != null) {
                            stamp.startzeit = stamp.endzeit;
                            DatabaseReference newref = ref.push();
                            newref.setValue(stamp);
                        }

                    } else {
                        DatabaseReference newref = ref.push();
                        newref.setValue(new Stamp("08:00", "08:00"));
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
        date.setText(textFormatter.format(cal.getTime()));
        LinearLayout datepickerbar = findViewById(R.id.datepicker_bar);
        datepickerbar.setOnClickListener(e -> {
            DatePickerDialog dpd = new DatePickerDialog(this, dl, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        //Section RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
        Query query = database
                .getReference(String.format(Locale.GERMAN, "arbeitstage/%s/%s", user.getUid(), df.format(cal.getTime())))
                .child("timestamps").orderByChild("startzeit");


        FirebaseRecyclerOptions<Stamp> options =
                new FirebaseRecyclerOptions.Builder<Stamp>()
                        .setQuery(query, Stamp.class)
                        .build();

                mAdapter = new FirebaseAdapterStundenkorrektur(options, findViewById(R.id.constraintStundenkorrektur), findViewById(R.id.checkBox_urlaub), findViewById(R.id.checkBox_krank),
                        database.getReference(String.format(Locale.GERMAN, "arbeitstage/%s/%s", user.getUid(), df.format(cal.getTime()))));

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter.startListening();


    }


    private DatePickerDialog.OnDateSetListener dl = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.YEAR, year);
            TextView date = findViewById(R.id.date);
            date.setText(textFormatter.format(cal.getTime()));

            Query query = database
                    .getReference(String.format(Locale.GERMAN, "arbeitstage/%s/%s", user.getUid(), df.format(cal.getTime())))
                    .child("timestamps")
                    .orderByChild("startzeit");

            FirebaseRecyclerOptions<Stamp> options =
                    new FirebaseRecyclerOptions.Builder<Stamp>()
                            .setQuery(query, Stamp.class)
                            .build();


            mAdapter = new FirebaseAdapterStundenkorrektur(options, findViewById(R.id.constraintStundenkorrektur),
                    findViewById(R.id.checkBox_urlaub), findViewById(R.id.checkBox_krank),
                    database.getReference(String.format(Locale.GERMAN, "arbeitstage/%s/%s", user.getUid(), df.format(cal.getTime()))));
            RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
            recyclerView.setAdapter(mAdapter);
            mAdapter.startListening();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
