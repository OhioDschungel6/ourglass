package com.othregensburg.ourglass;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.othregensburg.ourglass.RecyclerAdapter.FirebaseAdapterStundenuebersicht;

public class TagesuebersichtActivity extends AppDrawerBase implements FragmentTagesuebersicht.OnFragmentInteractionListener, FragmentStundeneinteilung.OnFragmentInteractionListener{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagesuebersicht);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_stundenuebersicht);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        String refUrl = extras.getString("DatabaseRef");
        int minutesWorekd =extras.getInt("minutesWorked");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = FragmentTagesuebersicht.newInstance(refUrl, minutesWorekd);
        fragmentTransaction.replace(R.id.stundenuebersicht_fragmentcontainer, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            //TODO: super.onBackPressed() würde reichen, aber Stundenübersicht lädt dann keine Daten -> entsprechende lifecycle methode fehlt wahrscheinlich
            Intent intent = new Intent(this, Stundenuebersicht.class);
            startActivity(intent);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
