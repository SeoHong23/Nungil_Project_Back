package com.nungil.Controller;

import com.nungil.Service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/*
    날짜 : 2025.01.22
    이름 : 박서홍
    내용 : 크롤링 컨트롤러 생성
 */

@RestController
public class CrawlerController {
    /*

    private final MovieService movieService;

    // 생성자 이름 수정: CrawlerController
    public CrawlerController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies/details")
    public Map<String, Object> getMovieDetailsWithOtt(@RequestParam String movieCd) {
        return movieService.getMovieWithOtt(movieCd); // 메서드 이름 확인 후 수정
    }
    */

}
