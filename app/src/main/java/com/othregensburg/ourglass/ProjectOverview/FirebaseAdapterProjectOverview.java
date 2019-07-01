package com.othregensburg.ourglass.ProjectOverview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.Entity.ProjectMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class FirebaseAdapterProjectOverview extends FirebaseRecyclerAdapter<ProjectMember, FirebaseAdapterProjectOverview.ViewHolder> {


    private int nextColor = 0;
    private Context context;

    public FirebaseAdapterProjectOverview(@NonNull FirebaseRecyclerOptions<ProjectMember> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAdapterProjectOverview.ViewHolder holder, int position, @NonNull ProjectMember model) {
        holder.name.setText(model.name);
        holder.zeit.setText(String.format(Locale.GERMAN, "Zeit: %d Std %d Min", model.zeit / 60, model.zeit % 60));
        holder.min = model.zeit;
    }

    @NonNull
    @Override
    public FirebaseAdapterProjectOverview.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        View mItemView = mInflater.inflate(R.layout.projekt_entry, viewGroup, false);
        return new FirebaseAdapterProjectOverview.ViewHolder(mItemView);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView zeit;
        private static final int PIE_CHART_TEXTSIZE = 14;
        int min;

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
                        pieChartData.setHasCenterCircle(true).setCenterText1(new Time(min).toString());
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

        private int getNextColor() {
            int color = ContextCompat.getColor(context, R.color.colorPrimaryDark);
            switch (nextColor % 3) {
                case 0:
                    color = ContextCompat.getColor(context, R.color.colorPrimaryDark);
                    break;
                case 1:
                    color = ContextCompat.getColor(context, R.color.colorAccent);
                    break;
                case 2:
                    color = ContextCompat.getColor(context, R.color.pieChart_thirdColor);
                    break;
            }
            nextColor++;
            return color;
        }
    }
}
