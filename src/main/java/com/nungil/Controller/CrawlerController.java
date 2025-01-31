package com.nungil.Controller;

import com.nungil.Service.CrawlerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/*
    날짜 : 2025.01.22
    이름 : 박서홍
    내용 : 크롤링 컨트롤러 생성
 */

@RestController
public class CrawlerController {
    /*

    private final CrawlerService crawlerService;


    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }


    @GetMapping("/api/sitemap-urls")
    public List<String> getMovieUrls() throws IOException {
        return crawlerService.getSitemapUrls();
    }
    @GetMapping("/api/all-movies")
    public List<String> getAllMovies() throws IOException {
        return crawlerService.fetchAllMovie();
    }
    */

}
