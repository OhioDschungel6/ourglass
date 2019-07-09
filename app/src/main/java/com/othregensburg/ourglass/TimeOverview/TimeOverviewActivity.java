package com.othregensburg.ourglass.TimeOverview;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.othregensburg.ourglass.AppDrawerBase;
import com.othregensburg.ourglass.Entity.Workday;
import com.othregensburg.ourglass.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TimeOverviewActivity extends AppDrawerBase {

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAdapterTimeOverview mAdapter;
    private Calendar firstDate = Calendar.getInstance();
    private Calendar secondDate = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener dateDialogFirstDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.YEAR, year);
            if (c.compareTo(secondDate) < 0) {
                firstDate = c;
                TextView textView_firstDate = findViewById(R.id.date1);
                DateFormat df = new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
                textView_firstDate.setText(df.format(firstDate.getTime()));


                //new Query
                DateFormat queryDate = new SimpleDateFormat("yyMMdd", Locale.GERMANY);
                RecyclerView recyclerView = findViewById(R.id.recyclerView_time_overview);
                Query query = database
                        .getReference("workdays/" + user.getUid())
                        .orderByKey().startAt(queryDate.format(firstDate.getTime())).endAt(queryDate.format(secondDate.getTime()));

                FirebaseRecyclerOptions<Workday> options =
                        new FirebaseRecyclerOptions.Builder<Workday>()
                                .setQuery(query, Workday.class)
                                .build();
                mAdapter = new FirebaseAdapterTimeOverview(options, view.getContext());
                recyclerView.setAdapter(mAdapter);
                mAdapter.startListening();
            }
        }
    };
    private DatePickerDialog.OnDateSetListener dateDialogSecondDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.YEAR, year);
            if (c.compareTo(firstDate) > 0) {
                secondDate = c;
                TextView textView_secondDate = findViewById(R.id.date2);
                DateFormat df = new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
                textView_secondDate.setText(df.format(secondDate.getTime()));


                //new Query
                DateFormat queryDate = new SimpleDateFormat("yyMMdd", Locale.GERMANY);
                RecyclerView recyclerView = findViewById(R.id.recyclerView_time_overview);
                Query query = database
                        .getReference("workdays/" + user.getUid())
                        .orderByKey().startAt(queryDate.format(firstDate.getTime())).endAt(queryDate.format(secondDate.getTime()));

                FirebaseRecyclerOptions<Workday> options =
                        new FirebaseRecyclerOptions.Builder<Workday>()
                                .setQuery(query, Workday.class)
                                .build();
                mAdapter = new FirebaseAdapterTimeOverview(options, getBaseContext());
                recyclerView.setAdapter(mAdapter);
                mAdapter.startListening();
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_overview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_time_overview);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //setDates
        firstDate.set(Calendar.DAY_OF_MONTH, 1);
        TextView textView_firstDate = findViewById(R.id.date1);
        TextView textView_secondDate = findViewById(R.id.date2);
        DateFormat df= new SimpleDateFormat("dd.MM.yy", Locale.GERMANY);
        textView_firstDate.setText(df.format(firstDate.getTime()));
        textView_secondDate.setText(df.format(secondDate.getTime()));


        //setDatePicker for Datepickerbar
        LinearLayout c1 = findViewById(R.id.datePickerLeft);
        c1.setOnClickListener(e->{
            DatePickerDialog dpd = new DatePickerDialog(this, dateDialogFirstDate, firstDate.get(Calendar.YEAR), firstDate.get(Calendar.MONTH), firstDate.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
        LinearLayout c2 = findViewById(R.id.datePickerRight);
        c2.setOnClickListener(e->{
            DatePickerDialog dpd = new DatePickerDialog(this, dateDialogSecondDate, secondDate.get(Calendar.YEAR), secondDate.get(Calendar.MONTH), secondDate.get(Calendar.DAY_OF_MONTH));

            dpd.show();
        });

        //Section RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_time_overview);
        DateFormat queryDate = new SimpleDateFormat("yyMMdd", Locale.GERMANY);

        Query query = database
                .getReference("workdays/" + user.getUid())
                .orderByKey().startAt(queryDate.format(firstDate.getTime())).endAt(queryDate.format(secondDate.getTime()));

        FirebaseRecyclerOptions<Workday> options =
                new FirebaseRecyclerOptions.Builder<Workday>()
                        .setQuery(query, Workday.class)
                        .build();
        mAdapter= new FirebaseAdapterTimeOverview(options,this);

        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        mAdapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerView = findViewById(R.id.recyclerView_time_overview);
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }
}
