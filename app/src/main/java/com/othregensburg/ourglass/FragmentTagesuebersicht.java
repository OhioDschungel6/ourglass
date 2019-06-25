package com.othregensburg.ourglass;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;


public class FragmentTagesuebersicht extends Fragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final int PIE_CHART_TEXTSIZE = 14;

    private static final String ARG_REF_URL = "refUrl";
    private static final String ARG_MINUTES_WORKED = "minutesWorked";

    private DatabaseReference ref;
    private Calendar date;
    private int minutesWorked;
    private int minutesUntagged;
    private int nextColor = 0;

    public FragmentTagesuebersicht() {
        // Required empty public constructor
    }

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
            date = new GregorianCalendar(Integer.parseInt(key.substring(0,2)) + 2000,Integer.parseInt(key.substring(2,4))-1,Integer.parseInt(key.substring(4)));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tagesuebersicht, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewDate = getView().findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("EEEE dd.MM.yy", Locale.GERMANY);
        textViewDate.setText(df.format(date.getTime()));

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
                    sliceValues.add(new SliceValue(minutesUntagged, Color.LTGRAY).setLabel(getString(R.string.fragment_tagesuebersicht_label_minutes_untagged)));
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
                        if(label.equals(getString(R.string.fragment_tagesuebersicht_label_minutes_untagged))) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle(getString(R.string.fragment_tagesuebersicht_label_minutes_untagged));
                            Time timeUntagged = new Time(minutesUntagged);
                            TextView textViewUntagged = new TextView(getContext());
                            textViewUntagged.setText(getString(R.string.dialog_taetigkeit_details_untagged_time, timeUntagged.toString()));
                            textViewUntagged.setTextSize(18);
                            textViewUntagged.setPadding(80, 32  , 0, 0);
                            builder.setView(textViewUntagged);
                            builder.setPositiveButton("Einteilen", (dialog, which) -> switchToFragmentStundeneinteilung());
                            builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
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

                                        element.setOnClickListener(v -> {
                                            //TODO: DialogFragment
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                                            builder1.setTitle(R.string.dialog_edit_einteilung_title);

                                            View viewInflated1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_einteilung, (ViewGroup) getView(), false);
                                            final SeekBar seekBar =  viewInflated1.findViewById(R.id.dialog_edit_einteilung_seekBar);
                                            final TextView textView = viewInflated1.findViewById(R.id.dialog_edit_einteilung_textView);

                                            seekBar.setMax(einteilung.minuten + minutesUntagged);
                                            seekBar.setProgress(einteilung.minuten);
                                            Time time = new Time(einteilung.minuten);
                                            textView.setText(time.toString());
                                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                                @Override
                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                    Time time = new Time(progress);
                                                    textView.setText(time.toString());
                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar) {

                                                }
                                            });

                                            builder1.setView(viewInflated1);

                                            builder1.setPositiveButton("Speichern", (dialog, which) -> {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("arbeitstage/" + user.getUid() + "/" + ref.getKey() + "/einteilung/" + d.getKey() + "/minuten", seekBar.getProgress());

                                                DatabaseReference refProjekt = database.getReference("/projekte/" + einteilung.projekt + "/mitarbeiter/" + user.getUid());
                                                refProjekt.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                        int oldTime = dataSnapshot1.child("zeit").getValue(Integer.class);
                                                        updates.put("projekte/" + einteilung.projekt + "/mitarbeiter/" + user.getUid() + "/zeit", oldTime + seekBar.getProgress() - einteilung.minuten);

                                                        database.getReference().updateChildren(updates);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            });

                                            builder1.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

                                            builder1.setNeutralButton("Löschen", ((dialog, which) -> {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("arbeitstage/" + user.getUid() + "/" + ref.getKey() + "/einteilung/" + d.getKey(), null);

                                                DatabaseReference refProjekt = database.getReference("/projekte/" + einteilung.projekt + "/mitarbeiter/" + user.getUid());
                                                refProjekt.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                        int oldTime = dataSnapshot1.child("zeit").getValue(Integer.class);
                                                        updates.put("projekte/" + einteilung.projekt + "/mitarbeiter/" + user.getUid() + "/zeit", oldTime - einteilung.minuten);

                                                        database.getReference().updateChildren(updates);
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }));

                                            builder1.show();
                                        });
                                    }
                                    builder.setView(viewInflated);

                                    builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
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
                    fabStundeneinteilung.setOnClickListener(view1 -> switchToFragmentStundeneinteilung());
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
        Fragment fragment = FragmentStundeneinteilung.newInstance(minutesUntagged, ref.toString());
        fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private int getNextColor () {
        int color = ContextCompat.getColor(getContext(),R.color.colorPrimaryDark);
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
}
