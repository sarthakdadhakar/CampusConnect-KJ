package com.example.sarthak.meetify;

import java.util.Comparator;

class AllSpeakers {
    private int speakerId;

    public int getSpeakerId() {
        return speakerId;
    }

    public void setSpeakerId(int speakerId) {
        this.speakerId = speakerId;
    }

    public Double getStart_time() {
        return start_time;
    }

    public void setStart_time(Double start_time) {
        this.start_time = start_time;
    }

    public Double getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Double end_time) {
        this.end_time = end_time;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    private Double start_time;
    private Double end_time;
    boolean isFinal;

}
