package com.nungil.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@NoArgsConstructor
@Setter
@Getter
public class NotInterestedDTO {

    private int id;
    private Long userId;
    private String videoId;
    private Date createdAt = new Date();

    public NotInterestedDTO(int id, Long userId, String videoId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.videoId = videoId;
        this.createdAt = createdAt;
    }
}
