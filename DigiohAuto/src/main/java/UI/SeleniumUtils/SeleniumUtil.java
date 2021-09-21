package UI.SeleniumUtils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;

public class SeleniumUtil {

    public static void scrollDownABit(WebDriver driver) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0,250)", "");
    }

    public static void waitForSpecifiedSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000);
    }

    public static String executeJavascript(WebDriver driver, String scriptToExecute) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript(scriptToExecute);
    }

    public static Boolean executeJavascript_Boolean(WebDriver driver, String scriptToExecute) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (Boolean) js.executeScript(scriptToExecute);
    }

    public static Long executeJavascript_Long(WebDriver driver, String scriptToExecute) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (Long) js.executeScript(scriptToExecute);
    }

    public static List<String> executeJavascript_Array(WebDriver driver, String scriptToExecute) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (ArrayList<String>)js.executeScript(scriptToExecute);
    }

}
