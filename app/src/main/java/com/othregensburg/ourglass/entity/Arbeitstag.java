package com.othregensburg.ourglass.entity;

import java.util.List;
import java.util.Map;

public class Arbeitstag {

    public boolean urlaub;
    public boolean krank;
    public Map<String,Stamp> timestamps;
    public Map<String,Projekteinteilung> einteilung;
}
