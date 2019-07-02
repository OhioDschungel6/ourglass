package com.othregensburg.ourglass.ProjectOverview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.AppDrawerBase;
import com.othregensburg.ourglass.Entity.ProjectMember;
import com.othregensburg.ourglass.R;

import java.util.ArrayList;
import java.util.List;


public class ProjectOverviewActivity extends AppDrawerBase {

    private static final String ADD_PROJEKT = "Neues Projekt hinzufügen";
    private ArrayAdapter<String> projektAdapter;
    private FirebaseAdapterProjectOverview mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projektuebersicht);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_projekt);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //spinner
        List<String> projekte = new ArrayList<>();
        Spinner projektSpinner = findViewById(R.id.spinnerProjekt);
        FirebaseDatabase.getInstance().getReference("projekte").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot projekt : dataSnapshot.getChildren()) {
                        projekte.add(projekt.getKey());
                    }
                    projekte.add(ADD_PROJEKT);
                    projektAdapter= new ArrayAdapter<>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, projekte);
                    projektSpinner.setAdapter(projektAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        projektSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ADD_PROJEKT.equals(parent.getItemAtPosition(position))) {
                    //TODO: DialogFragment
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.dialog_addProjekt_title);

                    View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add, (ViewGroup) view.getRootView() , false);
                    final EditText editTextNewProject = viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        String newProject = editTextNewProject.getText().toString();
                        FirebaseDatabase.getInstance().getReference("projekte/" + newProject).setValue(true);
                        projektAdapter.add(newProject);
                        projektAdapter.remove(ADD_PROJEKT);
                        projektAdapter.add(ADD_PROJEKT);
                        projektSpinner.setSelection(projektAdapter.getPosition(newProject));

                        RecyclerView recyclerView = findViewById(R.id.projekt_recycler);
                        String s = (parent.getItemAtPosition(position)) + "/mitarbeiter";
                        Query query = FirebaseDatabase.getInstance()
                                .getReference("projekte/").child(s);

                        FirebaseRecyclerOptions<ProjectMember> options =
                                new FirebaseRecyclerOptions.Builder<ProjectMember>()
                                        .setQuery(query, ProjectMember.class)
                                        .build();
                        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        mAdapter = new FirebaseAdapterProjectOverview(options,getBaseContext());
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.startListening();

                    });
                    builder.setNegativeButton("Abbrechen", (dialog, which) -> {
                        dialog.cancel();
                        projektSpinner.setSelection(0);
                    });
                    builder.show();
                } else {
                    RecyclerView recyclerView = findViewById(R.id.projekt_recycler);
                    String s = (parent.getItemAtPosition(position)) + "/mitarbeiter";
                    Query query = FirebaseDatabase.getInstance()
                            .getReference("projekte/").child(s);

                    FirebaseRecyclerOptions<ProjectMember> options =
                            new FirebaseRecyclerOptions.Builder<ProjectMember>()
                                    .setQuery(query, ProjectMember.class)
                                    .build();
                    recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                    mAdapter = new FirebaseAdapterProjectOverview(options,getBaseContext());
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.startListening();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


}
