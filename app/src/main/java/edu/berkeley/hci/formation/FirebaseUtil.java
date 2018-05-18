package edu.berkeley.hci.formation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

final class FirebaseUtil {

    private static FirebaseDatabase mDatabase;
    private static FirebaseStorage mStorage;

    private static DatabaseReference getDatabaseRef() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase.getReference();
    }

    private static StorageReference getStorageRef() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance();
        }
        return mStorage.getReference();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static DatabaseReference getPermissionsRef() {
        return getDatabaseRef().child("Permissions");
    }

    public static DatabaseReference getProjectsRef() {
        return getDatabaseRef().child("Projects");
    }

    public static DatabaseReference getUsersRef() {
        return getDatabaseRef().child("Users");
    }

    public static StorageReference getVideoStorageRef() {
        return getStorageRef().child("Videos");
    }

    public static DatabaseReference getCommentsRef(){return getDatabaseRef().child("Comments");}
}
