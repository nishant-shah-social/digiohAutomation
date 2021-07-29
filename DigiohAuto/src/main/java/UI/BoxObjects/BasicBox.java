package UI.BoxObjects;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class BasicBox extends BaseBox {
    public BasicBox(WebDriver driver){
        super(driver);
    }

    @FindBy(xpath="//iframe[@title='Blank Lightbox']")
    private WebElement basicBoxiFrame;

    @FindBy(id="text1")
    private WebElement textInsideBox;

    @FindBy(xpath="//button[@aria-label='Close Modal']")
    private WebElement closeModal;

    @Step("Waiting for Basic Box to appear")
    public void waitForBoxToAppear(int seconds){
        WebDriverWait wait = new WebDriverWait(driver,seconds);
        wait.until(ExpectedConditions.visibilityOf(basicBoxiFrame));
        driver.switchTo().frame(basicBoxiFrame);
    }

    @Step("Fetching the text appearing inside the Basic Box")
    public String getTextInsideBox(){
       waitForBoxToAppear(20);
       return textInsideBox.getText();
    }

    @Step("Clicking on the close icon at the top right corner of the Basic Box")
    public void closeBox(){
        closeModal.click();
        driver.switchTo().defaultContent();
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.invisibilityOf(basicBoxiFrame));
    }

    @Step("Checking Box is visible or not")
    public Boolean isBoxVisible(){
        return basicBoxiFrame.isDisplayed();
    }
}
