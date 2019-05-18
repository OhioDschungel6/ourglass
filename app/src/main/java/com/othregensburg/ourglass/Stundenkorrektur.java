package com.othregensburg.ourglass;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Stundenkorrektur extends AppDrawerBase {

    int mYear, mMonth, mDay;
    final Calendar cal = Calendar.getInstance();
    private List<Time> startDates = new LinkedList<>();
    private List<Time> endDates = new LinkedList<>();
    int changeIndex;
    boolean startDateChange;
    TextView changedView;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stundenkorrektur);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton done = findViewById(R.id.save_correct_time);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //ToDo Löschen
        startDates.add(new Time(9, 0, 0));
        startDates.add(new Time(10, 0, 0));
        startDates.add(new Time(12, 0, 0));
        endDates.add(new Time(9, 25, 0));
        endDates.add(new Time(10, 25, 0));
        endDates.add(new Time(13, 10, 0));


        if (startDates.size() == endDates.size()) {
            LinearLayout linearLayout = findViewById(R.id.linLayout);
            LayoutInflater inflater= getLayoutInflater();

            for (int i=0; i<startDates.size();i++) {
                LinearLayout element = (LinearLayout) inflater.inflate(R.layout.entry, linearLayout);
                TextView s = element.getChildAt(i).findViewById(R.id.startTime);
                TextView e = element.getChildAt(i).findViewById(R.id.endTime);
                //element.getChildAt(i).setTag(i);
                s.setText(String.format(Locale.GERMAN,"%2d.%02d", startDates.get(i).getHours(), startDates.get(i).getMinutes()));
                e.setText(String.format(Locale.GERMAN,"%2d.%02d", endDates.get(i).getHours(), endDates.get(i).getMinutes()));
                s.setOnClickListener(h->{
                    LinearLayout r=(LinearLayout)h.getParent();
                    LinearLayout parent = (LinearLayout)r.getParent();
                    changeIndex = parent.indexOfChild(r);
                    startDateChange=true;
                    TimePickerDialog tpd = new TimePickerDialog(this, onTimeDialogCallback, 0, 0, true);
                    tpd.show();
                });
                e.setOnClickListener(h->{
                    LinearLayout r=(LinearLayout)h.getParent();
                    LinearLayout parent = (LinearLayout)r.getParent();
                    changeIndex = parent.indexOfChild(r);
                    startDateChange=false;
                    TimePickerDialog tpd = new TimePickerDialog(this, onTimeDialogCallback, 0, 0, true);
                    tpd.show();
                });
                FloatingActionButton remove = element.getChildAt(i).findViewById(R.id.remove);
                remove.setOnClickListener(g->{
                    LinearLayout r=(LinearLayout)g.getParent();
                    LinearLayout parent = (LinearLayout)r.getParent();
                    int nr = parent.indexOfChild(r);
                    startDates.remove(nr);
                    endDates.remove(nr);
                    parent.removeViewAt(nr);
                });


            }
        }

        //Plus Button
        FloatingActionButton add = findViewById(R.id.addTime);
        add.setOnClickListener(f ->{
            int size=startDates.size();
            if (size > 0) {
                startDates.add(endDates.get(size-1));
                endDates.add(endDates.get(size-1));
            }else{
                startDates.add(new Time(8,0,0));
                endDates.add(new Time(8,0,0));
            }


            LinearLayout linearLayout = findViewById(R.id.linLayout);
            LayoutInflater inflater= getLayoutInflater();

            LinearLayout element = (LinearLayout) inflater.inflate(R.layout.entry, linearLayout);
            TextView s = element.getChildAt(size).findViewById(R.id.startTime);
            TextView e = element.getChildAt(size).findViewById(R.id.endTime);
            //Timepicker
            s.setOnClickListener(h->{
                LinearLayout r=(LinearLayout)h.getParent();
                LinearLayout parent = (LinearLayout)r.getParent();
                 changeIndex = parent.indexOfChild(r);
                 startDateChange=true;
                changedView = (TextView)h;
                TimePickerDialog tpd = new TimePickerDialog(this, onTimeDialogCallback, 0, 0, true);
                tpd.show();
            });
            e.setOnClickListener(h->{
                LinearLayout r=(LinearLayout)h.getParent();
                LinearLayout parent = (LinearLayout)r.getParent();
                changeIndex = parent.indexOfChild(r);
                startDateChange=false;
                changedView = (TextView)h;
                TimePickerDialog tpd = new TimePickerDialog(this, onTimeDialogCallback, 0, 0, true);
                tpd.show();
            });



            s.setText(String.format(Locale.GERMAN,"%2d.%02d", startDates.get(size).getHours(), startDates.get(size).getMinutes()));
            e.setText(String.format(Locale.GERMAN,"%2d.%02d", endDates.get(size).getHours(), endDates.get(size).getMinutes()));
            FloatingActionButton remove = element.getChildAt(size).findViewById(R.id.remove);
            remove.setOnClickListener(g->{
                LinearLayout r=(LinearLayout)g.getParent();
                LinearLayout parent = (LinearLayout)r.getParent();
                int nr = parent.indexOfChild(r);
                startDates.remove(nr);
                endDates.remove(nr);
                parent.removeViewAt(nr);
            });

        });





        //DrawerLayout
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
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

    private TimePickerDialog.OnTimeSetListener onTimeDialogCallback= new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (startDateChange) {
                startDates.get(changeIndex).setHours(hourOfDay);
                startDates.get(changeIndex).setMinutes(minute);
            } else {
                endDates.get(changeIndex).setHours(hourOfDay);
                endDates.get(changeIndex).setMinutes(minute);
            }
            changedView.setText(String.format(Locale.GERMAN,"%2d.%02d", hourOfDay, minute));
        }
    };

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
