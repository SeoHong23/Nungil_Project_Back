package com.nungil.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nungil.Document.VideoDocument;
import com.nungil.Json.JsonKMDB;
import com.nungil.Json.JsonKMDBData;
import com.nungil.Json.JsonVideo;
import com.nungil.Repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {
    private final VideoRepository videoRepository;
    private final S3ImageService s3ImageService;
    private final MongoTemplate mongoTemplate;

    public void processVideoImages(VideoDocument videoDocument) {
        // posters와 stlls 이미지 URL 변경
        videoDocument.changeAllImgUrlHQ(s3ImageService);
    }

    private List<JsonVideo> loadData(String filePath) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        JsonKMDB json1 = objectMapper.readValue(new File(filePath), JsonKMDB.class);

        // 데이터 구조를 출력해서 확인하기
        System.out.println("Loaded JSON data: " + json1.toString());

        // getData가 null이거나 비어있는지 확인
        if (json1.getData() == null || json1.getData().isEmpty()) {
            throw new IllegalArgumentException("Data is empty or null in the JSON file.");
        }

        JsonKMDBData json2 = json1.getData().get(0);
        return json2.getResult();
    }

    public void saveData(String filePath) throws IOException {

        List<JsonVideo> data = loadData(filePath);

        // Json 파일 내 데이터 순회
        data.stream().forEach(video -> {
            // 데이터 가공
            VideoDocument document = video.toVideoDocument();
            this.processVideoImages(document);

            // MongoDB에 저장
            videoRepository.save(document);
        });
    }
    public void updateData(String filePath) throws IOException {
        List<JsonVideo> data = loadData(filePath);
        data.stream().forEach(video -> {
            VideoDocument dc = video.toVideoDocument();
            Query query = new Query();
            query.addCriteria(Criteria.where("title").is(dc.getTitle()).and("releaseDate").is(dc.getReleaseDate()));

            // 수정할 내용 정의
            Update update = new Update();
            update.set("commCode", dc.getCommCode());
            update.set("awards1", dc.getAwards1());
            update.set("awards2", dc.getAwards2());


            mongoTemplate.updateMulti(query, update, VideoDocument.class);
        });
    }

}
