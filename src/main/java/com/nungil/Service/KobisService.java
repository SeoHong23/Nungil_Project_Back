package com.nungil.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KobisService {

    private final RestTemplate restTemplate;

    @Value("${kobis.api-key}")
    private String apiKey;

    public KobisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getMovieDetails(String movieCd) {
        String url = String.format(
                "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json?key=%s&movieCd=%s",
                apiKey, movieCd
        );
        return restTemplate.getForObject(url, Map.class);

    }

}
