package com.nungil.Controller;

import com.nungil.Dto.MovieDTO;
import com.nungil.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/*
    날짜 : 2025.01.22
    이름 : 박서홍
    내용 : 크롤링 컨트롤러 생성
 */
@RestController
@RequestMapping("/api/movie")
public class CrawlerController {

    @Autowired
    private MovieService movieService;

    /**
     * 제목으로 MongoDB에서 영화 검색 (검색 전용)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String title) {
        Map<String, Object> matchingMovies = movieService.updateOttInfo(title);

        if (matchingMovies.isEmpty()) {
            return ResponseEntity.badRequest().body("🚨 검색 결과가 없습니다.");
        }

        return ResponseEntity.ok(matchingMovies); // 검색된 영화 리스트 반환
    }

    /**
     * ott링크 수동 업데이트
     */
    @PutMapping("/ott")
    public ResponseEntity<String> updateOttInfo(@RequestBody MovieDTO request) {
        boolean updated = movieService.updateOTTLinksByTitle(request.getTitle(), request.getOttInfo(), request.getTheaterLinks());

        if (updated) {
            return ResponseEntity.ok("✅ OTT 정보가 성공적으로 업데이트되었습니다: " + request.getTitle());
        } else {
            return ResponseEntity.badRequest().body("🚨 업데이트 실패: 해당 영화가 MongoDB에 없습니다.");
        }
    }


    /**
     * mongodb에서 없을 시 kobisAPI호출해서 검색
     */
//    @GetMapping("/total")
//    public ResponseEntity<?> searchOrFetchMovie(@RequestParam String title) {
//        MovieDocument movie = movieService.searchOrFetchMovie(title);
//
//        if (movie == null) {
//            return ResponseEntity.badRequest().body("🚨 검색 결과가 없고, KOBIS에서도 데이터를 가져올 수 없습니다.");
//        }
//
//        return ResponseEntity.ok(movie); // 최종 영화 데이터 반환
//    }

}



