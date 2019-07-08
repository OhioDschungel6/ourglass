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
import android.text.Editable;
import android.text.TextWatcher;
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
    private ArrayAdapter<String> projectAdapter;
    private FirebaseAdapterProjectOverview mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_overview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_project);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        //spinner
        List<String> projects = new ArrayList<>();
        Spinner projectSpinner = findViewById(R.id.spinnerProject);
        FirebaseDatabase.getInstance().getReference("projects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot project : dataSnapshot.getChildren()) {
                        projects.add(project.getKey());
                    }
                }
                projects.add(ADD_PROJEKT);
                projectAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, projects);
                projectSpinner.setAdapter(projectAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ADD_PROJEKT.equals(parent.getItemAtPosition(position))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.dialog_addProject_title);

                    View viewInflated = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_add, (ViewGroup) view.getRootView() , false);
                    final EditText editTextNewProject = viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        String newProject = editTextNewProject.getText().toString();
                        FirebaseDatabase.getInstance().getReference("projects/" + newProject).setValue(true);
                        projectAdapter.add(newProject);
                        projectAdapter.remove(ADD_PROJEKT);
                        projectAdapter.add(ADD_PROJEKT);
                        projectSpinner.setSelection(projectAdapter.getPosition(newProject));

                        RecyclerView recyclerView = findViewById(R.id.project_recycler);
                        String s = (parent.getItemAtPosition(position)) + "/employee";
                        Query query = FirebaseDatabase.getInstance()
                                .getReference("projects/").child(s);

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
                        projectSpinner.setSelection(0);
                    });

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                    editTextNewProject.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if (editable.toString().trim().length() >= 1) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            } else {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }
                        }
                    });

                } else {
                    RecyclerView recyclerView = findViewById(R.id.project_recycler);
                    String s = (parent.getItemAtPosition(position)) + "/employee";
                    Query query = FirebaseDatabase.getInstance()
                            .getReference("projects/").child(s);

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
