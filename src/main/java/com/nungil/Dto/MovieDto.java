package com.nungil.Dto;

import java.util.List;

public class MovieDto {
    private String title;
    private double rating;
    private String releaseDate;
    private List<String> platform;

    public MovieDto(String title, double rating, String releaseDate, List<String> platform) {
        this.title = title;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.platform = platform;

    }

    public String getTitle() {
        return title;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public List<String> getPlatforms() {
        return platform;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setPlatform(List<String> platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "MovieDto{" +
                "title='" + title + '\'' +
                ", rating=" + rating +
                ", releaseDate='" + releaseDate + '\'' +
                ", platform=" + platform +
                '}';
    }
}

