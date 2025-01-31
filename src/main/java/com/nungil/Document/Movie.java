package com.nungil.Document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 :  MovieDocument 생성
 */

@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String title;
    private double rating;
    private String releaseDate;
    private String platform;
    private String imageUrl;
    private LocalDateTime lastUpdated;

    public String getId() {
        return id;
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

    public String getPlatform() {
        return platform;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }


    public void setId(String id) {
        this.id = id;
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

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", rating=" + rating +
                ", releaseDate='" + releaseDate + '\'' +
                ", platform='" + platform + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
