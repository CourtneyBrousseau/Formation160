package edu.berkeley.hci.formation;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class Dot implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Dot createFromParcel(Parcel in) {
            return new Dot(in);
        }

        public Dot[] newArray(int size) {
            return new Dot[size];
        }
    };
    public String uid;
    // public Dancer dancer;
    public int color;
    public Float x;
    public Float y;
    public String userid;
    public Boolean changed;

    public Dot() {
        this.uid = UUID.randomUUID().toString();
        this.color = Color.BLACK;
        this.changed = false;
        this.x = new Float(0);
        this.y = new Float(0);
        this.userid="";

    }

    Dot(Parcel in) {
        this.uid = in.readString();
        this.color = in.readInt();
        this.x = in.readFloat();
        this.y = in.readFloat();
        this.changed = in.readByte() != 0;
        this.userid = in.readString();
    }

//    public void setDancer(Dancer dancer) {
//        this.dancer = dancer;
//    }

    public void setCoordinates(Float x, Float y) {

        this.x = x;
        this.y = y;


    }

    public void setColor(Integer color) {
        this.color = color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeInt(color);
        dest.writeFloat(this.x);
        dest.writeFloat(this.y);
        dest.writeByte((byte) (this.changed ? 1 : 0));
        dest.writeString(userid);
    }

    public String getUid() {
        return this.uid;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }

    public void tag(String userid){
        this.userid = userid;
    }
}
