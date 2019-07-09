package com.othregensburg.ourglass.Entity;

public class ProjectClassification {
    public String project;
    public String activity;
    public String note;
    public int minutes;

    public ProjectClassification(){

    }

    public ProjectClassification(String activity, String project, String note, int minutes) {
        this.project = project;
        this.activity = activity;
        this.note = note;
        this.minutes = minutes;
    }
}
