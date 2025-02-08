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
            for (WebElement item : movieItems) {

                boolean success = false;
                int retryCount = 0;

                while (!success && retryCount < 3) {
                    try {

                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", item);
                        processMovieItem(item, driver, kinoMovies);

                        success = true;

                    } catch (StaleElementReferenceException e) {
                        System.err.println("🚨 요소가 업데이트되거나 사라졌습니다. 다시 시도합니다. (재시도 횟수: " + retryCount + ")");
                        retryCount++;
                    } catch (TimeoutException e) {
                        System.err.println("🚨 요소 로딩 시간 초과. 다시 시도합니다. (재시도 횟수: " + retryCount + ")");
                        retryCount++;
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
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
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


            // 영화 상세 페이지 링크 가져오기
            String detailPageUrl = item.getAttribute("href");
            System.out.println("🔗 상세 페이지 URL: " + detailPageUrl);

            List<MovieDTO.OTTInfo> ottPlatforms = fetchOTTInfo(detailPageUrl, driver, movieDTO);

            // MovieDTO 생성
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

    private List<MovieDTO.OTTInfo> fetchOTTInfo(String detailPageUrl, WebDriver driver, MovieDTO movie) {
        List<MovieDTO.OTTInfo> ottInfos = new ArrayList<>();
        System.out.println("fetchOTTInfo() 시작. 반환 값 초기 상태: " + ottInfos);

        try {
            driver.get(detailPageUrl);
            System.out.println("OTT 상세 페이지 접근: " + detailPageUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            List<WebElement> ottItems;
            try {
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.movie-price-link-wrap a.movie-price-link")));
                ottItems = driver.findElements(By.cssSelector("div.movie-price-link-wrap a.movie-price-link"));
            } catch (TimeoutException e) {
                System.out.println("🚨 OTT 정보 없음: 영화관 상영작이거나 아직 서비스되지 않음.");
                return Collections.emptyList();
            }

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

            if (ottInfos.isEmpty()) {
                boolean isInTheater = checkIfInTheater(driver);
                movie.setInTheater(isInTheater);  // DTO에 저장
                System.out.println("🎬 영화관 상영 여부: " + isInTheater);

                if (isInTheater) {
                    List<String> theaterLinks = fetchTheaterInfo(driver);
                    movie.setTheaterLinks(theaterLinks);
                    System.out.println("🎟️ 영화관 예매 링크: " + theaterLinks);
                } else {
                    System.out.println("🚨 영화관 예매 링크를 찾을 수 없습니다.");
                }

            } else {
                System.out.println("🚨 영화관 상영 정보를 확인할 수 없습니다.");

            }

        } catch (Exception e) {
            System.err.println("OTT 정보를 찾는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return ottInfos;
    }

    private boolean checkIfInTheater(WebDriver driver) {
        try {
            // 영화관 상영 여부 확인
            WebElement theaterTag = driver.findElement(By.cssSelector("h2.title"));
            if (theaterTag != null && theaterTag.getText().contains("영화관 상영중")) {
                return true; // 영화관 상영중이면 true
            }
        } catch (NoSuchElementException e) {
            System.out.println("🎬 영화관 상영 정보 없음.");
        }
        return false; // 아니면 false
    }

    private List<String> fetchTheaterInfo(WebDriver driver) {
        List<String> theaterLinks = new ArrayList<>();
        try {
            // 영화관 예매 링크 가져오기
            List<WebElement> theaterButtons = driver.findElements(By.cssSelector("a.theater"));

            for (WebElement button : theaterButtons) {
                String link = button.getAttribute("href");
                if (link != null && !link.isEmpty()) {
                    theaterLinks.add(link);
                    System.out.println("🎟️ 예매 링크 추가: " + link);
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("🎬 영화관 예매 링크 없음.");
        }
        return theaterLinks;
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