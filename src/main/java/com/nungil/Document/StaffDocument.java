package com.nungil.Document;

import com.nungil.Dto.StaffDTO;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "staff")
public class StaffDocument {

    @Id
    private String id;

    private String staffNm;
    private String staffRoleGroup;
    private String staffRole;
    private String staffId;

    public StaffDTO toDTO() {
        return StaffDTO.builder()
                .id(id)
                .staffNm(staffNm)
                .staffRoleGroup(staffRoleGroup)
                .staffRole(staffRole)
                .staffId(staffId)
                .build();
    }
}