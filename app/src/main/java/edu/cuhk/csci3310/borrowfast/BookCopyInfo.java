package edu.cuhk.csci3310.borrowfast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class BookCopyInfo {
    GeoPoint libraryLocation;
    String libraryName;
    String borrowedUID;
    Timestamp borrowedDate;
    Timestamp dueDate;

    public BookCopyInfo(){}

    public BookCopyInfo(GeoPoint libraryLocation, String libraryName, String borrowedUID, Timestamp borrowedDate, Timestamp dueDate){
        this.libraryLocation = libraryLocation;
        this.libraryName = libraryName;
        this.borrowedUID = borrowedUID;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
    }

    public GeoPoint getLibraryLocation() {
        return libraryLocation;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getBorrowedUID() {
        return borrowedUID;
    }
    public Timestamp getBorrowedDate() {
        return borrowedDate;
    }
    public Timestamp getDueDate() {
        return dueDate;
    }
}
