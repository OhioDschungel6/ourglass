package com.othregensburg.ourglass.entity;

public class Projekteinteilung {
    public String projekt;
    public String taetigkeit;
    public String notiz;
    public int minuten;

    public Projekteinteilung(String taetigkeit, String projekt, String notiz, int minuten) {
        this. projekt = projekt;
        this.taetigkeit = taetigkeit;
        this.notiz = notiz;
        this.minuten = minuten;
    }
}
