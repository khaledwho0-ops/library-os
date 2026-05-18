package com.library.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private double price;
    private String imagePath;

    // Constructor for Database Reading
    public Book(int id, String title, String author, String genre, double price, String imagePath) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.imagePath = imagePath;
    }

    // Constructor for Creating New Books
    public Book(String title, String author, String genre, double price, String imagePath) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.imagePath = imagePath;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }

    @Override
    public String toString() { return title; }
}