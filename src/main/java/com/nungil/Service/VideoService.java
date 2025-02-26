package com.nungil.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.Document.MovieDocument;
import com.nungil.Document.VideoDocument;
import com.nungil.Dto.VideoDTO;
import com.nungil.Json.JsonKMDB;
import com.nungil.Json.JsonKMDBData;
import com.nungil.Json.JsonVideo;
import com.nungil.Repository.ApiVideoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final ApiVideoRepository videoRepository;
    private final R2ImageService r2ImageService;
    private final MongoTemplate mongoTemplate;
    private final TmdbService tmdbService;
    private final static Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Value("${api.kmdb.serviceKey}")
    private String serviceKey;

    public VideoDTO readVideo(String id) {
        VideoDocument document = videoRepository.findById(id).orElse(null);
        if (document != null) {
            return document.toDTO();
        }
        return null;
    }

    public void processVideoImages(VideoDocument videoDocument) {
        // posters와 stlls 이미지 URL 변경
        videoDocument.changeAllImgUrlHQ(r2ImageService);
    }

    public List<String> findAllCommCodes() {
        return mongoTemplate.query(VideoDocument.class)
                .distinct("commCode")
                .as(String.class)
                .all();
    }

    public String buildApiUrl() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp?" +
                "collection=kmdb_new2&detail=Y&listCount=300&releaseDts=" + formattedDate +
                "&ServiceKey=" + serviceKey;
    }

    public void searchDataFromApi(String keyword) throws IOException {

        String apiUrl = "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp?" +
                "collection=kmdb_new2&detail=Y&listCount=500&title=" + keyword + "&ServiceKey=" + serviceKey;
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

        // JSON 데이터 바로 처리 (임시 파일 생성 필요 없음)
        List<JsonVideo> videoList = getDataFromJsonContent(jsonResponse);
        saveDataFromList(videoList);
        updateDataFromList(videoList);
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void saveDataFromApi() throws IOException {
        String apiUrl = this.buildApiUrl();
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

        // JSON 데이터 바로 처리
        List<JsonVideo> videoList = getDataFromJsonContent(jsonResponse);

        saveDataFromList(videoList);
        updateDataFromList(videoList);
    }

    public String cleanJsonString(String json) {
        StringBuilder sb = new StringBuilder();
        for (char c : json.toCharArray()) {
            if (c > 31 || c == '\n' || c == '\r' || c == '\t') {
                sb.append(c); // 정상적인 문자만 추가
            }
        }
        return sb.toString();
    }

    public List<JsonVideo> getDataFromJsonContent(String jsonContent) throws IOException {
        String cleanJsonString = cleanJsonString(jsonContent);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonKMDB json1 = objectMapper.readValue(cleanJsonString, JsonKMDB.class);

        return json1.getData().get(0).getResult();
    }

    private void saveDataFromList(List<JsonVideo> data) {
        Set<String> existingCommCodes = new HashSet<>(findAllCommCodes());
        for (JsonVideo video : data) {

            if (video.getPosters() == null || video.getPosters().isEmpty()) {
                continue; // 포스터 내용이 없으면 저장하지 않음
            }

            if (video.getGenre() != null && video.getGenre().contains("에로")) {
                continue; // "에로" 장르가 있으면 저장하지 않음
            }

            VideoDocument document = video.toVideoDocument();

            // commCode가 null이거나 빈 값인 경우 title과 prodYear로 중복 검사
            if (document.getCommCode() == null || document.getCommCode().isEmpty()) {
                // title과 prodYear가 동일한 document가 DB에 이미 있는지 확인
                boolean exists = videoRepository.existsByTitleAndReleaseDate(document.getTitle(), document.getReleaseDate());
                if (exists) {
                    System.out.println("Duplicate title and ReleaseDate detected: " + document.getTitle() + " - " + document.getReleaseDate());
                    continue; // 이미 존재하면 저장하지 않고 넘어감
                }
            } else {
                // commCode가 이미 존재하는지 확인 (DB에서 미리 가져온 commCode들과 비교)
                if (existingCommCodes.contains(document.getCommCode())) {
                    System.out.println("Duplicate comm code detected: " + document.getCommCode());
                    continue;
                }
            }
            this.processVideoImages(document); // commCode가 존재하지 않으면 이미지 링크 파일서버에 저장

            addTmdbIdToVideo(document);
            searchAndAddOttAtTmdb(document);
            updateOttPlatformNames(document);

            videoRepository.save(document); // MongoDB에 저장

            existingCommCodes.add(document.getCommCode()); // commCode를 existingCommCodes에 추가
        }
    }

    private void updateDataFromList(List<JsonVideo> data) {
        // 전체 데이터 크기
        int totalSize = data.size();

        // 진행 상황 출력 (1부터 시작하도록 설정)
        for (int i = 0; i < totalSize; i++) {
            JsonVideo video = data.get(i);
            VideoDocument dc = video.toVideoDocument();

            addTmdbIdToVideo(dc);
            searchAndAddOttAtTmdb(dc);
            Query query = new Query();
            query.addCriteria(Criteria.where("title").is(dc.getTitle()).and("prodYear").is(dc.getProdYear()));
            Update update = new Update();
            update.set("tmdbId", dc.getTmdbId());
            update.set("ottInfo", dc.getOttInfo());
            update.set("commCode", dc.getCommCode());
            update.set("rating", dc.getRating());
            update.set("awards1", dc.getAwards1());
            update.set("awards2", dc.getAwards2());

            mongoTemplate.updateMulti(query, update, VideoDocument.class);

            System.out.println("Processing " + (i + 1) + " of " + totalSize + " (" + (i + 1) * 100 / totalSize + "%)");
        }
    }

    private void addTmdbIdToVideo(VideoDocument video) {
        List<String> candidateYears = Arrays.asList(
                video.getReleaseDate() != null && video.getReleaseDate().length() >= 4 ? video.getReleaseDate().substring(0, 4) : null,
                video.getProdYear()
        );

        List<String> candidateTitles = Arrays.asList(
                video.getTitle(),
                video.getTitleEng(),
                video.getTitleOrg()
        );
        Integer tmdbId = null;
        for (String year : candidateYears) {
            if (year == null) continue;  // 유효하지 않은 연도는 건너뜀
            for (String title : candidateTitles) {
                if (title == null) continue;  // 유효하지 않은 제목은 건너뜀
                tmdbId = tmdbService.getTmdbId(title, year);
                if (tmdbId != null) break;  // ID를 찾으면 바로 중단
            }
            if (tmdbId != null) break;  // ID 찾으면 연도 반복 중단
        }
        video.setTmdbId(tmdbId);  // TMDB ID 저장
    }

    private void searchAndAddOttAtTmdb(VideoDocument video) {
        Integer tmdbId = video.getTmdbId();

        if (tmdbId != null && video.getOttInfo() != null) {
            List<MovieDocument.OTTInfo> ottInfos = tmdbService.getOttInfo(tmdbId);

            if (!ottInfos.isEmpty()) {
                // OTT 정보가 있으면 업데이트
                video.setOttInfo(ottInfos);
            }
        }
    }

    private void updateOttPlatformNames(VideoDocument video) {
        video.getOttInfo().forEach(ott -> {
            if (ott.getPlatform() == null || ott.getPlatform().isEmpty()) {
                ott.setPlatform(detectPlatformFromLink(ott.getLink()));
            }
            ott.setPlatform(convertToKoreanPlatformName(ott.getPlatform()));
            if (ott.getLink() == null) {
                ott.setLink(generateDefaultLink(ott.getPlatform(), video.getTitle()));
            }
        });
    }

    private String detectPlatformFromLink(String link) {
        if (link == null) return "기타";
        if (link.contains("coupang")) return "쿠팡플레이";
        if (link.contains("apple")) return "Apple TV";
        if (link.contains("uplus")) return "U+모바일tv";
        if (link.contains("laftel")) return "라프텔";
        if (link.contains("cinefox")) return "씨네폭스";
        return "기타";
    }

    private String convertToKoreanPlatformName(String platform) {
        return switch (platform) {
            case "wavve" -> "웨이브";
            case "Watcha" -> "왓챠";
            case "Netflix" -> "넷플릭스";
            case "Disney Plus" -> "디즈니+";
            default -> platform; // 기존 값 유지
        };
    }

    private String generateDefaultLink(String platform, String title) {
        return switch (platform) {
            case "웨이브" -> "https://www.wavve.com/search?searchWord=" + title;
            case "왓챠" -> "https://watcha.com/search?query=" + title + "&domain=all";
            case "Apple TV" -> "https://tv.apple.com/search?term=" + title;
            default -> null;
        };
    }


}
