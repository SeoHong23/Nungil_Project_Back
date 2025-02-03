package com.nungil.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class KobisService {

    private final RestTemplate restTemplate;

    @Value("${kobis.api-key}")
    private String apiKey;

    public KobisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public JsonNode getMovieByTitle(String movieTitle) {
        try {
            String encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieList.json?key=b6294028408df8733f4113de4cc9f206&movieNm=" + encodedTitle;

            // HTTP 요청
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 응답 읽기
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(response.toString());

            // 디버깅 로그
            System.out.println("KOBIS API 응답 데이터: " + responseJson);

            return responseJson.path("movieListResult").path("movieList");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}