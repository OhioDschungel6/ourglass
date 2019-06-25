package com.othregensburg.ourglass.Entity;

import java.util.Map;

public class Workday {

    public boolean urlaub;
    public boolean krank;
    public Map<String,Stamp> timestamps;
    public Map<String, ProjectClassification> einteilung;
}
