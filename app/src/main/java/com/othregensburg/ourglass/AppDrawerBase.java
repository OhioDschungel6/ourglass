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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AppDrawerBase extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //NFC
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (getClass() != Startseite.class && nfcAdapter!=null) {

            nfcAdapter.enableReaderMode(this, tag -> {
                Intent intent = new Intent(this, Startseite.class);
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
        } else if (getClass() != Startseite.class && getClass() != TagesuebersichtActivity.class){
            Intent intent = new Intent(this, Startseite.class);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_homescreen && getClass()!= Startseite.class) {
            Intent intent = new Intent(this, Startseite.class);
            startActivity(intent);
        } else if (id == R.id.nav_stundenuebersicht && getClass()!= Stundenuebersicht.class && getClass() != TagesuebersichtActivity.class) {
            Intent intent = new Intent(this, Stundenuebersicht.class);
            startActivity(intent);
        } else if (id == R.id.nav_korrektur && getClass()!= Stundenkorrektur.class) {
            Intent intent = new Intent(this, Stundenkorrektur.class);
            startActivity(intent);
        } else if (id == R.id.nav_projekt && getClass()!= Projektuebersicht.class) {
            Intent intent = new Intent(this, Projektuebersicht.class);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> getBackToStartActivity());
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void getBackToStartActivity () {
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }

}
