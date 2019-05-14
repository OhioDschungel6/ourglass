package com.othregensburg.ourglass;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Startseite extends AppDrawerBase {

    private boolean timeIsRunning=false;

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

        //TimeText

        TextView date= findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("E dd.MM HH:mm", Locale.GERMANY);
        date.setText(df.format(Calendar.getInstance().getTime()));


        //TimeStartButton
        ImageView start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //v.setBackgroundColor(getResources().getColor(R.color.startButton));

                timeIsRunning=!timeIsRunning;
                if (timeIsRunning) {
                    ImageView img = (ImageView) v;
                    img.setImageResource(R.drawable.hourglass_animation);
                    AnimationDrawable ad= (AnimationDrawable) img.getDrawable();
                    ad.start();
                } else {
                    ImageView img = (ImageView) v;
                    img.setImageResource(R.drawable.hourglass_full);
                }

            }
        });

    }

}
