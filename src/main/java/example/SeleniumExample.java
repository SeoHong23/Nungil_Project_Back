package example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class SeleniumExample {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        try {
            // 크롤링할 URL로 이동
            driver.get("https://m.kinolights.com");

            // 예: 특정 클래스의 요소 가져오기
            List<WebElement> titles = driver.findElements(By.cssSelector(".title-class")); // 클래스 이름 변경 필요
            for (WebElement title : titles) {
                System.out.println("Title: " + title.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 드라이버 종료
            driver.quit();
        }
    }
}
