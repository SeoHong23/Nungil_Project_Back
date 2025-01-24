package com.nungil.Service;

import com.nungil.Document.MovieDocument;
import com.nungil.Repository.MovieRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
    날짜 : 2025.01.22
    이름 : 박서홍
    내용 : 크롤링 서비스 생성
 */

@Service
public class CrawlerService {

    private final MovieRepository movieRepository;

    public CrawlerService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // 사이트맵 URL 가져오기
    public List<String> getSitemapUrls() throws IOException {
        String url = "https://kinolights.com/sitemap/title-index.xml";

        javax.swing.text.Document doc = Jsoup.connect(url).get();
        Elements locElements = doc.select("loc");

        List<String> sitemapUrls = new ArrayList<>();
        for (org.jsoup.nodes.Element element : locElements) {
            sitemapUrls.add(element.text());
        }

        return sitemapUrls;
    }
}
