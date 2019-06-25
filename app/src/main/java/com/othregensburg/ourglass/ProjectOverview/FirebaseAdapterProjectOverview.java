package com.othregensburg.ourglass.ProjectOverview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.Entity.ProjectMember;

import java.util.Locale;

public class FirebaseAdapterProjectOverview extends FirebaseRecyclerAdapter<ProjectMember, FirebaseAdapterProjectOverview.ViewHolder> {

    public FirebaseAdapterProjectOverview(@NonNull FirebaseRecyclerOptions<ProjectMember> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FirebaseAdapterProjectOverview.ViewHolder holder, int position, @NonNull ProjectMember model) {
        holder.name.setText(model.name);
        holder.zeit.setText(String.format(Locale.GERMAN, "Zeit: %d Std %02d Min", model.zeit / 60, model.zeit % 60));
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

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_field);
            zeit = itemView.findViewById(R.id.hour_field);

        }
    }
}
