package com.othregensburg.ourglass.entity;

import java.util.Locale;

public class Time {
    private int hour=0;
    private int minutes=0;

    public void add(Stamp stamp) {
        String[] start = stamp.startzeit.split(":");
        String[] end = stamp.endzeit.split(":");
        hour += Integer.parseInt(end[0]) - Integer.parseInt(start[0]);
        minutes += Integer.parseInt(end[1]) - Integer.parseInt(start[1]);
        if (minutes < 0) {
            minutes += 60;
            hour--;
        } else if (minutes > 60) {
            minutes -= 60;
            hour++;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.GERMAN,"%d:%02d",hour,minutes);
    }
}
