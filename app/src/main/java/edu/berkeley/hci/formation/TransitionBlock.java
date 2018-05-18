package edu.berkeley.hci.formation;

import android.graphics.Path;
import android.os.Parcel;

import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class TransitionBlock extends Block {
    public static final Creator<TransitionBlock> CREATOR = new Creator<TransitionBlock>() {
        @Override
        public TransitionBlock createFromParcel(Parcel source) {
            return new TransitionBlock(source);
        }

        @Override
        public TransitionBlock[] newArray(int size) {
            return new TransitionBlock[size];
        }
    };
    public ArrayList<Dot> dotsBefore;
    public ArrayList<Dot> dotsAfter;
    public HashMap<String, Path> paths;
    public Long duration;
    public String type;
    public int defaultLength;

    TransitionBlock() {

    }

    TransitionBlock(Block beforeBlock) {
        super(beforeBlock);

        this.type = Block.TRANSITION;
        this.dotsBefore = beforeBlock.getDots();
        this.paths = new HashMap<>();
        this.duration = Integer.toUnsignedLong(5000);
        this.defaultLength = 5;
        super.endTime = super.startTime + this.defaultLength;

    }

    protected TransitionBlock(Parcel in) {
        super.uid = in.readString();

        this.dotsBefore = in.createTypedArrayList(Dot.CREATOR);
        this.dotsAfter = in.createTypedArrayList(Dot.CREATOR);

        this.paths = new HashMap<>();
        int hashLen = in.readInt();
        for (int i = 0; i < hashLen; i++) {
            String key = in.readString();
            int len = in.readInt();
            float[] approx = new float[len];
            in.readFloatArray(approx);
            Path path = new Path();
            path.moveTo(approx[1], approx[2]);
            for (int c = 0; c < approx.length; c += 3) {
                path.lineTo(approx[c + 1], approx[c + 2]);
            }

            this.paths.put(key, path);
        }
        super.startTime = (Long) in.readValue(Long.class.getClassLoader());
        super.endTime = (Long) in.readValue(Long.class.getClassLoader());
        this.type = in.readString();
        super.title = in.readString();
        this.duration = in.readLong();
        this.defaultLength = in.readInt();
        int thumbNailLength = in.readInt();
        if (thumbNailLength > 0) {
            byte[] data = new byte[thumbNailLength];
            in.readByteArray(data);
            super.thumbnailimage = data;
        }
        super.notes = in.readString();
        super.thumbnailimageString = in.readString();


    }

    public void fixEndTime() {
        this.defaultLength = 5;
        super.endTime -= 5;
    }

    public void addDotPath(Path path, Dot dot, Float endX, Float endY) {
        paths.put(dot.uid, path);
        dot.setCoordinates(endX, endY);
        dotsAfter.add(dot);
    }

    public ArrayList<Dot> getDots() {
        return this.dotsAfter;
    }

    public ArrayList<Dot> getDotsBefore() {
        return this.dotsBefore;
    }

    public void addEndBlock(Block endBlock) {
        this.dotsAfter = endBlock.getDots();
        super.endTime = endBlock.getStartTime() + super.defaultLength;
    }

    public Long getEndTime() {
        return super.endTime;
    }

    public void makePaths() {
        for (Dot beforeDot : dotsBefore) {
            Dot dotAfter = findAfterDotById(beforeDot.getUid());

            Path path = new Path();
            if (dotAfter == null) {
                path.moveTo(beforeDot.getX(), beforeDot.getY());
                path.lineTo(beforeDot.getX(), beforeDot.getY());

            } else {
                path.moveTo(beforeDot.getX(), beforeDot.getY());
                path.lineTo(dotAfter.getX(), dotAfter.getY());
            }
            paths.put(beforeDot.getUid(), path);

        }

    }

    public Dot findAfterDotById(String id) {
        if (dotsAfter == null) {
            return null;
        }
        for (Dot d : dotsAfter) {
            if (id.equals(d.getUid())) {
                return d;
            }
        }
        return null;
    }

    public HashMap getPaths() {
        return this.paths;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(super.uid);
        dest.writeTypedList(this.dotsBefore);
        dest.writeTypedList(this.dotsAfter);
//        dest.writeSerializable((Serializable) this.paths);
        dest.writeInt(this.paths.keySet().size());
        for (HashMap.Entry<String, Path> entry : this.paths.entrySet()) {
            dest.writeString(entry.getKey());
            float[] approx = entry.getValue().approximate(0);
            dest.writeInt(approx.length);
            dest.writeFloatArray(approx);
        }
        dest.writeValue(super.startTime);
        dest.writeValue(super.endTime);
        dest.writeString(this.type);
        dest.writeString(super.title);
        dest.writeLong(this.duration);
        dest.writeInt(this.defaultLength);
        try {
            dest.writeInt(super.thumbnailimage.length);
        } catch (NullPointerException e ) {
            dest.writeInt(0);

        }
        if (super.thumbnailimage != null) {
            dest.writeByteArray(super.thumbnailimage);
        }

        dest.writeString(super.notes);
        dest.writeString(super.thumbnailimageString);


    }

    public String getType() {
        return Block.TRANSITION;
    }

    public void updatePath(String uid, Path p) {
        this.paths.put(uid, p);
    }

    public void setDotsAfter(ArrayList<Dot> dots) {
        this.dotsAfter = dots;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
