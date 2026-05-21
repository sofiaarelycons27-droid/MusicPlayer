// models/Song.java
package com.musicplayer.models;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private String duration;
    private String fileName;
    private boolean isFavorite;
    private int userId;  // Dueño de la canción

    public Song(int id, String title, String artist, String genre, String duration,
                String fileName, boolean isFavorite, int userId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.fileName = fileName;
        this.isFavorite = isFavorite;
        this.userId = userId;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}