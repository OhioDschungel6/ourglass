package com.othregensburg.ourglass.Entity;

import java.util.Locale;

public class Time {
    private int hours;
    private int minutes;

    public Time () {
        hours = 0;
        minutes = 0;
    }

    public Time(int givenMinutes) {
        hours = givenMinutes / 60;
        minutes = givenMinutes % 60;
    }

    public void add(Stamp stamp) {
        if (stamp.end != null && !stamp.end.equals("")) {
            String[] start = stamp.start.split(":");
            String[] end = stamp.end.split(":");
            hours += Integer.parseInt(end[0]) - Integer.parseInt(start[0]);
            minutes += Integer.parseInt(end[1]) - Integer.parseInt(start[1]);
            if (minutes < 0) {
                minutes += 60;
                hours--;
            } else if (minutes > 60) {
                minutes -= 60;
                hours++;
            }
        }

    }

    @Override
    public String toString() {
        return String.format(Locale.GERMAN,"%d:%02d",hours,minutes);
    }

    public int getMinutes () {
        return hours*60 + minutes;
    }
}
