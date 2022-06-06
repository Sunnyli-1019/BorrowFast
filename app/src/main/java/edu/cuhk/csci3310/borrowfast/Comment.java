package edu.cuhk.csci3310.borrowfast;

public class Comment {

    private String uid;
    private String comment;
    private int numUpvotes;
    private int numDownvotes;

    public Comment(){}

    public Comment(String uid, String comment, int numUpvotes, int numDownvotes){
        this.uid = uid;
        this.comment = comment;
        this.numUpvotes = numUpvotes;
        this.numDownvotes = numDownvotes;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setNumUpvotes(int numUpvotes) {
        this.numUpvotes = numUpvotes;
    }

    public int getNumUpvotes() {
        return numUpvotes;
    }

    public void setnumDownvotes(int numDownvotes) {
        this.numDownvotes = numDownvotes;
    }

    public int getnumDownvotes() {
        return numDownvotes;
    }
}
