package com.othregensburg.ourglass;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.othregensburg.ourglass.entity.Stamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Startseite extends AppDrawerBase {

    private boolean timeIsRunning=false;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startseite);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_homescreen);

       //TimeRunning


        DatabaseReference ref = database.getReference("user/"+user.getUid()+"/timeRunning");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timeIsRunning = dataSnapshot.getValue(Boolean.class);
                if (timeIsRunning) {
                    ImageView img = (ImageView) findViewById(R.id.start);
                    img.setImageResource(R.drawable.hourglass_animation);
                    AnimationDrawable ad= (AnimationDrawable) img.getDrawable();
                    ad.start();
                }else {
                    ImageView img = (ImageView) findViewById(R.id.start);
                    img.setImageResource(R.drawable.hourglass_full);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        //TimeText
        TextView date = findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("E dd.MM HH:mm", Locale.GERMANY);
        date.setText(df.format(calendar.getTime()));


        //TimeStartButton
        ImageView start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //v.setBackgroundColor(getResources().getColor(R.color.startButton));

                timeIsRunning=!timeIsRunning;
                DatabaseReference ref = database.getReference("user/"+user.getUid()+"/timeRunning");
                ref.setValue(timeIsRunning);
                if (timeIsRunning) {
                    ImageView img = (ImageView) v;
                    img.setImageResource(R.drawable.hourglass_animation);
                    AnimationDrawable ad= (AnimationDrawable) img.getDrawable();
                    ad.start();
                    DatabaseReference reference= database.getReference(String.format(Locale.GERMAN,"arbeitstage/%s/%02d%02d%02d",
                            user.getUid(),calendar.get(Calendar.YEAR)-2000,calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)))
                            .child("timestamps").push();
                    DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                    reference.setValue(new Stamp(df.format(calendar.getTime()), null));
                } else {
                    ImageView img = (ImageView) v;
                    img.setImageResource(R.drawable.hourglass_full);
                    Query query=database.getReference(String.format(Locale.GERMAN,"arbeitstage/%s/%02d%02d%02d",
                            user.getUid(),calendar.get(Calendar.YEAR)-2000,calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)))
                            .child("timestamps").orderByKey().limitToLast(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                                d.getRef().child("endzeit").setValue(df.format(calendar.getTime()));
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

    }

}
