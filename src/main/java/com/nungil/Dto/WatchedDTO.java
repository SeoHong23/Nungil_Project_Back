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
public class WatchedDTO {

    private Long userId;
    private String videoId;
    private Date createdAt;
}
