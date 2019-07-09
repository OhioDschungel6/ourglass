package com.othregensburg.ourglass.Entity;

import android.util.Pair;

public class Stamp {
    public String start;
    public String end;


    public Stamp() {
    }

    public Stamp(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public Pair<Integer, Integer> pairStarttime() {
        String[] s = start.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
    }

    public Pair<Integer, Integer> pairEndtime() {
        String[] s = end.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
    }


}
