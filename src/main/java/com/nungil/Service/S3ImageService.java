package com.nungil.Service;

import com.nungil.config.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;
    private final AwsS3Properties awsS3Properties;
    private final static Logger logger = LoggerFactory.getLogger(S3ImageService.class);

    private final String s3Folder = "images/";

    private static final List<String> POSSIBLE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".jfif",
            ".svg", ".tiff", ".ico", ".heif", ".heic", ".avif"
    );

    public String processImage(String imageUrl) throws Exception {

        String fileName = extractFileNameFromUrl(imageUrl); // 1. 이미지 URL에서 파일명 추출
        File downloadedFile = downloadImageFromUrl(imageUrl, fileName); // 2. 이미지 다운로드
        String uploadedUrl = uploadToS3(downloadedFile, fileName); // 3. 로컬에서 S3로 이미지 업로드

        return uploadedUrl; // 4. 업로드된 이미지의 S3 URL 리턴
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // URL에서 파일명 추출 (예: "DST709314_01.jpg" 형태)
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    // 외부 URL에서 이미지를 다운로드하여 로컬에 저장
    private File downloadImageFromUrl(String baseUrl, String fileName) throws Exception {
        // 외부 URL에서 InputStream을 열어 이미지 다운로드
        InputStream inputStream = findImageInputStream(baseUrl);

        // 로컬에 파일 저장
        String tempDirectory = System.getProperty("java.io.tmpdir"); // 시스템의 임시 디렉토리 경로
        File tempDir = new File(tempDirectory); // temp 디렉토리 객체
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 디렉토리가 없으면 생성
        }

        File localFile = new File(tempDir, fileName); // 임시 디렉토리에 파일 생성
        try (FileOutputStream fos = new FileOutputStream(localFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        return localFile;
    }

    // 확장자를 변경하며 InputStream 찾기
    private InputStream findImageInputStream(String baseUrl) throws Exception {
        for (String extension : POSSIBLE_EXTENSIONS) {
            String fullUrl = baseUrl + extension;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();

                // HTTP 200 OK일 경우 InputStream 반환
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    if(!extension.equals(".jpg")) System.out.println("File found: " + fullUrl);
                    return connection.getInputStream();
                } else {
                    System.out.println("File not found at: " + fullUrl);
                }
            } catch (Exception e) {
                System.out.println("Error accessing: " + fullUrl + " - " + e.getMessage());
            }
        }

        // 모든 확장자 시도 후 실패하면 예외 던짐
        throw new FileNotFoundException("File not found for any extensions at base URL: " + baseUrl);
    }

    // S3에 파일 업로드
    private String uploadToS3(File file, String fileName) {
        String bucketName = awsS3Properties.getBucketName();
        String region = awsS3Properties.getRegion();
        String fileKey = s3Folder + fileName;  // S3의 경로 + 파일명

        // 1. S3에서 해당 파일이 이미 존재하는지 확인
        if (isFileExistsInS3(bucketName, fileKey)) {
            return buildS3Url(bucketName, region, fileKey);  // 기존 URL 반환
        }

        // 2. 존재하지 않으면 업로드 진행
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType("image/jpeg")
                .build();

        s3Client.putObject(putObjectRequest, file.toPath());

        logger.info("File uploaded successfully: {}", fileKey);

        // 3. 업로드된 이미지의 URL을 리턴
        return buildS3Url(bucketName, region, fileKey);
    }

    // S3에서 파일 존재 여부 확인
    private boolean isFileExistsInS3(String bucketName, String fileKey) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(fileKey).build());
            return true;  // 파일이 존재하면 true 반환
        } catch (NoSuchKeyException e) {
            return false;  // 파일이 존재하지 않으면 false 반환
        }
    }

    // S3 URL 생성 헬퍼 메서드
    private String buildS3Url(String bucketName, String region, String fileKey) {
        return String.format("https://s3.%s.amazonaws.com/%s/%s", region, bucketName, fileKey);
    }

}
