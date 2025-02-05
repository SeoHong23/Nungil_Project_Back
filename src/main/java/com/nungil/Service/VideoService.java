package com.nungil.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.Document.VideoDocument;
import com.nungil.Json.*;
import com.nungil.Repository.ApiVideoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
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


    public void processVideoImages(VideoDocument videoDocument) {
        // posters와 stlls 이미지 URL 변경
        videoDocument.changeAllImgUrlHQ(s3ImageService);
    }

    private MultipartFile convertToMultipartFile(String jsonContent, String filename) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        return new MockMultipartFile(filename, filename, "application/json", inputStream);
    }

    private List<JsonVideo> loadData(String filePath) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        JsonKMDB json1 = objectMapper.readValue(new File(filePath), JsonKMDB.class);

        // getData가 null이거나 비어있는지 확인
        if (json1.getData() == null || json1.getData().isEmpty()) {
            throw new IllegalArgumentException("Data is empty or null in the JSON file.");
        }

        JsonKMDBData json2 = json1.getData().get(0);
        return json2.getResult();
    }

    public void saveData(String filePath) throws IOException {

        List<JsonVideo> data = loadData(filePath);

        // 먼저 DB에서 모든 commCode를 가져옴
        Set<String> existingCommCodes = new HashSet<>(findAllCommCodes());

        // Json 파일 내 데이터 순회
        for (JsonVideo video : data) {

            VideoDocument document = video.toVideoDocument(); // 데이터 가공

            // commCode가 이미 존재하는지 확인 (DB에서 미리 가져온 commCode들과 비교)
            if (existingCommCodes.contains(document.getCommCode())) {
                continue; // commCode가 존재하면 저장하지 않고 건너뜀
            }

            this.processVideoImages(document); // commCode가 존재하지 않으면 이미지 링크 파일서버에 저장

            videoRepository.save(document); // MongoDB에 저장
            existingCommCodes.add(document.getCommCode()); // 저장 후 commCode를 existingCommCodes에 추가
        }
    }
    public List<String> findAllCommCodes() {
        return mongoTemplate.query(VideoDocument.class)
                .distinct("commCode")
                .as(String.class)
                .all(); // 성능 최적화를 위해 페이징을 고려할 수 있음
    }

    public void updateData(String filePath) throws IOException {
        List<JsonVideo> data = loadData(filePath);
        data.stream().forEach(video -> {
            VideoDocument dc = video.toVideoDocument();
            Query query = new Query();
            query.addCriteria(Criteria.where("title").is(dc.getTitle()).and("releaseDate").is(dc.getReleaseDate()));

            // 수정할 내용 정의
            Update update = new Update();
            update.set("nation", dc.getNation());
            update.set("plots", dc.getPlots());
            update.set("rating", dc.getRating());
            update.set("genre", dc.getGenre());

            mongoTemplate.updateMulti(query, update, VideoDocument.class);
        });
    }

    public void saveDataFromApi() throws IOException {
        String apiUrl = this.buildApiUrl();
        // 외부 API에서 데이터 가져오기
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

        // JSON 데이터를 MultipartFile로 변환
        MultipartFile jsonFile = convertToMultipartFile(jsonResponse, "fetchedData.json");

        // 기존 saveData 로직에 전달
        saveDataFromMultipartFile(jsonFile);
    }

    public void saveDataFromMultipartFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("tempVideoData", ".json");
        file.transferTo(tempFile);
        saveData(tempFile.getAbsolutePath());
        tempFile.delete();
    }

    public String buildApiUrl() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "http://api.koreafilm.or.kr/openapi-data2/wisenut/search_api/search_json2.jsp?" +
                "collection=kmdb_new2&detail=Y&listCount=500&releaseDts=" + formattedDate +
                "&ServiceKey=" + serviceKey;
    }

}
