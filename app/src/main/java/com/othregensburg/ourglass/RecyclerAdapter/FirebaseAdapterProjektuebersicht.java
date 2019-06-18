package com.othregensburg.ourglass.RecyclerAdapter;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.entity.Projektmitglied;
import com.othregensburg.ourglass.entity.Stamp;

public class FirebaseAdapterProjektuebersicht extends FirebaseRecyclerAdapter<Projektmitglied, FirebaseAdapterProjektuebersicht.ViewHolder> {

    public FirebaseAdapterProjektuebersicht(@NonNull FirebaseRecyclerOptions<Projektmitglied> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAdapterProjektuebersicht.ViewHolder holder, int position, @NonNull Projektmitglied model) {
        holder.name.setText(model.name);
        holder.zeit.setText(""+model.zeit);
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

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_field);
            zeit = itemView.findViewById(R.id.hour_field);

        }
    }
}
