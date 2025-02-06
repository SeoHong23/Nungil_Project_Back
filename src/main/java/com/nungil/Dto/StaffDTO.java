package com.nungil.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {

    private String id;

    private String staffNm;
    private String staffRoleGroup;
    private String staffRole;
    private String staffId;

}