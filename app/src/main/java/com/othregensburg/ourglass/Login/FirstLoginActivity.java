package com.othregensburg.ourglass.Login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.othregensburg.ourglass.Entity.User;
import com.othregensburg.ourglass.Homescreen;
import com.othregensburg.ourglass.R;

public class FirstLoginActivity extends AppCompatActivity {
    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_firstlogin);

        SeekBar seekBarWeeklyWorkHours = findViewById(R.id.seekBar_weeklyWorkTime);
        TextView textViewWeeklyWorkHours = findViewById(R.id.textView_weeklyWorkTime);
        FloatingActionButton fab_confirm = findViewById(R.id.confirmData);

        seekBarWeeklyWorkHours.setMax(100);
        seekBarWeeklyWorkHours.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewWeeklyWorkHours.setText(Double.toString(progress / 2.0));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        fab_confirm.setOnClickListener(e->{
            Double workhours = seekBarWeeklyWorkHours.getProgress() / 2.0;

            if (workhours == 0.0) {
                Snackbar.make(findViewById(R.id.constraintLogin), R.string.first_login_activity_worktime_snackbar, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                findViewById(R.id.textView_weeklyWorkTimeLabel).setVisibility(View.GONE);
                textViewWeeklyWorkHours.setVisibility(View.GONE);
                seekBarWeeklyWorkHours.setVisibility(View.GONE);
                fab_confirm.setVisibility(View.GONE);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getUid()).setValue(new User(workhours / 5.0), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Intent intent = new Intent(getBaseContext(), Homescreen.class);
                        intent.putExtra("nfc",getIntent().getBooleanExtra("nfc",false) );
                        startActivity(intent);
                        finish();
                    }
                });
            }

        });
    }
}
