package edu.berkeley.hci.formation;

import android.net.Uri;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by cindy on 4/16/2018.
 */

public class User{
    public String uuid;
    public String displayName;
    public String email;
    public String photoUrl;

    public User() {

    }

    public User(String displayName, String email, String uuid, String photoUrl) {
        this.displayName = displayName;
        this.email = email;
        this.uuid = uuid;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return displayName;
    }

    public String getImage() {
        return photoUrl;
    }

    public String getUUID() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }
}