package com.othregensburg.ourglass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.entity.Projekteinteilung;
import com.othregensburg.ourglass.entity.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentTagesuebersicht.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentTagesuebersicht#newInstance} factory method to
 * create an instance of this fragment.
 */

public class FragmentTagesuebersicht extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final int PIE_CHART_TEXTSIZE = 14;
    private static final String LABEL_MINUTES_UNTAGGED = "Nicht eingeteilt";

    private static final String ARG_REF_URL = "refUrl";
    private static final String ARG_MINUTES_WORKED = "minutesWorked";

    private DatabaseReference ref;
    private Date date;
    private int minutesWorked;
    private int minutesUntagged;
    private int nextColor = 0;

    private OnFragmentInteractionListener mListener;

    public FragmentTagesuebersicht() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentTagesuebersicht.
     */

    public static FragmentTagesuebersicht newInstance(String refUrl, int minutesWorked) {
        FragmentTagesuebersicht fragment = new FragmentTagesuebersicht();
        Bundle args = new Bundle();
        args.putString(ARG_REF_URL, refUrl);
        args.putInt(ARG_MINUTES_WORKED, minutesWorked);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String refUrl = getArguments().getString(ARG_REF_URL);
            minutesWorked = getArguments().getInt(ARG_MINUTES_WORKED);
            minutesUntagged = minutesWorked;
            ref = database.getReferenceFromUrl(refUrl);
            String key = ref.getKey();
            date = new Date(Integer.parseInt(key.substring(0,2))+100,Integer.parseInt(key.substring(2,4))-1,Integer.parseInt(key.substring(4)));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tagesuebersicht, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewDate = getView().findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("EEEE dd.MM.yy", Locale.GERMANY);
        textViewDate.setText(df.format(date));

        DatabaseReference refEinteilungen = ref.child("/einteilung");


        refEinteilungen.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                minutesUntagged = minutesWorked;
                Map<String, Integer> mapTaetigkeiten = new HashMap<>();

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Projekteinteilung einteilung = d.getValue(Projekteinteilung.class);
                    int minutesTaetigkeit = mapTaetigkeiten.getOrDefault(einteilung.taetigkeit,0);
                    minutesTaetigkeit += einteilung.minuten;
                    mapTaetigkeiten.put(einteilung.taetigkeit, minutesTaetigkeit);
                    minutesUntagged -= einteilung.minuten;
                }
                List<SliceValue> sliceValues = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : mapTaetigkeiten.entrySet()) {
                    sliceValues.add(new SliceValue(entry.getValue(), getNextColor()).setLabel(entry.getKey()));
                }
                if (minutesUntagged > 0) {
                    sliceValues.add(new SliceValue(minutesUntagged, Color.LTGRAY).setLabel(LABEL_MINUTES_UNTAGGED));
                }
                PieChartData pieChartData = new PieChartData(sliceValues);
                pieChartData.setHasLabels(true).setValueLabelTextSize(PIE_CHART_TEXTSIZE);
                Time timeWorked = new Time(minutesWorked);
                //Set size and color of font in the middle:
                //pieChartData.setHasCenterCircle(true).setCenterText1("Sales in million").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
                pieChartData.setHasCenterCircle(true).setCenterText1(timeWorked.toString());
                PieChartView pieChartView = getView().findViewById(R.id.pie_chart);
                pieChartView.setPieChartData(pieChartData);
                pieChartView.refreshDrawableState();

                pieChartView.setOnValueTouchListener(new PieChartOnValueSelectListener() {
                    @Override
                    public void onValueSelected(int arcIndex, SliceValue value) {
                        String label = String.copyValueOf(value.getLabelAsChars());
                        if(label.equals(LABEL_MINUTES_UNTAGGED)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(LABEL_MINUTES_UNTAGGED);
                            Time timeUntagged = new Time(minutesUntagged);
                            TextView textViewUntagged = new TextView(getContext());
                            textViewUntagged.setText("Zeit: " + timeUntagged.toString());
                            textViewUntagged.setTextSize(18);
                            textViewUntagged.setPadding(80, 32  , 0, 0);
                            builder.setView(textViewUntagged);
                            builder.setPositiveButton("Einteilen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switchToFragmentStundeneinteilung();
                                }
                            });
                            builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                        else {
                            Query selected = ref.child("einteilung").orderByChild("taetigkeit").equalTo(label);
                            selected.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle(label);

                                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taetigkeit_details, (ViewGroup) getView(), false);
                                    LinearLayout einteilungenList = viewInflated.findViewById(R.id.einteilungen_list);

                                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                                        Projekteinteilung einteilung = d.getValue(Projekteinteilung.class);
                                        //attach to root?
                                        View element = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taetigkeit_details_entry, einteilungenList, false);
                                        ((TextView) element.findViewById(R.id.textView_projekt)).setText(einteilung.projekt);
                                        ((TextView) element.findViewById(R.id.textView_notiz)).setText(einteilung.notiz);
                                        String stringTime = new Time(einteilung.minuten).toString();
                                        TextView textViewTime = element.findViewById(R.id.dialog_taetigkeit_textView_time);
                                        textViewTime.setText(stringTime);
                                        einteilungenList.addView(element);
                                        //TODO: Bearbeiten der Einteilungen möglich machen

                                        element.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ConstraintLayout editLayout = element.findViewById(R.id.dialog_taetigkeit_layout_edit);
                                                SeekBar seekBar = element.findViewById(R.id.seekBar_editTime);
                                                FloatingActionButton fabSave = element.findViewById(R.id.button_save);
                                                seekBar.setMax(einteilung.minuten + minutesUntagged);
                                                seekBar.setProgress(einteilung.minuten);
                                                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                    @Override
                                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
                                                editLayout.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                    builder.setView(viewInflated);

                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onValueDeselected() {

                    }
                });

                FloatingActionButton fabStundeneinteilung = getView().findViewById(R.id.fab_stundeneinteilung);
                if(minutesUntagged == 0) {
                    fabStundeneinteilung.setEnabled(false);
                    fabStundeneinteilung.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                    fabStundeneinteilung.setAlpha(0.4f);
                }
                else {
                    fabStundeneinteilung.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switchToFragmentStundeneinteilung();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Todo: DatabaseError
            }
        });
    }

    private void switchToFragmentStundeneinteilung(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = FragmentStundeneinteilung.newInstance(minutesUntagged, ref.toString(), minutesWorked);
        fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private int getNextColor () {
        int color = ContextCompat.getColor(getContext(),R.color.colorPrimaryDark);;
        switch (nextColor%4) {
            case 0: color = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
                break;
            case 1: color = ContextCompat.getColor(getContext(),R.color.colorAccent);
                break;
            case 2: color = ContextCompat.getColor(getContext(),R.color.colorPrimaryDark);
                break;
            case 3: color = ContextCompat.getColor(getContext(),R.color.pieChart_thirdColor);
        }
        nextColor++;
        return color;
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
