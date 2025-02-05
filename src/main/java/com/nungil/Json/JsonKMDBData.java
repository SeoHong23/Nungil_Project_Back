package com.nungil.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonKMDBData {


    @JsonProperty("CollName")
    private String collName;


    @JsonProperty("TotalCount")
    private int totalCount;

    @JsonProperty("Count")
    private int count;

    @JsonProperty("Result")
    private List<JsonVideo> result;
}
