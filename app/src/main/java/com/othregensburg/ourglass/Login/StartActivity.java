package com.othregensburg.ourglass.Login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.othregensburg.ourglass.R;
import com.othregensburg.ourglass.Homescreen;

import java.util.Arrays;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private static final List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_start);
        ImageView img = (ImageView) findViewById(R.id.loading);
        AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
        ad.start();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.hourglass_full)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                IdpResponse resp= (IdpResponse) data.getExtras().get("extra_idp_response");

                if (resp!=null && resp.isNewUser()) {
                    Intent intent = new Intent(this, FirstLoginActivity.class);
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
                        intent.putExtra("nfc", true);
                    } else {
                        intent.putExtra("nfc", false);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(this, Homescreen.class);
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
                        intent.putExtra("nfc", true);
                    } else {
                        intent.putExtra("nfc", false);
                    }
                    startActivity(intent);
                    finish();
                }

            } else if (resultCode == RESULT_CANCELED) {
                ImageView img = findViewById(R.id.loading);
                img.setImageResource(R.drawable.hourglass_full);
                Button signInButton = findViewById(R.id.button_sign_in);
                signInButton.setVisibility(View.VISIBLE);

                Snackbar.make(findViewById(R.id.start_activity_constraintLayout), "Login wurde abgebrochen", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                signInButton.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signInButton.setVisibility(View.GONE);
                        ImageView img = findViewById(R.id.loading);
                        img.setImageResource(R.drawable.hourglass_animation);
                        AnimationDrawable ad= (AnimationDrawable) img.getDrawable();
                        ad.start();

                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setAvailableProviders(providers)
                                        .setLogo(R.drawable.hourglass_full)
                                        .build(),
                                RC_SIGN_IN);
                    }
                });
            }
        }
    }
}
