package com.nungil.Document;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "staff")
public class StaffDocument {

    @Id
    private String id;

    private String staffNm;
    private String staffRoleGroup;
    private String staffRole;
    private String staffId;

    // 생성자, getter, setter 생략
}