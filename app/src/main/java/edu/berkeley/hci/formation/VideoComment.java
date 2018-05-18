package edu.berkeley.hci.formation;

/*
 * Created by Ashley on 04/19
 * Class to store the comments made on videos by choreographers
 */
public class VideoComment {
    private String comment = "";
    private int position = 0;

    public VideoComment(){
        //empty constructor for Firebase
    }

    public VideoComment(String comment, int position) {
        this.comment = comment;
        this.position = position;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
