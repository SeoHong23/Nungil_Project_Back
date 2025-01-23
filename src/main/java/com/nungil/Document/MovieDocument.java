package com.nungil.Document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 :  MovieDocument 생성
 */

@Document(collation = "movies")
public class MovieDocument {
    @Id
    private String id;
    private String title;
    private String genre;
    private String ottPlatform;
    private String url;

    @Override
    public String toString() {
        return "MovieDocument{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", ottPlatform='" + ottPlatform + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
