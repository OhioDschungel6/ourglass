package com.othregensburg.ourglass;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentStundeneinteilung.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentStundeneinteilung#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentStundeneinteilung extends Fragment {
    private static final String ARG_MINUTES_UNTAGGED = "minutesUntagged";

    private int minutesUntagged;

    private OnFragmentInteractionListener mListener;

    public FragmentStundeneinteilung() {
        // Required empty public constructor
    }

    public static FragmentStundeneinteilung newInstance(int minutesUntagged) {
        FragmentStundeneinteilung fragment = new FragmentStundeneinteilung();
        Bundle args = new Bundle();
        args.putInt(ARG_MINUTES_UNTAGGED, minutesUntagged);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            minutesUntagged = getArguments().getInt(ARG_MINUTES_UNTAGGED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stundeneinteilung, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SeekBar seekBarTime = getView().findViewById(R.id.seekBar_time);
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
