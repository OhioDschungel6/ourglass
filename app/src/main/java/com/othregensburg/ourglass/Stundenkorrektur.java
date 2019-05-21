package com.othregensburg.ourglass;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Stundenkorrektur extends AppDrawerBase {

    int mYear, mMonth, mDay;
    final Calendar cal = Calendar.getInstance();
    private List<Pair<Time,Time>> dates = new ArrayList<>();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stundenkorrektur);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ToDo Löschen
        dates.add(new Pair<>(new Time(9, 0, 0),new Time(9, 25, 0)));
        dates.add(new Pair<>(new Time(12, 0, 0),new Time(12, 25, 0)));
        dates.add(new Pair<>(new Time(13, 10, 0),new Time(13, 25, 0)));

        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenkorrektur);
        StundenkorrekturAdapter mAdapter = new StundenkorrekturAdapter(this, dates);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //Plus Button
        FloatingActionButton addTime = findViewById(R.id.addTime);
        addTime.setOnClickListener(e->{
            Time second=dates.get(dates.size()-1).second;
            dates.add(new Pair<>(new Time(second.getTime()), new Time(second.getTime())));
            mAdapter.notifyDataSetChanged();
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
        }
    };


}
