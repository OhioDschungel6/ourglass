package com.othregensburg.ourglass;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.othregensburg.ourglass.Entity.Stamp;
import com.othregensburg.ourglass.Entity.Time;
import com.othregensburg.ourglass.Entity.Workday;
import com.othregensburg.ourglass.Login.FirstLoginActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Homescreen extends AppDrawerBase implements View.OnClickListener{

    private boolean timeIsRunning = false;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Calendar calendar = Calendar.getInstance();
    private double dailyworktime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //NFC
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, tag -> {
                onClick(findViewById(R.id.start));
            } ,NfcAdapter.FLAG_READER_NFC_F |NfcAdapter.FLAG_READER_NFC_B| NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_V,null);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_homescreen);

        //TimeRunning
        initTimeIsRunning();


        //TimeText
        TextView date = findViewById(R.id.date);
        DateFormat df = new SimpleDateFormat("E dd.MM HH:mm", Locale.GERMANY);
        date.setText(df.format(calendar.getTime()));


        loadUI();
        addRefreshTimer();


    }

    private void initTimeIsRunning() {
        DatabaseReference ref = database.getReference("user/" + user.getUid() + "/timeRunning");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Intent intent = new Intent(getBaseContext(), FirstLoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                timeIsRunning = dataSnapshot.getValue(Boolean.class);
                if (getIntent().getBooleanExtra("nfc",false)) {
                    //if Started via NFC
                    onClick(findViewById(R.id.start));
                }
                if (timeIsRunning) {
                    ImageView img = findViewById(R.id.start);
                    img.setImageResource(R.drawable.hourglass_animation);
                    AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
                    ad.start();
                } else {
                    ImageView img = findViewById(R.id.start);
                    img.setImageResource(R.drawable.hourglass_full);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","The read failed: " + databaseError.getCode());
            }
        });
    }

    private void addRefreshTimer() {
        Timer timer = new Timer(true);
        Date now= Calendar.getInstance().getTime();
        now.setSeconds(0);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calendar = Calendar.getInstance();
                //TimeText
                TextView date = findViewById(R.id.date);
                DateFormat df = new SimpleDateFormat("E dd.MM HH:mm", Locale.GERMANY);
                date.setText(df.format(calendar.getTime()));

                //TodaysWorktime
                updateTodaysWorktime();

                //MonthlyIsWorktime
                updateMonthlyIsWorktime();

            }
        },now,60000);
    }

    private void updateTodaysWorktime() {
        //Todays worktime
        TextView todayWorktime = findViewById(R.id.worktime);
        DatabaseReference worktime = database.getReference(String.format(Locale.GERMAN, "workdays/%s/%02d%02d%02d/timestamps", user.getUid(), calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
        worktime.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Time t = new Time();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        Stamp s = d.getValue(Stamp.class);
                        if (s.end == null) {
                            DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                            calendar= Calendar.getInstance();
                            s.end = df.format(calendar.getTime());
                        }
                        t.add(s);
                    }
                }
                todayWorktime.setText(t.toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","The read failed: " + databaseError.getCode());
            }
        });
    }

    private void loadUI() {
        //Todays worktime
        updateTodaysWorktime();


        //TimeStartButton
        ImageView start = findViewById(R.id.start);
        start.setOnClickListener(this);

        //Sollstd:
        TextView sollStd = findViewById(R.id.textView_sollStdAnz);
        DatabaseReference sollStdRef = database.getReference(String.format(Locale.GERMAN, "user/%s/Sollstd", user.getUid()));
        sollStdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double anz = dataSnapshot.getValue(Double.class);
                if (anz != null) {
                    dailyworktime = anz;
                    int workdays = getWorkdays((Calendar) calendar.clone());

                    Date date = new Date(user.getMetadata().getCreationTimestamp());
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.set(Calendar.DAY_OF_MONTH, 1);
                    if (calendar2.getTime().compareTo(date) < 0) {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.setTime(date);
                        workdays = workdays - getWorkdays(calendar1);
                        if (date.getDay() != 6 && date.getDay() != 0) {
                            workdays++;
                        }
                    }
                    double d = anz * workdays;
                    sollStd.setText(String.format(Locale.GERMAN, "%d:%02d", (int) d, (int) ((d - (int) d) * 60)));
                }
                //months Worktime
                updateMonthlyIsWorktime();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","The read failed: " + databaseError.getCode());
            }
        });

    }

    public void updateMonthlyIsWorktime() {
        //months isWorktime
        TextView monthTime = findViewById(R.id.textView_istStdAnz);
        Query monthTimeQuery = database.getReference("workdays/" + user.getUid())
                .orderByKey()
                .startAt(String.format(Locale.GERMAN, "%02d%02d%02d", calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1, 1));
        monthTimeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Time t = new Time();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Workday workday = ds.getValue(Workday.class);
                    if (workday.ill || workday.holiday) {
                        t.add(new Stamp("00:00", String.format(Locale.GERMAN, "%02d:%02d", (int) dailyworktime, (int) ((dailyworktime - ((int) dailyworktime)) * 60))));
                    } else {
                        if (workday.timestamps != null) {
                            for (Stamp stamp : workday.timestamps.values()) {
                                if (stamp.end == null) {
                                    DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                                    calendar = Calendar.getInstance();
                                    stamp.end = df.format(calendar.getTime());
                                }
                                t.add(stamp);
                            }
                        }
                    }
                }
                monthTime.setText(t.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","The read failed: " + databaseError.getCode());
            }
        });
    }

    public int getWorkdays(Calendar c) {
        Calendar cal = c;
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int oldDay = day;
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int weekDayOfFirst = cal.get(Calendar.DAY_OF_WEEK);

        if (weekDayOfFirst == 1) {
            weekDayOfFirst += 7;
        }

        weekDayOfFirst--;

        day += weekDayOfFirst - 1;
        int weekenddays = (day / 7) * 2;
        if (weekday == 7) {
            weekenddays++;
        }

        return oldDay - weekenddays;
    }


    @Override
    public void onClick(View v) {
        vibrate();
        timeIsRunning = !timeIsRunning;
        DatabaseReference ref = database.getReference("user/" + user.getUid() + "/timeRunning");
        ref.setValue(timeIsRunning);
        if (timeIsRunning) {
            ImageView img = (ImageView) v;
            img.setImageResource(R.drawable.hourglass_animation);
            AnimationDrawable ad = (AnimationDrawable) img.getDrawable();
            ad.start();
            calendar = Calendar.getInstance();
            DatabaseReference reference = database.getReference(String.format(Locale.GERMAN, "workdays/%s/%02d%02d%02d",
                    user.getUid(), calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
                    .child("timestamps").push();
            DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
            reference.setValue(new Stamp(df.format(calendar.getTime()), null));
        } else {
            ImageView img = (ImageView) v;
            img.setImageResource(R.drawable.hourglass_full);
            calendar = Calendar.getInstance();
            Query query = database.getReference(String.format(Locale.GERMAN, "workdays/%s/%02d%02d%02d",
                    user.getUid(), calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
                    .child("timestamps").orderByKey().limitToLast(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        DateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                        d.getRef().child("end").setValue(df.format(calendar.getTime()));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","The read failed: " + databaseError.getCode());
            }
            });
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(70);
        }
    }
}
