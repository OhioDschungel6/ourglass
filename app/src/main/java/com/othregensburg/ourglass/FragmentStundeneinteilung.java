package com.othregensburg.ourglass;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


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
                String[] taetigkeitenArray = taetigkeiten.toArray(new String[0]);
                taetigkeitAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, taetigkeitenArray);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.dialog_addTaetigkeit_title);

                    final EditText newTaetigkeit = new EditText(getContext());
                    builder.setView(newTaetigkeit);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            database.getReference("user/"+user.getUid()+"/taetigkeiten/" + newTaetigkeit.getText().toString()).setValue(true);
                            spinnerTaetigkeit.setSelection(taetigkeitAdapter.getPosition(newTaetigkeit.getText().toString()));
                        }
                    });
                    builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
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
                String[] projekteArray = projekte.toArray(new String[0]);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, projekteArray);
                spinnerProjekt.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //TODO: db error handle
            }
        });

        /*
        spinnerProjekt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(((String) parent.getItemAtPosition(position)).equals(ADD_PROJEKT)) {
                    //add project
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */

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
                    DatabaseReference einteilung = ref.child("einteilung").push();
                    einteilung.setValue(new Projekteinteilung(spinnerTaetigkeit.getSelectedItem().toString(), spinnerProjekt.getSelectedItem().toString(), editTextNotiz.getText().toString(), seekBarTime.getProgress()));

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    //TODO: Ordner anim in res und integers.xml in values ist aus Musterlösung zur Fragmentsübung übernommen
                    fragmentTransaction.setCustomAnimations(R.anim.alpha_transition_in, R.anim.alpha_transition_out);
                    Fragment fragment = FragmentTagesuebersicht.newInstance(ref.toString(), minutesWorekd);
                    fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);
                    fragmentTransaction.commit();
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
