package com.othregensburg.ourglass;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
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
import java.util.List;
import java.util.Locale;

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
                int n = 0;
                List<SliceValue> pieData = new ArrayList<>();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Projekteinteilung einteilung = d.getValue(Projekteinteilung.class);
                    //TODO: !!!grausam!!!, wie gehts besser?
                    boolean alreadyExists = false;
                    for (SliceValue pd : pieData) {
                        String pdLabel = String.copyValueOf(pd.getLabelAsChars());
                        if(pdLabel.equals(einteilung.taetigkeit)){
                            float minutes = pd.getValue();
                            pd.setValue(minutes + einteilung.minuten);
                            alreadyExists = true;
                        }
                    }
                    if(!alreadyExists){
                        pieData.add(new SliceValue(einteilung.minuten, getNextColor(n)).setLabel(einteilung.taetigkeit));
                        n++;
                    }
                    minutesUntagged -= einteilung.minuten;
                }
                if (minutesUntagged > 0) {
                    pieData.add(new SliceValue(minutesUntagged, Color.LTGRAY).setLabel(LABEL_MINUTES_UNTAGGED));
                }
                PieChartData pieChartData = new PieChartData(pieData);
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
                            //TODO:uneingeteilte Zeit anzeigen, button für neue einteilung einfügen
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
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
                                    //TODO: builder.setCustomTitle()

                                    View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taetigkeit_details, (ViewGroup) getView(), false);
                                    LinearLayout einteilungenList = viewInflated.findViewById(R.id.einteilungen_list);

                                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                                        Projekteinteilung einteilung = d.getValue(Projekteinteilung.class);
                                        //attach to root?
                                        View element = LayoutInflater.from(getContext()).inflate(R.layout.dialog_taetigkeit_details_entry, einteilungenList, false);
                                        ((TextView) element.findViewById(R.id.textView_projekt)).setText(einteilung.projekt);
                                        ((TextView) element.findViewById(R.id.textView_notiz)).setText(einteilung.notiz);
                                        ((TextView) element.findViewById(R.id.textView_time)).setText(Integer.toString(einteilung.minuten));
                                        einteilungenList.addView(element);
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

    private int getNextColor (int n) {
        switch (n%4) {
            //TODO: R.color richtig oder color.parse?
            case 0: return ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
            case 1: return ContextCompat.getColor(getContext(),R.color.colorAccent);
            case 2: return ContextCompat.getColor(getContext(),R.color.colorPrimaryDark);
            case 3: return ContextCompat.getColor(getContext(),R.color.pieChart_thirdColor);
            default: return ContextCompat.getColor(getContext(),R.color.colorPrimaryDark);
        }
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
