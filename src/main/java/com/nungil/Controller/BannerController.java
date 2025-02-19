package com.nungil.Controller;

import com.nungil.Dto.BannerDTO;
import com.nungil.Service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {
    private static final String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads").toString() + "/";


    @Value("${file.upload-dir}")
    private String uploadDir;

    private final String bannerDir = uploadDir + "banner/";

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    public ResponseEntity<List<BannerDTO>> list() {
        List<BannerDTO> banners = bannerService.getAllBanner();
        return ResponseEntity.ok(banners);
    }

    @PostMapping("/insert")
    public ResponseEntity<Boolean> uploadBanner (
            @RequestParam("title") String title,
            @RequestPart(value = "image") MultipartFile image) {
        try {
// 업로드 폴더가 없으면 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 1. 이미지 파일이 있을 경우 저장
            if (image != null && !image.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                String filePath = bannerDir + fileName;
                File uploadFile = new File(filePath);

                // 파일 저장
                image.transferTo(uploadFile);
                System.out.println("✅ 이미지 저장 완료: " + uploadFile.getAbsolutePath());

                bannerService.insertBanner(title, fileName);


            } else {
                System.out.println("⚠️ 이미지 없이 배너 저장");
            }

            // 2. 제목 저장 (데이터베이스 저장 로직 추가 가능)
            System.out.println("📌 저장할 배너 제목: " + title);

            return ResponseEntity.ok(true); // 성공 응답
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/bannerInfo")
    public ResponseEntity<BannerDTO> getBanner(@RequestParam String id){
        BannerDTO result = bannerService.getBannerById(Integer.parseInt(id));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws MalformedURLException {
        Path filePath = Paths.get(bannerDir).resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG) // PNG로 가정 (JPG 등 다른 확장자도 가능)
                .body(resource);
    }

    @DeleteMapping("/delete")
    public ResponseEntity deleteBanner(@RequestParam String id) {
        try{
            bannerService.deleteBanner(Integer.parseInt(id));

            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
