package com.nungil.Service;


import com.nungil.Dto.MovieDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;

@Service
public class KinoService {
    public List<MovieDTO> fetchMoviesByTitle(String title) {
        List<MovieDTO> kinoMovies = new ArrayList<>();
        WebDriver driver = initializeDriver();

        try {
            String searchUrl = "https://m.kinolights.com/search?keyword=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            System.out.println("키노라이츠 검색 URL: " + searchUrl);
            driver.get(searchUrl);

            // 검색 결과 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.content__body")));

            List<WebElement> movieItems = driver.findElements(By.cssSelector("a.content__body"));
            System.out.println("검색된 영화 수: " + movieItems.size());

            for (WebElement item : movieItems) {
                try {
                    // 요소를 처리하기 전에 스크롤하여 가시 영역으로 이동
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", item);
                    processMovieItem(item, driver, kinoMovies);
                } catch (StaleElementReferenceException e) {
                    System.err.println("StaleElementReferenceException 발생, 요소를 건너뜁니다.");
                }
            }
        } catch (Exception e) {
            System.err.println("키노라이츠 크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return kinoMovies;
    }

    private WebDriver initializeDriver() {
        System.out.println("WebDriver 초기화 중...");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        System.out.println("WebDriver 초기화 완료.");
        return driver;
    }

//    private void processMovieItem(WebElement item,  WebDriver driver, List<MovieDTO> kinoMovies) {
//        try {
//            String kinoTitle = getElementText(item, ".metadata__title span");
//            String kinoSubtitle = getElementText(item, ".metadata__subtitle span");
//
//            String kinoYear = extractYearFromSubtitle(kinoSubtitle);
//
//            System.out.println("영화 제목: " + kinoTitle);
////            System.out.println("영화 제목: " + kinoTitle + ", 개봉 연도: " + kinoYear);
//            if (kobisYear.equals(kinoYear)) {
//                String detailPageUrl = item.getAttribute("href");
//                System.out.println("상세 페이지 URL: " + detailPageUrl);
//
//                List<MovieDTO.OTTInfo> ottPlatforms = fetchOTTInfo(detailPageUrl, driver);
//                // OTTInfo 객체 생성 및 설정
//                MovieDTO.OTTInfo ottInfo = new MovieDTO.OTTInfo();
//                if (!ottPlatforms.isEmpty()) {
//                    ottInfo.setOttPlatform(ottPlatforms.get(0).getOttPlatform()); // 첫 번째 플랫폼 이름
//                    ottInfo.setAvailable(ottPlatforms.get(0).getAvailable());     // 첫 번째 플랫폼의 사용 가능 여부 설정
//                } else {
//                    ottInfo.setOttPlatform("N/A");
//                    ottInfo.setAvailable(false);
//                }
//
//                // MovieDTO 객체 생성 및 설정
//                MovieDTO movieDTO = new MovieDTO();
//                movieDTO.setTitle(kinoTitle);
////                movieDTO.setReleaseDate(kinoYear);
//                movieDTO.setOttInfo(fetchOTTInfo(detailPageUrl, driver));
//
//                // MovieDTO 리스트에 추가
//                kinoMovies.add(movieDTO);
//
//            } else {
////                System.out.println("연도가 일치하지 않음: " + kinoYear);
//            }
//        } catch (Exception e) {
//            System.err.println("영화 처리 중 오류 발생: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

private void processMovieItem(WebElement item, WebDriver driver, List<MovieDTO> kinoMovies) {
    try {
        String kinoTitle = getElementText(item, ".metadata__title span");
        String kinoSubtitle = getElementText(item, ".metadata__subtitle span");
        String kinoYear = extractYearFromSubtitle(kinoSubtitle); // 연도 추출

        System.out.println("🎬 영화 제목: " + kinoTitle + ", 개봉 연도: " + kinoYear);

        // 영화 상세 페이지 링크 가져오기
        String detailPageUrl = item.getAttribute("href");
        System.out.println("🔗 상세 페이지 URL: " + detailPageUrl);

        List<MovieDTO.OTTInfo> ottPlatforms = fetchOTTInfo(detailPageUrl, driver);

        // MovieDTO 생성
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setTitle(kinoTitle);
        movieDTO.setReleaseDate(kinoYear);
        movieDTO.setOttInfo(ottPlatforms);

        kinoMovies.add(movieDTO);

    } catch (Exception e) {
        System.err.println("❌ 영화 처리 중 오류 발생: " + e.getMessage());
        e.printStackTrace();
    }
}



    private String extractYearFromSubtitle(String subtitle) {
        if (subtitle == null || subtitle.isEmpty()) {
            System.out.println("자막이 비어 있음.");
            return "";
        }
        String year = "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{4}").matcher(subtitle);
        if (matcher.find()) {
            year = matcher.group();
        }
        System.out.println("추출된 연도: " + year);
        return year;
    }

    private List<MovieDTO.OTTInfo> fetchOTTInfo(String detailPageUrl, WebDriver driver) {
        List<MovieDTO.OTTInfo> ottInfos = new ArrayList<>();
        System.out.println("fetchOTTInfo() 시작. 반환 값 초기 상태: " + ottInfos);

        try {
            driver.get(detailPageUrl);
            System.out.println("OTT 상세 페이지 접근: " + detailPageUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.movie-price-link-wrap a.movie-price-link")));

            List<WebElement> ottItems = driver.findElements(By.cssSelector("div.movie-price-link-wrap a.movie-price-link"));
            System.out.println("OTT 플랫폼 수: " + ottItems.size());

            for (WebElement ottItem : ottItems) {
                String provider = getElementText(ottItem, "div.provider-info__title span.name");
                String link = ottItem.getAttribute("href");

                if (!provider.isEmpty() && link != null) {
                    // ✅ Lombok @AllArgsConstructor 활용
                    MovieDTO.OTTInfo ottInfo = new MovieDTO.OTTInfo(provider, true, link);

                    ottInfos.add(ottInfo); // 리스트에 추가

                    System.out.println("리스트에 추가된 OTTInfo 객체: " + ottInfo);
                }
            }
            System.out.println("최종 ottInfos 리스트 크기: " + ottInfos.size());

        } catch (Exception e) {
            System.err.println("OTT 정보를 찾는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return ottInfos;
    }



    private String getElementText(WebElement element, String cssSelector) {
        try {
            WebElement target = element.findElement(By.cssSelector(cssSelector));
            String text = target != null ? target.getText().trim() : "";
            System.out.println("CSS Selector: " + cssSelector + ", 텍스트: " + text);
            return text;
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            System.out.println("CSS Selector [" + cssSelector + "]에서 요소를 찾을 수 없음.");
            return "";
        }
    }
}