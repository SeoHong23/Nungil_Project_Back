package com.nungil.Controller;

import com.nungil.Dto.MovieDTO;
import com.nungil.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/*
    날짜 : 2025.01.22
    이름 : 박서홍
    내용 : 크롤링 컨트롤러 생성
 */
@RestController
public class CrawlerController {

    @Autowired
    private MovieService movieService;

    /**
     * 영화 제목을 검색하여 KOBIS 및 키노라이츠 정보를 반환
     *
     * @param title 사용자 입력 영화 제목
     * @return 영화 정보와 OTT 링크
     */
    @GetMapping("/api/movie")
    public ResponseEntity<?> getMovie(@RequestParam String title, @RequestParam String kobisYear) {
        Map<String, Object> movieDetails = movieService.getMovieDetails(title, kobisYear);
        System.out.println("📢 최종 JSON 응답: " + movieDetails);
        return ResponseEntity.ok(movieDetails);
    }

    @PutMapping("/{title}/ott-info")
    public ResponseEntity<String> updateOttInfo(
            @PathVariable String title,
            @RequestBody List<MovieDTO.OTTInfo> ottInfoList
    ) {
        // OTT 정보 업데이트 로직
        System.out.println("Title: " + title);
        ottInfoList.forEach(ott -> {
            System.out.println("Platform: " + ott.getOttPlatform());
            System.out.println("Available: " + ott.getAvailable());
            System.out.println("Link: " + ott.getLink());
        });
        return ResponseEntity.ok("OTT information updated successfully.");
    }
}
