package com.nungil.Dto;

import lombok.*;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SettingDTO {
    private int settingId;
    private Long userId;
    private char isAlert;
}



