package com.othregensburg.ourglass;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.othregensburg.ourglass.entity.User;

public class FirstLogin extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.app_bar_firstlogin);

        EditText name = findViewById(R.id.secondname);
        EditText firstname = findViewById(R.id.firstname);
        //TODO durch seekbar ersetzen
        EditText weeklyWorkHours = findViewById(R.id.weeklyworktime);
        FloatingActionButton confirm = findViewById(R.id.confirmData);
        confirm.setOnClickListener(e->{
            String sName=name.getText().toString();
            String sFirstname=firstname.getText().toString();
            String sWorkhours= weeklyWorkHours.getText().toString();
            if (!sName.isEmpty() && !sFirstname.isEmpty() && !sWorkhours.isEmpty()) {
                FirebaseDatabase.getInstance().getReference("user/"+ FirebaseAuth.getInstance().getUid()).setValue(new User(sName, sFirstname, Double.parseDouble(sWorkhours) / 5.0), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Intent intent = new Intent(getBaseContext(), Startseite.class);
                        startActivity(intent);
                        finish();

                    }
                });
            }

        });
    }
}
