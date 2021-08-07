package UI.SeleniumUtils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class SeleniumUtil {

    public static void scrollDownABit(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0,250)", "");
    }

    public static void waitForSpecifiedSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

}
