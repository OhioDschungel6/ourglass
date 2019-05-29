package com.othregensburg.ourglass;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.RecyclerAdapter.FirebaseAdapterStundenkorrektur;
import com.othregensburg.ourglass.RecyclerAdapter.FirebaseAdapterStundenuebersicht;
import com.othregensburg.ourglass.RecyclerAdapter.StundenuebersichtAdapter;
import com.othregensburg.ourglass.entity.Arbeitstag;
import com.othregensburg.ourglass.entity.Stamp;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Stundenuebersicht extends AppDrawerBase implements FragmentTagesuebersicht.OnFragmentInteractionListener, FragmentStundeneinteilung.OnFragmentInteractionListener{

    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAdapterStundenuebersicht mAdapter;

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



        //Section RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView_stundenuebersicht);
        Query query = database
                .getReference("arbeitstage/"+user.getUid())
                .orderByKey();
/*
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Arbeitstag a = d.getValue(Arbeitstag.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
         */

        FirebaseRecyclerOptions<Arbeitstag> options =
                new FirebaseRecyclerOptions.Builder<Arbeitstag>()
                        .setQuery(query, Arbeitstag.class)
                        .build();
        mAdapter= new FirebaseAdapterStundenuebersicht(options,this);

        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter.startListening();




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
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
