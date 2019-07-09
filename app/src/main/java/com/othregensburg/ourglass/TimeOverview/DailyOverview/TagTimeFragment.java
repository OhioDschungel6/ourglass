package com.othregensburg.ourglass.TimeOverview.DailyOverview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.Entity.ProjectClassification;
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagTimeFragment extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final String ARG_MINUTES_UNTAGGED = "minutesUntagged";
    private static final String ARG_REF_URL = "refUrl";

    private int minutesUntagged;
    private DatabaseReference ref;

    private ArrayAdapter<String> activityAdapter;

    public TagTimeFragment() {
        // Required empty public constructor
    }

    public static TagTimeFragment newInstance(int minutesUntagged, String refUrl) {
        TagTimeFragment fragment = new TagTimeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MINUTES_UNTAGGED, minutesUntagged);
        args.putString(ARG_REF_URL, refUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            minutesUntagged = getArguments().getInt(ARG_MINUTES_UNTAGGED);
            ref = database.getReferenceFromUrl(getArguments().getString(ARG_REF_URL));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag_time, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SeekBar seekBarTime = view.findViewById(R.id.seekBar_time);
        Spinner spinnerActivity = view.findViewById(R.id.spinner_tätigkeit);
        Spinner spinnerProject = view.findViewById(R.id.spinner_project);
        EditText editTextNote = view.findViewById(R.id.editTextMultiline_note);

        DatabaseReference refActivities = database.getReference("user/" + user.getUid() + "/activities");
        refActivities.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> activities = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    activities.add(d.getKey());
                }
                activities.add(getString(R.string.fragment_tag_time_add_activity));
                activityAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, activities);
                spinnerActivity.setAdapter(activityAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((parent.getItemAtPosition(position)).equals(getString(R.string.fragment_tag_time_add_activity))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.fragment_tag_time_add_activity);

                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, (ViewGroup) getView(), false);
                    final EditText editTextNewActivity = viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        String newActivity = editTextNewActivity.getText().toString();
                        database.getReference("user/" + user.getUid() + "/activities/" + newActivity).setValue(true);
                        activityAdapter.add(newActivity);
                        activityAdapter.remove(getString(R.string.fragment_tag_time_add_activity));
                        activityAdapter.add(getString(R.string.fragment_tag_time_add_activity));
                        spinnerActivity.setSelection(activityAdapter.getPosition(newActivity));
                    });
                    builder.setNegativeButton("Abbrechen", (dialog, which) -> {
                        dialog.cancel();
                        spinnerActivity.setSelection(0);
                        if (spinnerActivity.getSelectedItem().toString().equals(getString(R.string.fragment_tag_time_add_activity))) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                    editTextNewActivity.addTextChangedListener(new TextWatcher() {
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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        DatabaseReference refProjects = database.getReference("/projects");
        refProjects.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> projects = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    projects.add(d.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, projects);
                spinnerProject.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        seekBarTime.setMax(minutesUntagged);
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView textViewTime = getView().findViewById(R.id.textView_time);
                Time time = new Time(progress);
                textViewTime.setText(time.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        FloatingActionButton fabSave = getView().findViewById(R.id.fab_save);
        fabSave.setOnClickListener(v -> {
            if (seekBarTime.getProgress() == 0) {
                Snackbar.make(getActivity().findViewById(R.id.fragment_tag_time_frameLayout), R.string.fragment_tag_time_no_time_snackbar_text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                ProjectClassification projectClassification = new ProjectClassification(spinnerActivity.getSelectedItem().toString(), spinnerProject.getSelectedItem().toString(), editTextNote.getText().toString(), seekBarTime.getProgress());

                String classificationKey = ref.child("classification").push().getKey();
                Map<String, Object> updates = new HashMap<>();
                updates.put("workdays/" + user.getUid() + "/" + ref.getKey() + "/classification/" + classificationKey, projectClassification);

                DatabaseReference refProject = database.getReference("/projects/" + projectClassification.project + "/employee/" + user.getUid());
                String path = "projects/" + projectClassification.project + "/employee/" + user.getUid() + "/";
                refProject.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int oldTime = dataSnapshot.child("time").getValue(Integer.class);
                            updates.put(path + "time", oldTime + projectClassification.minutes);
                        } else {
                            updates.put(path + "time", projectClassification.minutes);
                            updates.put(path + "name", user.getDisplayName());
                        }

                        DataSnapshot dsActivity = dataSnapshot.child("activities/" + projectClassification.activity);
                        if (!dsActivity.exists()) {
                            updates.put(path + "activities/" + projectClassification.activity, projectClassification.minutes);
                        } else {
                            int oldTime = dsActivity.getValue(Integer.class);
                            updates.put(path + "activities/" + projectClassification.activity, oldTime + projectClassification.minutes);
                        }

                        database.getReference().updateChildren(updates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
