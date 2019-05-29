package com.othregensburg.ourglass.entity;

import android.util.Pair;

public class Stamp {
    //TODO not public
    public String startzeit;
    public String endzeit;


    public Stamp() {
    }

    public Stamp(String startzeit, String endzeit) {
        this.startzeit = startzeit;
        this.endzeit = endzeit;
    }



    public Pair<Integer,Integer> pairStartzeit() {
        String[] s = startzeit.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
    }
    public Pair<Integer,Integer> pairEndzeit() {
        String[] s = endzeit.split(":");
        return new Pair<Integer, Integer>(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
    }

}
