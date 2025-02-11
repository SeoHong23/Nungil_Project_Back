package com.nungil.Dto;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VideoRankResponseDTO {
    private String id;
    private String title;
    private String poster;
    private String rank;
    private String rankInten;
    private String rankOldAndNew;
}
