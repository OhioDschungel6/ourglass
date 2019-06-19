package com.othregensburg.ourglass.RecyclerAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.entity.Projektmitglied;
import com.othregensburg.ourglass.entity.Stamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class FirebaseAdapterProjektuebersicht extends FirebaseRecyclerAdapter<Projektmitglied, FirebaseAdapterProjektuebersicht.ViewHolder> {


    private int nextColor=0;
    public FirebaseAdapterProjektuebersicht(@NonNull FirebaseRecyclerOptions<Projektmitglied> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAdapterProjektuebersicht.ViewHolder holder, int position, @NonNull Projektmitglied model) {
        holder.name.setText(model.name);
        holder.zeit.setText(String.format(Locale.GERMAN, "Zeit: %d Std %02d Min", model.zeit / 60, model.zeit % 60));
    }

    @NonNull
    @Override
    public FirebaseAdapterProjektuebersicht.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        View mItemView = mInflater.inflate(R.layout.projekt_entry, viewGroup, false);
        return new FirebaseAdapterProjektuebersicht.ViewHolder(mItemView);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView zeit;
        private static final int PIE_CHART_TEXTSIZE = 14;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_field);
            zeit = itemView.findViewById(R.id.hour_field);

            itemView.setOnClickListener(v -> {
                AlertDialog.Builder dialog = new AlertDialog.Builder(itemView.getContext());
                PieChartView pie = new PieChartView(itemView.getContext());
                List<SliceValue> sliceValuesList = new ArrayList<>();

                dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                getRef(getAdapterPosition()).child("taetigkeiten").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        PieChartData pieChartData = new PieChartData(sliceValuesList);
                        pieChartData.setHasLabels(true).setValueLabelTextSize(PIE_CHART_TEXTSIZE);
                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                            sliceValuesList.add(new SliceValue(child.getValue(Integer.class)).setLabel(child.getKey()).setColor(getNextColor()));
                        }
                        pie.setPieChartData(pieChartData);
                        dialog.setView(pie);
                        dialog.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            });

        }
    }
    private int getNextColor () {
        int color = Color.RED;;
        switch (nextColor%4) {
            case 0: color = Color.RED;
                break;
            case 1: color = Color.YELLOW;
                break;
            case 2: color = Color.GREEN;
                break;
            case 3: color = Color.BLUE;
        }
        nextColor++;
        return color;
    }
}
