package com.nungil.Controller;

import com.nungil.Dto.VideoDTO;
import com.nungil.Service.MovieService;
import com.nungil.Service.VideoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    private final MovieService movieService;
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    // JSON 파일을 받아 MongoDB에 저장하는 엔드포인트
    @PostMapping("/upload")
    public String uploadVideoData(@RequestParam("file") MultipartFile file) {
        try {

            // 업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("videoData", ".json");
            file.transferTo(tempFile);

            videoService.saveData(tempFile.getAbsolutePath()); // 서비스 호출

            tempFile.delete(); // 처리 후 파일 삭제

            return "JSON 데이터가 성공적으로 저장되었습니다.";
        } catch (IOException e) {
            logger.error("데이터 저장 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return "데이터 저장 중 오류 발생: " + e.getMessage();
        }
    }

    // JSON 파일을 받아 MongoDB를 수정하는 엔드포인트
    @PutMapping("/upload")
    public String updateVideoData(@RequestParam("file") MultipartFile file) {
        try {

            // 업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("videoData", ".json");
            file.transferTo(tempFile);

            videoService.updateData(tempFile.getAbsolutePath()); // 서비스 호출

            tempFile.delete(); // 처리 후 파일 삭제

            return "데이터가 성공적으로 수정되었습니다.";
        } catch (IOException e) {
            logger.error("데이터 수정 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return "데이터 수정 중 오류 발생: " + e.getMessage();
        }
    }

    // JSON 데이터를 외부 API에서 가져와 저장하는 엔드포인트
    @PostMapping("/fetch-and-save")
    public String fetchAndSaveData() {
        try {
            videoService.saveDataFromApi();
            return "API 데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            return "API 데이터 저장 중 오류 발생: " + e.getMessage();
        }
    }

    // Id값을 받아와 해당 id의 MongoDB 데이터를 조회하는 엔드포인트
    @GetMapping
    public VideoDTO fetchVideoData(@RequestParam String id) {
        return videoService.readVideo(id);
    }

}
