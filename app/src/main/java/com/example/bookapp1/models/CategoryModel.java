package com.example.bookapp1.models;

public class CategoryModel {
    // make sure to use some spellings for model variables as in firebase
    String id, category, uid;
    long timeStamp;

    // require empty constructor for firebase
    public CategoryModel() {
    }

    // create the parameterized constructor
    public CategoryModel(String id, String category, String uid, long timeStamp) {
        this.id = id;
        this.category = category;
        this.uid = uid;
        this.timeStamp = timeStamp;
    }

    // creating getters/ setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
