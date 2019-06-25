package com.othregensburg.ourglass.Entity;

public class ProjectClassification {
    public String projekt;
    public String taetigkeit;
    public String notiz;
    public int minuten;

    public ProjectClassification(){

    }

    public ProjectClassification(String taetigkeit, String projekt, String notiz, int minuten) {
        this. projekt = projekt;
        this.taetigkeit = taetigkeit;
        this.notiz = notiz;
        this.minuten = minuten;
    }
}
