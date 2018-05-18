package edu.berkeley.hci.formation;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.UUID;

public abstract class Block implements Parcelable {

    public static final String STATIC = "Static";
    public static final String TRANSITION = "Transition";

    public String uid;
    public long startTime;
    public long endTime;
    public long defaultLength;
    public String type;
    public String title;
    public int thumbnail;
    public byte[] thumbnailimage;
    public String thumbnailimageString;
    public String notes;

    Block() {

    }

    Block(Long startTime) {
        this.uid = UUID.randomUUID().toString();
        this.startTime = startTime;
        defaultLength = 10;
        this.type = "Block";
        this.endTime = this.startTime + defaultLength;
        this.notes = "";
    }

    Block(Block beforeBlock) {
        this.uid = UUID.randomUUID().toString();
        defaultLength = 10;
        this.type = "Block";
        if (beforeBlock != null) {
            this.startTime = beforeBlock.getEndTime();
            this.endTime = this.startTime + this.defaultLength;
        } else {
            this.startTime = 0L;
            this.endTime = 10L;
        }
        this.title = "Block Title";
        this.notes = "";


    }

    public String getUid() {
        return this.uid;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public ArrayList<Dot> getDots() {
        return new ArrayList<Dot>();
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public void addDot(Dot dot) {
    }

    public void removeDot(Dot dot) {
    }

    public byte[] getThumbnail() {
        return this.thumbnailimage;

    }

    public void setThumbnail(byte[] image) {
        this.thumbnailimage = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeLong(this.defaultLength);
        dest.writeString(this.type);
        dest.writeString(this.title);
        dest.writeInt(this.thumbnail);
        dest.writeInt(this.thumbnailimage.length);
        dest.writeByteArray(this.thumbnailimage);
        dest.writeString(this.notes);

    }

    public Block(Parcel in){
        this.uid = in.readString();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.defaultLength = in.readLong();
        this.type = in.readString();
        this.title = in.readString();
        this.thumbnail = in.readInt();
        byte[] data = new byte[in.readInt()];
        in.readByteArray(data);
        this.notes = in.readString();
    }

    public void incrStart() {

        this.startTime += 1;

    }

    public void incrEnd() {

        this.endTime += 1;
    }

    public void decrStart() {
        if (this.startTime > 0) {
            this.startTime -= 1;
        }
    }

    public void decrEnd() {

        if (this.endTime > 0) {
            this.endTime -= 1;
        }
    }

    public String getNotes() {
        return this.notes;
    }
    public void setNotes(String str) {this.notes = str;}

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return this.type;
    }


}