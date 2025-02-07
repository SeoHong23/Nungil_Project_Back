package com.nungil.Service;

import com.nungil.config.R2Properties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class R2ImageService {
    private final MinioClient minioClient;
    private final R2Properties r2Properties;

    private final static Logger logger = LoggerFactory.getLogger(R2ImageService.class);

    private static final List<String> POSSIBLE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp", ".jfif", ".svg", ".tiff", ".ico"
    );

    public String processImage(String imageUrl, String path) throws Exception {
        String fileName = extractFileNameFromUrl(imageUrl);
        fileName = sanitizeFileName(fileName);

        String bucketName = r2Properties.getBucketName();
        String fileKey = (path != null && !path.isEmpty()) ? path + "/" + fileName : fileName;

        if (isFileExistsInMinio(bucketName, fileKey)) {
            return r2Properties.getPublicUrl() + "/" + fileKey;
        }

        try (InputStream inputStream = findImageInputStream(imageUrl)) {
            byte[] imageBytes = inputStreamToByteArray(inputStream); // InputStream을 바이트 배열로 변환
            return uploadToR2(imageBytes, path, fileName);
        }
    }

    // InputStream을 바이트 배열로 변환 (속도 최적화)
    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; // 8KB 버퍼 사용
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // URL에서 파일명 추출 (예: "DST709314_01" 형태)
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    // 파일명 정리 정규 표현식 사용
    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }
        return originalFileName.replaceAll("^D", "").replaceAll("_01$", "");
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
                    if (!extension.equals(".jpg")) System.out.println("File found: " + fullUrl);
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

    // R2에 파일 업로드
    private String uploadToR2(byte[] imageBytes, String path, String fileName) throws Exception {
        String bucketName = r2Properties.getBucketName();
        String sanitizedFileName = sanitizeFileName(fileName); // 파일명 정리
        String fileKey = (path != null && !path.isEmpty()) ? path + "/" + sanitizedFileName : sanitizedFileName;

        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) { // 바이트 배열을 InputStream으로 변환
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .stream(inputStream, imageBytes.length, -1)
                            .contentType("image/jpeg")
                            .build()
            );
        }
        logger.info("File uploaded successfully: {}", fileKey);

        // 업로드된 파일의 URL 반환
        return r2Properties.getPublicUrl() + "/" + fileKey;
    }

    // R2에서 파일 존재 여부 확인
    private boolean isFileExistsInMinio(String bucketName, String fileKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build()
            );
            return true; // 파일이 존재하면 true 반환
        } catch (MinioException e) {
            return false; // 파일이 존재하지 않으면 false 반환
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage(), e);
            return false;
        }
    }

    // 파일 확장자에 맞는 contentType 설정
    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".svg" -> "image/svg+xml";
            default -> "application/octet-stream";  // 기본적인 binary 타입
        };
    }


}
