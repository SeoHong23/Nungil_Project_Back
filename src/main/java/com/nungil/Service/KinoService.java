package com.nungil.Service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class KinoService {
    public List<String> fetchOttPlatforms(String keyword) {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");

        // Selenium 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 브라우저 창을 띄우지 않음
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver(options);
        List<String> ottList = new ArrayList<>();

        try {
            // 키노라이츠 검색 URL 접속
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = "https://www.kinolights.com/search?keyword=" + encodedKeyword;
            driver.get(url);

            // 로딩 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement firstMovie = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-list a")));

            // 첫 번째 영화의 OTT 플랫폼 정보 가져오기
            List<WebElement> ottElements = firstMovie.findElements(By.cssSelector(".ott-area img"));
            for (WebElement ott : ottElements) {
                String platformName = ott.getAttribute("alt"); // OTT 플랫폼 이름
                ottList.add(platformName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return ottList;
    }
}
