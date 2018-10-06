package com.example.sarthak.meetify;

import android.os.Parcel;
import android.os.Parcelable;

class Dialogue implements Parcelable {

    private int speakerId;
    private Double start_time;
    private Double end_time;
    private String transcript;


    protected Dialogue(Parcel in) {
        speakerId = in.readInt();
        if (in.readByte() == 0) {
            start_time = null;
        } else {
            start_time = in.readDouble();
        }
        if (in.readByte() == 0) {
            end_time = null;
        } else {
            end_time = in.readDouble();
        }
        transcript = in.readString();
    }

    public static final Creator<Dialogue> CREATOR = new Creator<Dialogue>() {
        @Override
        public Dialogue createFromParcel(Parcel in) {
            return new Dialogue(in);
        }

        @Override
        public Dialogue[] newArray(int size) {
            return new Dialogue[size];
        }
    };

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

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    Dialogue()
    {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(speakerId);
        if (start_time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(start_time);
        }
        if (end_time == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(end_time);
        }
        dest.writeString(transcript);
    }
}
