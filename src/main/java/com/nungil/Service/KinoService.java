package com.nungil.Service;

import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;


@Service
public class KinoService {

    @Autowired
    private MongoTemplate mongoTemplate; // ✅ MongoDB 접근 객체

    public List<MovieDTO> fetchMoviesByTitle(String title) {
        List<MovieDTO> kinoMovies = new ArrayList<>();
        WebDriver driver = initializeDriver();

        try {
            String searchUrl = "https://m.kinolights.com/search?keyword="  + URLEncoder.encode(title, StandardCharsets.UTF_8);
            System.out.println("키노라이츠 검색 URL: " + searchUrl);
            driver.get(searchUrl);

            // 검색 결과 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            try {
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.content__body")));
            } catch (TimeoutException e) {
                System.err.println("🚨 검색 결과 로딩 시간 초과: " + e.getMessage());
                return kinoMovies; // 검색 결과 없음
            }

            List<WebElement> movieItems = driver.findElements(By.cssSelector("a.content__body"));
            for (WebElement item : movieItems) {
                boolean success = false;
                int retryCount = 0;
                while (!success && retryCount < 3) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", item);
                        processMovieItem(item, driver, kinoMovies);
                        success = true;
                    } catch (StaleElementReferenceException e) {
                        System.err.println("🚨 요소가 업데이트됨, 다시 시도 중... (재시도 횟수: " + retryCount + ")");
                        retryCount++;
                    } catch (TimeoutException e) {
                        System.err.println("🚨 요소 로딩 시간 초과. 다음 영화로 이동합니다.");
                        break; // 현재 영화 건너뛰고 다음 영화 크롤링
                    }
                }
            }
        } finally {
            driver.quit();
        }
        return kinoMovies;
    }


    private WebDriver initializeDriver() {
        System.out.println("WebDriver 초기화 중...");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments( "--disable-gpu", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);
        System.out.println("WebDriver 초기화 완료.");
        return driver;
    }


    private void processMovieItem(WebElement item, WebDriver driver, List<MovieDTO> kinoMovies) {
        try {
            String kinoTitle = getElementText(item, ".metadata__title span");
            String kinoSubtitle = getElementText(item, ".metadata__subtitle span");
            String kinoYear = extractYearFromSubtitle(kinoSubtitle); // 연도 추출

            System.out.println("🎬 영화 제목: " + kinoTitle + ", 개봉 연도: " + kinoYear);

            MovieDTO movieDTO = new MovieDTO();

            MovieDocument matchedMovie = findMovieByTitleAndYear(kinoTitle, kinoYear);
            if (matchedMovie == null) {
                System.out.println("제목과 연도가 일치하는 영화가 없음:" + kinoTitle + "(" + kinoYear + ")");
                return;

            }

            System.out.println("제목, 연도가 일치하는 영화 발견 : " + matchedMovie.getTitle());

            // 영화 상세 페이지 링크 가져오기
            String detailPageUrl = item.getAttribute("href");
            System.out.println("🔗 상세 페이지 URL: " + detailPageUrl);

            List<MovieDTO.OTTInfo> ottPlatforms = fetchOTTInfo(detailPageUrl, driver, movieDTO);

            boolean isInTheater = checkIfInTheater(driver);
            List<String> theaterLinks = isInTheater ? fetchTheaterInfo(driver) : new ArrayList<>();

            // MovieDTO 생성
            movieDTO.setTitle(kinoTitle);
            movieDTO.setReleaseDate(kinoYear);
            movieDTO.setOttInfo(ottPlatforms);
            movieDTO.setInTheater(isInTheater);
            movieDTO.setTheaterLinks(theaterLinks);

            System.out.println("📌 저장된 MovieDTO: " + movieDTO);

            kinoMovies.add(movieDTO);

        } catch (Exception e) {
            System.err.println("❌ 영화 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private MovieDocument findMovieByTitleAndYear(String kinoTitle, String kinoYear) {

        String cleanedKinoTitle = kinoTitle.replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase().trim();
        Criteria criteria = Criteria.where("title").regex(cleanedKinoTitle, "i")
                .and("releaseDate").regex("^" + kinoYear);



        Query query = new Query(criteria);

        MovieDocument movie = mongoTemplate.findOne(query, MovieDocument.class);

        if (movie != null) {
            System.out.println("✅ 일치하는 영화 발견: " + movie.getTitle() + " (" + movie.getReleaseDate() + ")");
        } else {
            System.out.println("❌ 제목과 연도가 일치하는 영화가 없음: " + kinoTitle + " (" + kinoYear + ")");
        }
        return movie;
    }


    private String extractYearFromSubtitle(String subtitle) {
        if (subtitle == null || subtitle.isEmpty()) {
            System.out.println("자막이 비어 있음.");
            return "";
        }

        // "2024년" 같은 형식에서 연도를 추출
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{4})년").matcher(subtitle);
        if (matcher.find()) {
            String year = matcher.group(1); // 첫 번째 그룹 (2024) 가져오기
            System.out.println("추출된 연도: " + year);
            return year;
        }

        System.out.println("연도 추출 실패.");
        return "";
    }


    private List<MovieDTO.OTTInfo> fetchOTTInfo(String detailPageUrl, WebDriver driver, MovieDTO movie) {
        List<MovieDTO.OTTInfo> ottInfos = new ArrayList<>();
        List<String> theaterLinks = new ArrayList<>(); // 🎟️ 영화 예매 링크 저장
        System.out.println("fetchOTTInfo() 시작. 반환 값 초기 상태: " + ottInfos);

        try {
            driver.get(detailPageUrl);
            System.out.println("OTT 상세 페이지 접근: " + detailPageUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // ✅ OTT 정보 크롤링
            List<WebElement> ottItems;
            try {
                ottItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.movie-price-link-wrap a.movie-price-link")));
                System.out.println("OTT 플랫폼 수: " + ottItems.size());

                for (WebElement ottItem : ottItems) {
                    String provider = getElementText(ottItem, "div.provider-info__title span.name");
                    String link = ottItem.getAttribute("href");

                    if (!provider.isEmpty() && link != null) {
                        ottInfos.add(new MovieDTO.OTTInfo(provider, true, link));
                        System.out.println("OTT 추가됨: " + provider + " - " + link);
                    }
                }
            } catch (TimeoutException e) {
                System.out.println("🚨 OTT 정보 없음: 영화관 상영작이거나 아직 서비스되지 않음.");
            }

            // ✅ 영화 예매 정보 크롤링 (현재 상영 중 확인)
            boolean isInTheater = checkIfInTheater(driver);
            if (isInTheater) {
                theaterLinks = fetchTheaterInfo(driver);
                System.out.println("🎟️ 영화관 예매 링크: " + theaterLinks);
            }

            // 🔥 조건에 따라 데이터 저장
            if (!theaterLinks.isEmpty()) {
                movie.setInTheater(true);
                movie.setTheaterLinks(theaterLinks);
            } else if (!ottInfos.isEmpty()) {
                movie.setInTheater(false);
                movie.setOttInfo(ottInfos);
            } else {
                movie.setInTheater(false);
                System.out.println("🚨 OTT 정보와 영화관 예매 정보가 없음.");
            }

        } catch (Exception e) {
            System.err.println("OTT 정보를 찾는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return ottInfos;
    }


    private boolean checkIfInTheater(WebDriver driver) {
        // "movie-current-released" 클래스를 가진 요소가 있는지 확인
        List<WebElement> theaterElements = driver.findElements(By.cssSelector("div.movie-current-released"));

        boolean isInTheater = !theaterElements.isEmpty();
        System.out.println("🎬 현재 상영 여부: " + isInTheater);

        return isInTheater;
    }

    private List<String> fetchTheaterInfo(WebDriver driver) {
        List<String> theaterLinks = new ArrayList<>();

        // "a.theater" 클래스를 가진 요소 목록 가져오기
        List<WebElement> theaterButtons = driver.findElements(By.cssSelector("a.theater"));

        if (theaterButtons.isEmpty()) {
            System.out.println("🎬 영화관 예매 링크 없음.");
            return theaterLinks;
        }

        for (WebElement button : theaterButtons) {
            String link = button.getAttribute("href");
            if (link != null && !link.isBlank()) { // `isBlank()`로 공백 체크
                theaterLinks.add(link);
                System.out.println("🎟️ 예매 링크 추가: " + link);
            }
        }

        return theaterLinks;
    }

    private String getElementText(WebElement element, String cssSelector) {
        try {
            WebElement target = element.findElement(By.cssSelector(cssSelector));
            if (target != null) {
                String text = target.getText().trim();
                System.out.println("CSS Selector: " + cssSelector + ", 텍스트: " + text);
                return text;
            }
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            System.out.println("❌ 요소 찾기 실패: " + cssSelector);
        }
        return "";
    }


}