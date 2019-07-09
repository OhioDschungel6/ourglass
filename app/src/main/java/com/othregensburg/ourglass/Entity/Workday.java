package com.othregensburg.ourglass.Entity;

import java.util.Map;

public class Workday {

    public boolean holiday;
    public boolean ill;
    public Map<String,Stamp> timestamps;
    public Map<String, ProjectClassification> classification;
}
