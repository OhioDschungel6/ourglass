package com.othregensburg.ourglass;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.othregensburg.ourglass.entity.Projekteinteilung;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentStundeneinteilung.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentStundeneinteilung#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentStundeneinteilung extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final String ARG_MINUTES_UNTAGGED = "minutesUntagged";
    private static final String ARG_REF_URL = "refUrl";
    private static final String ARG_MINUTES_WORKED = "minutesWorked";

    private static final String ADD_TAETIGKEIT = "neue Tätigkeit hinzufügen";
    private static final String ADD_PROJEKT = "neues Projekt hinzufügen";

    private int minutesUntagged;
    private int minutesWorekd;
    private DatabaseReference ref;

    private ArrayAdapter<String> taetigkeitAdapter;
    private OnFragmentInteractionListener mListener;

    public FragmentStundeneinteilung() {
        // Required empty public constructor
    }

    public static FragmentStundeneinteilung newInstance(int minutesUntagged, String refUrl, int minutesWorked) {
        FragmentStundeneinteilung fragment = new FragmentStundeneinteilung();
        Bundle args = new Bundle();
        args.putInt(ARG_MINUTES_UNTAGGED, minutesUntagged);
        args.putString(ARG_REF_URL, refUrl);
        args.putInt(ARG_MINUTES_WORKED, minutesWorked);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            minutesUntagged = getArguments().getInt(ARG_MINUTES_UNTAGGED);
            ref = database.getReferenceFromUrl(getArguments().getString(ARG_REF_URL));
            minutesWorekd = getArguments().getInt(ARG_MINUTES_WORKED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stundeneinteilung, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SeekBar seekBarTime = view.findViewById(R.id.seekBar_time);
        Spinner spinnerTaetigkeit = view.findViewById(R.id.spinner_tätigkeit);
        Spinner spinnerProjekt = view.findViewById(R.id.spinner_projekt);
        EditText editTextNotiz = view.findViewById(R.id.editTextMultiline_notiz);

        DatabaseReference refTaetigkeiten = database.getReference("user/"+user.getUid()+"/taetigkeiten");
        refTaetigkeiten.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> taetigkeiten = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    taetigkeiten.add(d.getKey());
                }
                taetigkeiten.add(ADD_TAETIGKEIT);
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
                if(((String) parent.getItemAtPosition(position)).equals(ADD_TAETIGKEIT)) {
                    //TODO: DialogFragment
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.dialog_addTaetigkeit_title);

                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add, (ViewGroup) getView(), false);
                    final EditText editTextNewTaetigkeit = (EditText) viewInflated.findViewById(R.id.editText_new);
                    builder.setView(viewInflated);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newTaetigkeit = editTextNewTaetigkeit.getText().toString();
                            database.getReference("user/"+user.getUid()+"/taetigkeiten/" + newTaetigkeit).setValue(true);
                            taetigkeitAdapter.add(newTaetigkeit);
                            taetigkeitAdapter.remove(ADD_TAETIGKEIT);
                            taetigkeitAdapter.add(ADD_TAETIGKEIT);
                            spinnerTaetigkeit.setSelection(taetigkeitAdapter.getPosition(newTaetigkeit));
                        }
                    });
                    builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            spinnerTaetigkeit.setSelection(0);
                        }
                    });
                    builder.show();
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
                //projekte.add(ADD_PROJEKT);
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
                textViewTime.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        FloatingActionButton fabSave = getView().findViewById(R.id.fab_save);
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seekBarTime.getProgress() == 0) {
                    //TODO: Snackbar Texte in strings.xml?
                    Snackbar.make(getActivity().findViewById(R.id.fragment_stundeneinteilung_frameLayout), "Bitte Zeit angeben!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else {
                    //TODO: multipath update, aber wie neues Element zu einteilungen hinzufügen (was push macht)

                    Projekteinteilung projekteinteilung = new Projekteinteilung(spinnerTaetigkeit.getSelectedItem().toString(), spinnerProjekt.getSelectedItem().toString(), editTextNotiz.getText().toString(), seekBarTime.getProgress());

                    String einteilungKey = ref.child("einteilung").push().getKey();
                    Map<String, Object> updates = new HashMap<>();
                    //userUpdates.put("gracehop/nickname", "Amazing Grace");
                    //userUpdates.put("alanisawesome", new User(null, null, "Alan The Machine"));
                    updates.put("arbeitstage/" + user.getUid() + "/" + ref.getKey() + "/einteilung/" + einteilungKey, projekteinteilung);

                    DatabaseReference refProjekt = database.getReference("/projekte/" + projekteinteilung.projekt + "/" + user.getUid());
                    refProjekt.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                int oldTime = dataSnapshot.child("zeit").getValue(Integer.class);
                                updates.put("projekte/" + projekteinteilung.projekt + "/" + user.getUid() + "/zeit", oldTime + projekteinteilung.minuten);
                            }
                            else {
                                updates.put("projekte/" + projekteinteilung.projekt + "/" + user.getUid() + "/zeit", projekteinteilung.minuten);
                                updates.put("projekte/" + projekteinteilung.projekt + "/" + user.getUid() + "/name", user.getDisplayName());
                            }

                            database.getReference().updateChildren(updates);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
