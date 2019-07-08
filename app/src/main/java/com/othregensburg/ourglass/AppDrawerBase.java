package com.othregensburg.ourglass;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.othregensburg.ourglass.Correction.CorrectionActivity;
import com.othregensburg.ourglass.Login.StartActivity;
import com.othregensburg.ourglass.ProjectOverview.ProjectOverviewActivity;
import com.othregensburg.ourglass.TimeOverview.DailyOverview.DailyOverviewActivity;
import com.othregensburg.ourglass.TimeOverview.TimeOverviewActivity;

public class AppDrawerBase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //NFC
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (getClass() != Homescreen.class && nfcAdapter!=null) {

            nfcAdapter.enableReaderMode(this, tag -> {
                Intent intent = new Intent(this, Homescreen.class);
                intent.putExtra("nfc", true);
                startActivity(intent);
            } ,NfcAdapter.FLAG_READER_NFC_F |NfcAdapter.FLAG_READER_NFC_B| NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_V,null);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getClass() != Homescreen.class && getClass() != DailyOverviewActivity.class){
            Intent intent = new Intent(this, Homescreen.class);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_homescreen && getClass()!= Homescreen.class) {
            Intent intent = new Intent(this, Homescreen.class);
            startActivity(intent);
        } else if (id == R.id.nav_time_overview && getClass() != TimeOverviewActivity.class && getClass() != DailyOverviewActivity.class) {
            Intent intent = new Intent(this, TimeOverviewActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_korrektur && getClass()!= CorrectionActivity.class) {
            Intent intent = new Intent(this, CorrectionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_project && getClass() != ProjectOverviewActivity.class) {
            Intent intent = new Intent(this, ProjectOverviewActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> getBackToStartActivity());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getBackToStartActivity () {
        Intent intent = new Intent(this, StartActivity.class);
        intent.putExtra("persistent", true);
        startActivity(intent);
        finish();
    }

}
