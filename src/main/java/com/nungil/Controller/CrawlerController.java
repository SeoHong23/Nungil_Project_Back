package com.nungil.Controller;

import com.nungil.Dto.MovieDto;
import com.nungil.Service.KinoService;
import com.nungil.Service.KobisService;
import com.nungil.Service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
    public Map<String, Object> getMovie(@RequestParam("title") String title, String kobisYear) {
        return movieService.getMovieDetails(title, kobisYear);
    }
}
