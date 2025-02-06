package com.nungil.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final ApiVideoRepository videoRepository;
    private final S3ImageService s3ImageService;
    private final MongoTemplate mongoTemplate;
    private final static Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Value("${api.kmdb.serviceKey}")
    private String serviceKey;

    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void fetchDataAndSaveDaily() {
        try {
            saveDataFromApi();
            logger.info("Daily fetch and save completed successfully.");
        } catch (Exception e) {
            logger.error("Error during scheduled fetch and save: {}", e.getMessage(), e);
        }
    }

    public VideoDTO readVideo(String id) {
        VideoDocument document = videoRepository.findById(id).orElse(null);
        if (document != null) {
            return document.toDTO();
        }
        return null;
    }

    public void processVideoImages(VideoDocument videoDocument) {
        // posters와 stlls 이미지 URL 변경
        videoDocument.changeAllImgUrlHQ(s3ImageService);
    }

    public List<String> findAllCommCodes() {
        return mongoTemplate.query(VideoDocument.class)
                .distinct("commCode")
                .as(String.class)
                .all(); // 성능 최적화를 위해 페이징을 고려할 수 있음
    }

    public void updateData(String filePath) throws IOException {
        List<JsonVideo> data = loadData(filePath);

        // 전체 데이터 크기
        int totalSize = data.size();

        // 진행 상황 출력 (1부터 시작하도록 설정)
        for (int i = 0; i < totalSize; i++) {
            JsonVideo video = data.get(i);
            VideoDocument dc = video.toVideoDocument();

            Query query = new Query();
            query.addCriteria(Criteria.where("title").is(dc.getTitle()).and("prodYear").is(dc.getProdYear()));

            this.processVideoImages(dc);

            // 수정할 내용 정의
            Update update = new Update();
            update.set("releaseDate", dc.getReleaseDate());
            update.set("stlls", dc.getStlls());
            update.set("posters", dc.getPosters());

            // MongoDB 업데이트
            mongoTemplate.updateMulti(query, update, VideoDocument.class);

            // 진행 상황 출력
            System.out.println("Processing " + (i + 1) + " of " + totalSize + " (" + (i + 1) * 100 / totalSize + "%)");
        }
    }

    public void saveData(String filePath) throws IOException {
        List<JsonVideo> data = loadData(filePath);
        saveDataFromList(data);
    }

    public String buildApiUrl() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp?" +
                "collection=kmdb_new2&detail=Y&listCount=500&releaseDts=" + formattedDate +
                "&ServiceKey=" + serviceKey;
    }

    public void saveDataFromApi() throws IOException {
        String apiUrl = this.buildApiUrl();
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

        // JSON 데이터 바로 처리 (임시 파일 생성 필요 없음)
        saveDataFromJsonContent(jsonResponse);
    }

    public void saveDataFromJsonContent(String jsonContent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonKMDB json1 = objectMapper.readValue(jsonContent, JsonKMDB.class);

        List<JsonVideo> data = json1.getData().get(0).getResult();
        saveDataFromList(data);
    }

    private void saveDataFromList(List<JsonVideo> data) {
        Set<String> existingCommCodes = new HashSet<>(findAllCommCodes());
        for (JsonVideo video : data) {
            if(video.getPosters() == null || video.getPosters().isEmpty()) {continue;}
            VideoDocument document = video.toVideoDocument();
            // commCode가 null이거나 빈 값인 경우 title과 prodYear로 중복 검사
            if (document.getCommCode() == null || document.getCommCode().isEmpty()) {
                // title과 prodYear가 동일한 document가 DB에 이미 있는지 확인
                boolean exists = videoRepository.existsByTitleAndProdYear(document.getTitle(), document.getProdYear());
                if (exists) {
                    System.out.println("Duplicate title and prodYear detected: " + document.getTitle() + " - " + document.getProdYear());
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
            videoRepository.save(document); // MongoDB에 저장

            existingCommCodes.add(document.getCommCode()); // commCode를 existingCommCodes에 추가
        }
    }

    private List<JsonVideo> loadData(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonKMDB json1 = objectMapper.readValue(new File(filePath), JsonKMDB.class);

        // getData가 null이거나 비어있는지 확인
        if (json1.getData() == null || json1.getData().isEmpty()) {
            throw new IllegalArgumentException("Data is empty or null in the JSON file.");
        }

        JsonKMDBData json2 = json1.getData().get(0);
        return json2.getResult();
    }


}
