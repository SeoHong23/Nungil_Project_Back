package com.nungil.Controller;

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
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    // JSON 파일을 받아 MongoDB에 저장하는 엔드포인트
    @PostMapping("/upload")
    public String uploadVideoData(@RequestParam("file") MultipartFile file) {
        try {

            // 업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("videoData", ".json");
            file.transferTo(tempFile);

            // 업로드된 파일 정보 로그 찍기
            logger.info("업로드된 파일 경로: {}", tempFile.getAbsolutePath());
            logger.info("업로드된 파일 이름: {}", file.getOriginalFilename());
            logger.info("파일 크기: {} bytes", file.getSize());

            // 서비스 호출
            videoService.saveData(tempFile.getAbsolutePath());

            // 처리 후 파일 삭제
            tempFile.delete();

            return "JSON 데이터가 성공적으로 저장되었습니다.";
        } catch (IOException e) {
            logger.error("데이터 저장 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return "데이터 저장 중 오류 발생: " + e.getMessage();
        }
    }

    @PutMapping("/upload")
    public String updateVideoData(@RequestParam("file") MultipartFile file) {
        try {

            // 업로드된 파일을 임시 디렉토리에 저장
            File tempFile = File.createTempFile("videoData", ".json");
            file.transferTo(tempFile);

            // 업로드된 파일 정보 로그 찍기
            logger.info("업로드된 파일 경로: {}", tempFile.getAbsolutePath());
            logger.info("업로드된 파일 이름: {}", file.getOriginalFilename());
            logger.info("파일 크기: {} bytes", file.getSize());

            // 서비스 호출
            videoService.updateData(tempFile.getAbsolutePath());

            // 처리 후 파일 삭제
            tempFile.delete();

            return "JSON 데이터가 성공적으로 저장되었습니다.";
        } catch (IOException e) {
            logger.error("데이터 저장 중 오류 발생: {}", e.getMessage());
            e.printStackTrace();
            return "데이터 저장 중 오류 발생: " + e.getMessage();
        }
    }
}
