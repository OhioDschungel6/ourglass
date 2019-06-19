package com.othregensburg.ourglass;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import com.othregensburg.ourglass.RecyclerAdapter.FirebaseAdapterProjektuebersicht;
import com.othregensburg.ourglass.entity.Projektmitglied;

import java.util.ArrayList;
import java.util.List;


public class Projektuebersicht extends AppDrawerBase {

    private static final String ADD_PROJEKT = "neues Projekt hinzufügen";
    private ArrayAdapter<String> projektAdapter;
    private FirebaseAdapterProjektuebersicht mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projektuebersicht);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
                if (ADD_PROJEKT.equals((String) parent.getItemAtPosition(position))) {
                    //TODO: DialogFragment
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.dialog_addProjekt_title);

                    View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add, (ViewGroup) view.getRootView() , false);
                    final EditText editTextNewProject = viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        String newProject = editTextNewProject.getText().toString();
                        //TODO: nicht nur true speichern, sondern entity klasse projekt erstellen und dort abspeichern
                        FirebaseDatabase.getInstance().getReference("projekte/" + newProject).setValue(true);
                        projektAdapter.add(newProject);
                        projektAdapter.remove(ADD_PROJEKT);
                        projektAdapter.add(ADD_PROJEKT);
                        projektSpinner.setSelection(projektAdapter.getPosition(newProject));
                    });
                    builder.setNegativeButton("Abbrechen", (dialog, which) -> {
                        dialog.cancel();
                        projektSpinner.setSelection(0);
                    });
                    builder.show();
                } else {
                    RecyclerView recyclerView = findViewById(R.id.projekt_recycler);
                    String s = ((String) parent.getItemAtPosition(position)) + "/mitarbeiter";
                    Query query = FirebaseDatabase.getInstance()
                            .getReference("projekte/").child(s);

                    FirebaseRecyclerOptions<Projektmitglied> options =
                            new FirebaseRecyclerOptions.Builder<Projektmitglied>()
                                    .setQuery(query, Projektmitglied.class)
                                    .build();
                    recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                    mAdapter = new FirebaseAdapterProjektuebersicht(options);
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
