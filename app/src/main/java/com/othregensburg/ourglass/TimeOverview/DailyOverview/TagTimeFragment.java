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

    private ArrayAdapter<String> taetigkeitAdapter;

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
        return inflater.inflate(R.layout.fragment_stundeneinteilung, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SeekBar seekBarTime = view.findViewById(R.id.seekBar_time);
        Spinner spinnerTaetigkeit = view.findViewById(R.id.spinner_tätigkeit);
        Spinner spinnerProjekt = view.findViewById(R.id.spinner_projekt);
        EditText editTextNotiz = view.findViewById(R.id.editTextMultiline_notiz);

        DatabaseReference refTaetigkeiten = database.getReference("user/" + user.getUid() + "/taetigkeiten");
        refTaetigkeiten.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> taetigkeiten = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    taetigkeiten.add(d.getKey());
                }
                taetigkeiten.add(getString(R.string.fragment_stundeneinteilung_add_taetigkeit));
                taetigkeitAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, taetigkeiten);
                spinnerTaetigkeit.setAdapter(taetigkeitAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: db error handle
            }
        });

        spinnerTaetigkeit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((parent.getItemAtPosition(position)).equals(getString(R.string.fragment_stundeneinteilung_add_taetigkeit))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.fragment_stundeneinteilung_add_taetigkeit);

                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, (ViewGroup) getView(), false);
                    final EditText editTextNewTaetigkeit = viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", (dialog, which) -> {
                        String newTaetigkeit = editTextNewTaetigkeit.getText().toString();
                        database.getReference("user/" + user.getUid() + "/taetigkeiten/" + newTaetigkeit).setValue(true);
                        taetigkeitAdapter.add(newTaetigkeit);
                        taetigkeitAdapter.remove(getString(R.string.fragment_stundeneinteilung_add_taetigkeit));
                        taetigkeitAdapter.add(getString(R.string.fragment_stundeneinteilung_add_taetigkeit));
                        spinnerTaetigkeit.setSelection(taetigkeitAdapter.getPosition(newTaetigkeit));
                    });
                    builder.setNegativeButton("Abbrechen", (dialog, which) -> {
                        dialog.cancel();
                        spinnerTaetigkeit.setSelection(0);
                        if (spinnerTaetigkeit.getSelectedItem().toString().equals(getString(R.string.fragment_stundeneinteilung_add_taetigkeit))) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                    editTextNewTaetigkeit.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if (editable.length() >= 1) {
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

        DatabaseReference refProjekte = database.getReference("/projekte");
        refProjekte.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> projekte = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    projekte.add(d.getKey());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, projekte);
                spinnerProjekt.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: db error handle
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
                //TODO: Snackbar Texte in strings.xml?
                Snackbar.make(getActivity().findViewById(R.id.fragment_stundeneinteilung_frameLayout), "Bitte Zeit angeben!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                ProjectClassification projectClassification = new ProjectClassification(spinnerTaetigkeit.getSelectedItem().toString(), spinnerProjekt.getSelectedItem().toString(), editTextNotiz.getText().toString(), seekBarTime.getProgress());

                String classificationKey = ref.child("einteilung").push().getKey();
                Map<String, Object> updates = new HashMap<>();
                updates.put("arbeitstage/" + user.getUid() + "/" + ref.getKey() + "/einteilung/" + classificationKey, projectClassification);

                DatabaseReference refProjekt = database.getReference("/projekte/" + projectClassification.projekt + "/mitarbeiter/" + user.getUid());
                String path = "projekte/" + projectClassification.projekt + "/mitarbeiter/" + user.getUid() + "/";
                refProjekt.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int oldTime = dataSnapshot.child("zeit").getValue(Integer.class);
                            updates.put(path + "zeit", oldTime + projectClassification.minuten);
                        } else {
                            updates.put(path + "zeit", projectClassification.minuten);
                            updates.put(path + "name", user.getDisplayName());
                        }

                        DataSnapshot dsTaetigkeit = dataSnapshot.child("taetigkeiten/" + projectClassification.taetigkeit);
                        if (!dsTaetigkeit.exists()) {
                            updates.put(path + "taetigkeiten/" + projectClassification.taetigkeit, projectClassification.minuten);
                        } else {
                            int oldTime = dsTaetigkeit.getValue(Integer.class);
                            updates.put(path + "taetigkeiten/" + projectClassification.taetigkeit, oldTime + projectClassification.minuten);
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
