package com.othregensburg.ourglass;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

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
    private static final int PIE_CHART_TEXTSIZE = 14;

    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";

    // TODO: java util Date oder sql date?
    private int day;
    private int month;
    private int year;
    private Date date;

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


    // TODO: Rename and change types and number of parameters
    public static FragmentTagesuebersicht newInstance(int day, int month, int year) {
        FragmentTagesuebersicht fragment = new FragmentTagesuebersicht();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);
        args.putInt(ARG_MONTH, month-1);
        args.putInt(ARG_YEAR, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            day = getArguments().getInt(ARG_DAY);
            month = getArguments().getInt(ARG_MONTH);
            year = getArguments().getInt(ARG_YEAR);
            date = new GregorianCalendar(year, month, day).getTime();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tagesuebersicht, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textViewDate = getView().findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("E dd.MM.yy", Locale.GERMANY);
        textViewDate.setText(df.format(date));

        PieChartView pieChartView = getView().findViewById(R.id.pie_chart);
        //TODO: test data, insert values from database later
        //SliceValue arguments: 1. float value for size, 2. color
        List<SliceValue> pieData = new ArrayList<>();
        pieData.add(new SliceValue(0.3f, Color.BLUE).setLabel("Email"));
        pieData.add(new SliceValue(3, Color.GRAY).setLabel("abc"));
        pieData.add(new SliceValue(2.5f, Color.RED).setLabel("Projekt Xyz"));
        pieData.add(new SliceValue(1.64f, Color.MAGENTA).setLabel("Nicht zugeteilt"));

        PieChartData pieChartData = new PieChartData(pieData);
        pieChartData.setHasLabels(true).setValueLabelTextSize(PIE_CHART_TEXTSIZE);
        //TODO: get timeWorked from database
        String timeWorked = "8:23";
        //Set size and color of font in the middle:
        //pieChartData.setHasCenterCircle(true).setCenterText1("Sales in million").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
        pieChartData.setHasCenterCircle(true).setCenterText1(timeWorked);
        pieChartView.setPieChartData(pieChartData);

        //TODO: Evtl nach Designrichtlinien mit ViewModel (bzw Interactioninterface) implementieren
        FloatingActionButton fabStundeneinteilung = getView().findViewById(R.id.fab_stundeneinteilung);
        fabStundeneinteilung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //TODO: Ordner anim in res und integers.xml in values ist aus Musterlösung zur Fragmentsübung übernommen
                fragmentTransaction.setCustomAnimations(R.anim.alpha_transition_in, R.anim.alpha_transition_out);
                //TODO: Testdaten, später aus Datenbank holen
                Fragment fragment = FragmentStundeneinteilung.newInstance(3.21f);
                //todo wieder einkommentieren
                // fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);
                fragmentTransaction.commit();
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
