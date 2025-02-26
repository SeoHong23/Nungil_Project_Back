package com.nungil.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.Document.MovieDocument;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbService {

    @Value("${api.tmdb.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final static Logger logger = LoggerFactory.getLogger(VideoService.class);

    private ResponseEntity<String> fetchTmdbData(String url, int retryCount) throws InterruptedException {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            int MAX_RETRIES = 3;
            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && retryCount < MAX_RETRIES) {
                logger.warn("Rate limit exceeded. Retrying... Attempt: {}", retryCount + 1);
                Thread.sleep(10000);  // 개선: Retry-After 헤더 확인 가능
                return fetchTmdbData(url, retryCount + 1);
            }
            return response;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("TMDB resource not found for URL: {}", url);
            } else {
                logger.error("HTTP error while calling TMDB API: {}", e.getStatusCode(), e);
            }
            return null;
        }
    }


    public Integer getTmdbId(String title, String releaseYear) {
        Integer movieId = searchTmdbId(title, releaseYear, "movie");
        Integer tvId = searchTmdbId(title, releaseYear, "tv");

        if (movieId == null) return tvId;  // 영화 결과 없으면 TV 반환
        if (tvId == null) return movieId;  // TV 결과 없으면 영화 반환

        // 둘 다 있으면 유사도 비교
        double movieSimilarity = calculateSimilarity(title, getTitleById(movieId, "movie"));
        double tvSimilarity = calculateSimilarity(title, getTitleById(tvId, "tv"));

        return (movieSimilarity >= tvSimilarity) ? movieId : tvId;
    }

    private Integer searchTmdbId(String title, String releaseYear, String type) {
        String url = "https://api.themoviedb.org/3/search/" + type +
                "?query=" + UriUtils.encode(title, StandardCharsets.UTF_8) +
                "&year=" + releaseYear + "&api_key=" + apiKey + "&language=ko";

        try {
            ResponseEntity<String> response = fetchTmdbData(url,0);
            if (response != null && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode results = objectMapper.readTree(response.getBody()).path("results");

                if (results.isArray() && !results.isEmpty()) {
                    double highestSimilarity = 0.0;
                    Integer bestMatchId = null;

                    for (JsonNode result : results) {
                        String resultTitle = "movie".equals(type) ? result.path("title").asText("") : result.path("name").asText("");
                        double similarity = calculateSimilarity(title, resultTitle);

                        logger.debug("Comparing '{}' with '{}', Similarity: {}", title, resultTitle, similarity);

                        if (similarity > highestSimilarity) {
                            highestSimilarity = similarity;
                            bestMatchId = result.path("id").asInt();
                        }
                    }

                    if (bestMatchId != null) {
                        logger.info("Best match for '{}' ({}) in '{}': ID={} with similarity={}", title, releaseYear, type, bestMatchId, highestSimilarity);
                        return bestMatchId;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during TMDB search for '{}' ({}) [{}]: {}", title, releaseYear, type, e.getMessage());
        }

        logger.warn("No suitable match found for '{}' ({}) in '{}'", title, releaseYear, type);
        return null;
    }


    private String getTitleById(Integer id, String type) {
        String url = "https://api.themoviedb.org/3/" + type + "/" + id + "?api_key=" + apiKey + "&language=ko";
        try {
            ResponseEntity<String> response = fetchTmdbData(url,0);
            if (response != null && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBody());
                return "movie".equals(type) ? root.path("title").asText("") : root.path("name").asText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private double calculateSimilarity(String title1, String title2) {
        return new JaroWinklerSimilarity().apply(title1, title2);
    }


    public List<MovieDocument.OTTInfo> getOttInfo(Integer tmdbId) {
        List<MovieDocument.OTTInfo> ottInfos = new ArrayList<>();

        // movie와 tv 모두 탐색
        ottInfos.addAll(fetchOttInfoByType(tmdbId, "movie"));
        ottInfos.addAll(fetchOttInfoByType(tmdbId, "tv"));

        // 중복 OTT 제거
        return new ArrayList<>(ottInfos.stream()
                .collect(Collectors.toMap(MovieDocument.OTTInfo::getPlatform, info -> info, (info1, info2) -> info1))
                .values());
    }

    private List<MovieDocument.OTTInfo> fetchOttInfoByType(Integer tmdbId, String type) {
        String url = "https://api.themoviedb.org/3/" + type + "/" + tmdbId + "/watch/providers?api_key=" + apiKey;
        List<MovieDocument.OTTInfo> ottInfos = new ArrayList<>();

        try {
            ResponseEntity<String> response = fetchTmdbData(url,0);
            if (response != null && response.getBody() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode results = objectMapper.readTree(response.getBody()).path("results");

                // 한국(KR) OTT 목록 추출
                JsonNode kr = results.path("KR");
                List<String> krProviders = new ArrayList<>();
                if (kr.has("flatrate")) {
                    kr.path("flatrate").forEach(node -> krProviders.add(node.path("provider_name").asText()));
                }

                // 글로벌 OTT 서비스 목록
                List<String> globalProviders = List.of("Netflix", "Disney Plus", "Amazon Prime Video", "Apple TV+", "Google Play Movies");

                // 글로벌 OTT 예외 목록 (한국에 없어도 추가)
                List<String> krGlobalExceptions = List.of("Apple TV+", "Google Play Movies");

                // 모든 국가 탐색
                results.fieldNames().forEachRemaining(countryCode -> {
                    JsonNode countryNode = results.path(countryCode);
                    if (countryNode.has("flatrate")) {
                        countryNode.path("flatrate").forEach(providerNode -> {
                            String providerName = providerNode.path("provider_name").asText();

                            // 조건:
                            // 1. 글로벌 서비스 AND 한국에 실제 존재
                            // 2. 예외 목록 포함 시 한국에 없어도 추가
                            if (globalProviders.contains(providerName) &&
                                    (krProviders.contains(providerName) || krGlobalExceptions.contains(providerName))) {
                                ottInfos.add(new MovieDocument.OTTInfo(
                                        providerName,
                                        true,  // 사용 가능
                                        null   // 필요 시 링크 추가
                                ));
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ottInfos;
    }

}
