package com.nungil.Service;

import com.nungil.Document.MovieDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

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

    public List<String> getSitemapUrls() throws IOException {
        String url = "https://kinolights.com/sitemap/title-index.xml";

        Document doc = Jsoup.connect(url).get();

        Elements locElements = doc.select("loc");

        List<String> sitemapUrls = new ArrayList<>();
        for (org.jsoup.nodes.Element element : locElements) {
            sitemapUrls.add(element.text());

        }

        return sitemapUrls;
    }

    public List<String> fetchAllMovie() throws IOException {
        List<String> allMovieUrls = new ArrayList<>();
        List<String> sitemapUrls = getSitemapUrls();

        for (String sitemapUrl : sitemapUrls) {
            try {
                // Sitemap 파일 처리
                Document doc = Jsoup.connect(sitemapUrl).get();
                Elements locElements = doc.select("loc");

                for (org.jsoup.nodes.Element element : locElements) {
                    allMovieUrls.add(element.text());
                }

                // 서버 과부하 방지를 위한 딜레이
                Thread.sleep(1000); // 1초 대기
            } catch (Exception e) {
                System.err.println("Failed to process Sitemap: " + sitemapUrl);
                e.printStackTrace();
            }
        }
        return new ArrayList<>(allMovieUrls);
    }
}
