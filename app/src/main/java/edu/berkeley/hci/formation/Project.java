package edu.berkeley.hci.formation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by daphnenhuch on 4/6/18.
 */

public class Project implements Parcelable{
    private Blocking blocking;
    private List<String> videoIds = new ArrayList<>();
    private List<String> videoTitles = new ArrayList<>();
    private List<String> sharedUserUUIDs = new ArrayList<>();
    //   public ArrayList<Dancer> dancers;
    private String title;
    private String uuid;

    public Project(Parcelable in) {
        in.describeContents();
    }

    public Project() {
        this(new Blocking(), "Default Project", UUID.randomUUID().toString());
    }

    public Project(Blocking blocking, String title) {
        this(blocking, title, UUID.randomUUID().toString());
    }

    public Project(Blocking blocking, String title, String uuid) {
        this.blocking = blocking;
        this.title = title;
        this.uuid = uuid;
    }

    public Project(Parcel in){
        int len = in.readInt();
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (int i = 0; i < len; i++) {
            String type = in.readString();
            if (type.equals(Block.STATIC)) {
                StaticBlock sb = (StaticBlock) in.readTypedObject(staticblockCreator);
                blocks.add(sb);
            } else {
                TransitionBlock tb = (TransitionBlock) in.readTypedObject(transitionBlockCreator);
                blocks.add(tb);
            }
        }
        this.blocking = new Blocking(blocks);
        this.title = in.readString();
        this.uuid = in.readString();
        this.videoIds = new ArrayList<>();
        this.videoTitles = new ArrayList<>();
        this.sharedUserUUIDs = new ArrayList<>();
        int vidLen = in.readInt();
        for (int i = 0; i < vidLen; i++) {
            videoIds.add(in.readString());
            videoTitles.add(in.readString());
        }
        int idLen = in.readInt();
        for (int i = 0; i < idLen; i++) {
            sharedUserUUIDs.add(in.readString());
        }
    }

    public void updateDatabase() {
        for (Block b : blocking.getBlocks()) {
            b.setThumbnail(null);
        }
        FirebaseUtil.getProjectsRef().child(uuid).setValue(this);
    }

    public static final Parcelable.Creator transitionBlockCreator = new Parcelable.Creator() {
        public Block createFromParcel(Parcel in) {
            return new TransitionBlock(in);
        }

        public TransitionBlock[] newArray(int size) {
            return new TransitionBlock[size];
        }
    };

    public static final Parcelable.Creator staticblockCreator = new Parcelable.Creator() {
        public Block createFromParcel(Parcel in) {
            return new StaticBlock(in);
        }

        public StaticBlock[] newArray(int size) {
            return new StaticBlock[size];
        }
    };

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.getBlocking().getBlocks().size());
        for (Block b : this.getBlocking().getBlocks()) {
            if (b.getType().equals(Block.STATIC)) {
                dest.writeString(Block.STATIC);
                dest.writeTypedObject((StaticBlock) b, 0);
            } else {
                dest.writeString(Block.TRANSITION);
                dest.writeTypedObject((TransitionBlock) b, 0);

            }
        }
        dest.writeString(title);
        dest.writeString(uuid);
        dest.writeInt(videoIds.size());
        for (int i = 0; i < getNumVideos(); i++) {
            dest.writeString(videoIds.get(i));
            dest.writeString(videoTitles.get(i));
        }
        dest.writeInt(sharedUserUUIDs.size());
        for (String id : sharedUserUUIDs) {
            dest.writeString(id);
        }

    }

    public void setBlock(Block block, int index) {
        blocking.setBlock(block, index);
    }

    public void addBlock(Block block) {
        blocking.addBlock(block);
    }

    public void addBlock(Block block, Integer index) {
        blocking.addBlock(block, index);
    }

    public Blocking getBlocking() {
        return this.blocking;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVideoId(int position) {
        if (position < 0 || videoIds.size() <= position) {
            return null;
        }
        return videoIds.get(position);
    }

    public String getVideoTitle(int position) {
        if (position < 0 || videoTitles.size() <= position) {
            return null;
        }
        return videoTitles.get(position);
    }

    public void addVideo(String videoId, String videoTitle) {
        videoIds.add(videoId);
        videoTitles.add(videoTitle);
    }

    public List<String> getVideoIds(){
        return new ArrayList<>(videoIds);
    }

    public void setVideoIds(List<String> videoIds) {
        this.videoIds = new ArrayList<>(videoIds);
    }

    public List<String> getVideoTitles() {
        return new ArrayList<>(videoTitles);
    }

    public void setVideoTitles(List<String> videoTitles) {
        this.videoTitles = new ArrayList<>(videoTitles);
    }

    public int getNumVideos() {
        return videoIds.size();
    }

    public List<String> getSharedUserUUIDs() {
        return new ArrayList<>(sharedUserUUIDs);
    }

    public void setSharedUserUUIDs(List<String> sharedUserUUIDs) {
        this.sharedUserUUIDs = new ArrayList<>(sharedUserUUIDs);
    }

    public void addSharedUserUUID(String sharedUserUUID) {
        this.sharedUserUUIDs.add(sharedUserUUID);
    }
}
