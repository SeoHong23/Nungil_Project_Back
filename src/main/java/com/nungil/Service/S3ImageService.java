package com.nungil.Service;

import com.nungil.Config.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
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

    private final String s3Folder = "images/";

    private static final List<String> POSSIBLE_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp"
    );

    public String processImage(String imageUrl) throws Exception {

        String fileName = extractFileNameFromUrl(imageUrl); // 1. 이미지 URL에서 파일명 추출
        File downloadedFile = downloadImageFromUrl(imageUrl, fileName); // 2. S3에서 이미지 다운로드
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
                    System.out.println("File found: " + fullUrl);
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

        // S3에 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Folder + fileName)  // S3의 경로 + 파일명
                .contentType("image/jpeg") // 이미지 타입 설정 (필요시 변경)
                .build();

        s3Client.putObject(putObjectRequest, file.toPath());

        // 업로드된 이미지의 URL을 리턴
        return String.format("https://%s.s3.%s.amazonaws.com/%s%s",
                bucketName, region, s3Folder, fileName);
    }

}
