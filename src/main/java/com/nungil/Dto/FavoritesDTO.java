package com.nungil.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Setter
@Getter
public class FavoritesDTO {

    private Long userId;
    private String videoId;

}
