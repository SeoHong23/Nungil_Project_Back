package com.nungil.Document;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "plot")
public class PlotDocument {

    @Id
    private String id;

    private String plotLang;
    private String plotText;

    // 생성자, getter, setter 생략
}

