package com.nungil.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter
public class VideoListDTO {
    private String id;
    private String title;
    private String poster; // 첫 번째 포스터만 가져옴

    public VideoListDTO(String id, String title, String poster) {
        this.id = id;
        this.title = title;
        this.poster = poster;
    }
}