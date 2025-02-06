package com.nungil.Document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document(collection = "plot")
public class PlotDocument {

    @Id
    private String id;

    private String plotLang;

    @Getter
    private String plotText;

}

