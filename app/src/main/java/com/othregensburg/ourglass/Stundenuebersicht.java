package com.othregensburg.ourglass;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;

import com.othregensburg.ourglass.RecyclerAdapter.StundenuebersichtAdapter;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Stundenuebersicht extends AppDrawerBase implements FragmentTagesuebersicht.OnFragmentInteractionListener, FragmentStundeneinteilung.OnFragmentInteractionListener{

    private List<Date> dates;
    private Map<Date, List<Pair<Time, Time>>> times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stundenuebersicht);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_stundenuebersicht);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //todo wieder löschen
        dates = new ArrayList<>();
        times = new HashMap<>();

        dates.add(new Date(2019, 2, 20));
        dates.add(new Date(2019, 2, 21));
        dates.add(new Date(2019, 2, 22));
        dates.add(new Date(2019, 2, 23));
        dates.add(new Date(2019, 2, 24));

        List<Pair<Time,Time>> l1 = new ArrayList<>();
        l1.add(new Pair<Time,Time>(new Time(9,0,0),new Time(12,0,0)));
        l1.add(new Pair<Time,Time>(new Time(13,0,0),new Time(13,10,0)));
        l1.add(new Pair<Time,Time>(new Time(13,25,0),new Time(13,50,0)));
        times.put(dates.get(0), l1);

        List<Pair<Time,Time>> l2 = new ArrayList<>();
        l2.add(new Pair<Time,Time>(new Time(9,0,0),new Time(12,0,0)));
        l2.add(new Pair<Time,Time>(new Time(13,0,0),new Time(13,10,0)));
        l2.add(new Pair<Time,Time>(new Time(13,25,0),new Time(13,50,0)));
        times.put(dates.get(1), l2);

        List<Pair<Time,Time>> l3 = new ArrayList<>();
        l3.add(new Pair<Time,Time>(new Time(9,0,0),new Time(12,0,0)));
        l3.add(new Pair<Time,Time>(new Time(13,0,0),new Time(13,10,0)));
        times.put(dates.get(2), l3);

        List<Pair<Time,Time>> l4 = new ArrayList<>();
        l4.add(new Pair<Time,Time>(new Time(9,0,0),new Time(12,0,0)));
        l4.add(new Pair<Time,Time>(new Time(13,0,0),new Time(13,10,0)));
        l4.add(new Pair<Time,Time>(new Time(13,25,0),new Time(13,50,0)));
        l4.add(new Pair<Time,Time>(new Time(14,10,0),new Time(16,20,0)));
        times.put(dates.get(3), l4);


        //recyclerMainscreen
        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenuebersicht);
        StundenuebersichtAdapter mAdapter = new StundenuebersichtAdapter(this, dates,times);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //TODO: Ordner anim in res und integers.xml in values ist aus Musterlösung zur Fragmentsübung übernommen
        fragmentTransaction.setCustomAnimations(R.anim.alpha_transition_in, R.anim.alpha_transition_out);
        //TODO: Testdaten, später vom aufrufenden Eintrag aus der Stundenübersicht übernehmen
        //Fragment fragment = FragmentTagesuebersicht.newInstance(1, 1, 2019);
        //fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);

        fragmentTransaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
