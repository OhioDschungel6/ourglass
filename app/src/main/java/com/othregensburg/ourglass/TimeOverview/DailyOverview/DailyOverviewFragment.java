package com.othregensburg.ourglass.TimeOverview.DailyOverview;

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
import com.othregensburg.ourglass.Entity.ProjectClassification;
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;


public class DailyOverviewFragment extends Fragment {
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
    private ValueEventListener selectedEventlistener;

    public DailyOverviewFragment() {
        // Required empty public constructor
    }

    public static DailyOverviewFragment newInstance(String refUrl, int minutesWorked) {
        DailyOverviewFragment fragment = new DailyOverviewFragment();
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
        return inflater.inflate(R.layout.fragment_daily_overview, container, false);
    }

    ValueEventListener eventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            minutesUntagged = minutesWorked;
            Map<String, Integer> mapActivities = new HashMap<>();

            for (DataSnapshot d : dataSnapshot.getChildren()) {
                ProjectClassification classification = d.getValue(ProjectClassification.class);
                int minutesActivity = mapActivities.getOrDefault(classification.activity, 0);
                minutesActivity += classification.minutes;
                mapActivities.put(classification.activity, minutesActivity);
                minutesUntagged -= classification.minutes;
            }
            List<SliceValue> sliceValues = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : mapActivities.entrySet()) {
                sliceValues.add(new SliceValue(entry.getValue(), getNextColor()).setLabel(entry.getKey()));
            }
            if (minutesUntagged > 0) {
                sliceValues.add(new SliceValue(minutesUntagged, Color.LTGRAY).setLabel(getString(R.string.fragment_daily_overview_label_minutes_untagged)));
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
                    if (label.equals(getString(R.string.fragment_daily_overview_label_minutes_untagged))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(getString(R.string.fragment_daily_overview_label_minutes_untagged));
                        Time timeUntagged = new Time(minutesUntagged);
                        TextView textViewUntagged = new TextView(getContext());
                        textViewUntagged.setText(getString(R.string.dialog_activity_details_untagged_time, timeUntagged.toString()));
                        textViewUntagged.setTextSize(18);
                        textViewUntagged.setPadding(80, 32, 0, 0);
                        builder.setView(textViewUntagged);
                        builder.setPositiveButton("Einteilen", (dialog, which) -> switchToFragmentTagTime());
                        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    } else {
                        Query selected = ref.child("classification").orderByChild("activity").equalTo(label);

                        AlertDialog.Builder detailBuilder = new AlertDialog.Builder(getContext());
                        detailBuilder.setTitle(label);
                        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity_details, (ViewGroup) getView(), false);
                        LinearLayout classificationList = viewInflated.findViewById(R.id.classification_list);
                        detailBuilder.setView(viewInflated);
                        detailBuilder.setPositiveButton("Ok", (dialog, which) -> {

                            dialog.dismiss();

                        });
                        detailBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                selected.removeEventListener(selectedEventlistener);
                            }
                        });
                        AlertDialog detailDialog = detailBuilder.show();

                        selectedEventlistener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    detailDialog.dismiss();
                                }

                                classificationList.removeAllViews();
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    ProjectClassification classification = d.getValue(ProjectClassification.class);
                                    View element = LayoutInflater.from(getContext()).inflate(R.layout.dialog_activity_details_entry, classificationList, false);
                                    ((TextView) element.findViewById(R.id.textView_project)).setText(classification.project);
                                    ((TextView) element.findViewById(R.id.textView_note)).setText(classification.note);
                                    String stringTime = new Time(classification.minutes).toString();
                                    TextView textViewTime = element.findViewById(R.id.dialog_activity_textView_time);
                                    textViewTime.setText(stringTime);
                                    classificationList.addView(element);

                                    element.setOnClickListener(v -> {
                                        AlertDialog.Builder editBuilder = new AlertDialog.Builder(getContext());
                                        editBuilder.setTitle(R.string.dialog_edit_classification_title);

                                        View viewInflated1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_classification, (ViewGroup) getView(), false);
                                        final SeekBar seekBar = viewInflated1.findViewById(R.id.dialog_edit_classification_seekBar);
                                        final TextView textView = viewInflated1.findViewById(R.id.dialog_edit_classification_textView);

                                        seekBar.setMax(classification.minutes + minutesUntagged);
                                        seekBar.setProgress(classification.minutes);
                                        Time time = new Time(classification.minutes);
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

                                        editBuilder.setView(viewInflated1);

                                        String path = "projects/" + classification.project + "/employee/" + user.getUid() + "/";
                                        editBuilder.setPositiveButton("Speichern", (dialog, which) -> {
                                            Map<String, Object> updates = new HashMap<>();

                                            int newTime = seekBar.getProgress();
                                            if (newTime > 0) {
                                                updates.put("workdays/" + user.getUid() + "/" + ref.getKey() + "/classification/" + d.getKey() + "/minutes", newTime);
                                            } else {
                                                updates.put("workdays/" + user.getUid() + "/" + ref.getKey() + "/classification/" + d.getKey(), null);
                                            }

                                            DatabaseReference refProject = database.getReference("/projects/" + classification.project + "/employee/" + user.getUid());
                                            refProject.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot ds) {
                                                    int oldTimeProject = ds.child("time").getValue(Integer.class);
                                                    int newTimeProject = oldTimeProject + seekBar.getProgress() - classification.minutes;
                                                    if (newTimeProject > 0) {
                                                        updates.put(path + "time", newTimeProject);

                                                        int oldTimeActivity = ds.child("activities/" + classification.activity).getValue(Integer.class);
                                                        int newTimeActivity = oldTimeActivity + seekBar.getProgress() - classification.minutes;
                                                        if (newTimeActivity > 0) {
                                                            updates.put(path + "activities/" + classification.activity, newTimeActivity);
                                                        } else {
                                                            updates.put(path + "activities/" + classification.activity, null);
                                                        }
                                                    } else {
                                                        updates.put(path, null);
                                                    }

                                                    database.getReference().updateChildren(updates);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        });

                                        editBuilder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());

                                        editBuilder.setNeutralButton("Löschen", ((dialog, which) -> {
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("workdays/" + user.getUid() + "/" + ref.getKey() + "/classification/" + d.getKey(), null);

                                            DatabaseReference refProject = database.getReference("/projects/" + classification.project + "/employee/" + user.getUid());
                                            refProject.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot ds) {
                                                    int oldTimeProject = ds.child("time").getValue(Integer.class);
                                                    int newTimeProject = oldTimeProject - classification.minutes;
                                                    if (newTimeProject > 0) {
                                                        updates.put(path + "time", newTimeProject);

                                                        int oldTimeActivity = ds.child("activities/" + classification.activity).getValue(Integer.class);
                                                        int newTimeActivity = oldTimeActivity - classification.minutes;
                                                        if (newTimeActivity > 0) {
                                                            updates.put(path + "activities/" + classification.activity, newTimeActivity);
                                                        } else {
                                                            updates.put(path + "activities/" + classification.activity, null);
                                                        }
                                                    } else {
                                                        updates.put(path, null);
                                                    }

                                                    database.getReference().updateChildren(updates);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }));

                                        editBuilder.show();
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };
                        selected.addValueEventListener(selectedEventlistener);
                    }
                }

                @Override
                public void onValueDeselected() {

                }
            });

            FloatingActionButton fabTagTime = getView().findViewById(R.id.fab_tag_time);
            if (minutesUntagged == 0) {
                fabTagTime.setEnabled(false);
                fabTagTime.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                fabTagTime.setAlpha(0.4f);
            } else {
                fabTagTime.setEnabled(true);
                fabTagTime.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorAccent)));
                fabTagTime.setAlpha(1f);
                fabTagTime.setOnClickListener(view1 -> switchToFragmentTagTime());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewDate = getView().findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("EEEE dd.MM.yy", Locale.GERMANY);
        textViewDate.setText(df.format(date.getTime()));

        DatabaseReference refEinteilungen = ref.child("/classification");

        refEinteilungen.addValueEventListener(eventListener);
    }

    @Override
    public void onDestroyView() {
        ref.child("/classification").removeEventListener(eventListener);
        super.onDestroyView();
    }

    private void switchToFragmentTagTime() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = TagTimeFragment.newInstance(minutesUntagged, ref.toString());
        fragmentTransaction.replace(R.id.time_overview_fragmentcontainer, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private int getNextColor() {
        int color = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        switch (nextColor % 4) {
            case 0:
                color = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
                break;
            case 1:
                color = ContextCompat.getColor(getContext(), R.color.colorAccent);
                break;
            case 2:
                color = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
                break;
            case 3:
                color = ContextCompat.getColor(getContext(), R.color.pieChart_thirdColor);
        }
        nextColor++;
        return color;
    }
}
