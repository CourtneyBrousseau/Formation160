package edu.berkeley.hci.formation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class StaticBlock extends Block {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Dot createFromParcel(Parcel in) {
            return new Dot(in);
        }

        public Dot[] newArray(int size) {
            return new Dot[size];
        }
    };

    public ArrayList<Dot> dots;
    public String type;

    StaticBlock() {
        this.dots = new ArrayList<>();
    }

    StaticBlock(Block beforeBlock) {
        super(beforeBlock);
        this.type = Block.STATIC;

        try {
            //If previous block was a transition set current block to end state of transition
            if (beforeBlock.getType().equals(Block.TRANSITION)) {
                this.dots = beforeBlock.getDots();
            } else {
                //Otherwise set it to an empty list
                this.dots = new ArrayList<>();
            }
        } catch (RuntimeException e) {
            this.dots = new ArrayList<>();
        }


    }

    StaticBlock(Parcel in) {
        super.uid = in.readString();
        super.startTime = in.readLong();
        super.endTime = in.readLong();
        super.defaultLength = in.readLong();
        this.type = in.readString();
        super.title = in.readString();
        super.thumbnail = in.readInt();
        int thumbNailLength = in.readInt();
        if (thumbNailLength > 0) {
            byte[] data = new byte[thumbNailLength];
            in.readByteArray(data);

            super.thumbnailimage = data;
        }
        ArrayList<Dot> dots = new ArrayList<>();

        int dotLen = in.readInt();
        for (int i = 0; i < dotLen; i++) {
            Dot d = (Dot) in.readParcelable(Dot.class.getClassLoader());
            dots.add(d);
        }
        this.dots = dots;
        super.notes = in.readString();
        super.thumbnailimageString = in.readString();


    }

    public String getType() {
        return Block.STATIC;
    }

    @Override
    public void addDot(Dot dot) {
        this.dots.add(dot);
    }

    public void removeDot(Dot dot) {
        dots.remove(dot);
    }

    public byte[] getThumbnail() {
        return super.thumbnailimage;
    }

    public void setThumbnail(byte[] img) {
        super.thumbnailimage = img;
    }

    public ArrayList<Dot> getDots() {
        return this.dots;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeLong(super.startTime);
        dest.writeLong(super.endTime);
        dest.writeLong(super.defaultLength);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeInt(thumbnail);
        try {
            dest.writeInt(super.thumbnailimage.length);
        } catch (NullPointerException e ) {
            dest.writeInt(0);

        }
        if (super.thumbnailimage != null) {
            dest.writeByteArray(super.thumbnailimage);
        }

        dest.writeInt(this.dots.size());
        for (Dot d : dots) {
            dest.writeParcelable(d, 0);
        }
        dest.writeString(super.notes);
        dest.writeString(super.thumbnailimageString);


    }

    public void setStartTime(long startTime) {
        super.startTime = startTime;
    }

}

