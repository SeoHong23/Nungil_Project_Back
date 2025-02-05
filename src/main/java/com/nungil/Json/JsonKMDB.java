package com.nungil.Json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * 생성날짜 : 2025/01/24
 * 이름 : 김주경
 * 01.24 - KMDb에 api요청한 json 데이터 형태 작성
 */

@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonKMDB {
    @JsonProperty("TotalCount")
    private int totalCount;

    @JsonProperty("Data")
    private List<JsonKMDBData> data;
}

