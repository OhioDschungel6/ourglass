package com.othregensburg.ourglass.entity;

public class User {
    String Name;
    String Vorname;
    double Sollstd;
    boolean timeRunning=false;

    public User() {
    }

    public User(String name, String vorname, double sollstd) {
        Name = name;
        Vorname = vorname;
        Sollstd = sollstd;
    }
}
