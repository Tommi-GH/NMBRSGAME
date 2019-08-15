package com.example.tommi.nmbrs;

import android.support.annotation.NonNull;

class Record implements Comparable<Record>{

    private int level;
    private String name;
    private int score;
    private String time;

    public Record (){

    }

    Record(int level, String name, int score, String time){
        this.level = level;
        this.name = name;
        this.score = score;
        this.time = time;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public String getTime() {
        return time;
    }

    @Override
    public int compareTo(@NonNull Record record) {

        if (record.getScore() == this.score){
            return -1*record.getTime().compareTo(this.time);
        }
        return Integer.compare(record.getScore(),this.score);
    }

    @Override
    public String toString() {

        return "-- "+name+" - "+score+" - "+time+" --";
    }
}
