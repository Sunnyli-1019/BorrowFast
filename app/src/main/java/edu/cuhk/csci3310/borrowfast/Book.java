package edu.cuhk.csci3310.borrowfast;

import com.google.firebase.events.Publisher;

import java.sql.Timestamp;
import java.util.List;

import kotlin.PublishedApi;

public class Book {

    private String title;
    private String author;
    private String publisher;
    private String description;
    private String qrcode;
    private String img;
    private Timestamp due_date;

    private int bid;
    private int borrowedUID;
    private List<String> category;
    private String yearOfPublish; // temporary
    private double rating;
    private int numRatings;
    private int CID;

    public Book(){}

    public Book(String title, String author, String publisher, String description, String qrcode, String img, Timestamp due_date,
                int bid, int borrowedUID, List<String> category, String yearOfPublish, double rating, int numRatings, int CID){
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.qrcode = qrcode;
        this.img = img;
        this.bid= bid;
        this.borrowedUID = borrowedUID;
        this.CID = CID;
        this.category = category;
        this.yearOfPublish = yearOfPublish;
        this.rating = rating;
        this.numRatings = numRatings;
        this.due_date = due_date;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getNumRatings() {
        return numRatings;
    }
    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }
    public Timestamp getDueDate() {return due_date; };

    public String getYearOfPublish() {
        return yearOfPublish;
    }
    public String getPublisher() {
        return publisher;
    }

}
