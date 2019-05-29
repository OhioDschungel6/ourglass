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



    public Pair<Integer,Integer> getPairStartzeit() {
        return new Pair<Integer, Integer>(Integer.parseInt(startzeit.substring(0,2)),Integer.parseInt(startzeit.substring(3,5)));
    }

}
