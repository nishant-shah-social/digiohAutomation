package UI.BoxObjects;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EmailBox extends BaseBox {
    public EmailBox(WebDriver driver){
        super(driver);
    }

    @FindBy(xpath="//iframe[@title='Simple Email Sidebar']")
    private WebElement emailBoxiFrame;

    @FindBy(id="form_input_email")
    private WebElement emailEditText;

    @FindBy(xpath="//button[@aria-label='Submit Modal Form']")
    private WebElement goButton;

    @FindBy(xpath="//button[@aria-label='Close Modal']")
    private WebElement closeModal;

    @FindBy(id="thxtext2")
    private WebElement keepAnEyeOnInboxMessage;

    @FindBy(id="thxtext1")
    private WebElement thankYouMessage;

    @FindBy(id="thximage1")
    private WebElement emailIcon;

    @FindBy(id="error_message")
    private WebElement errorMessage;

    @FindBy(id="ep1text1")
    private WebElement specialText1;

    @FindBy(id="text1")
    private WebElement emailBoxMainTitle;

    @Step("Waiting for SubmitEmail Box to appear")
    public void waitForBoxToAppear(int seconds){
        WebDriverWait wait = new WebDriverWait(driver,seconds);
        wait.until(ExpectedConditions.visibilityOf(emailBoxiFrame));
        driver.switchTo().frame(emailBoxiFrame);
    }

    @Step("Enter email address inside the SubmitEmail Box")
    public void enterEmailAddress(String emailAddress){
        emailEditText.sendKeys(emailAddress);
    }

    @Step("Click on Go Button of the SubmitEmail Box")
    public void clickGo(){
        goButton.click();
    }

    @Step("Verify Keep an eye out in your inbox! message appears in the SubmitEmail Box")
    public Boolean verifyKeepAnEyeOutMessageAppearingSuccessfully(){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(keepAnEyeOnInboxMessage));
        if(keepAnEyeOnInboxMessage.getText().equals("Keep an eye out in your inbox!"))
            return true;
        else
            return false;
    }

    @Step("Verify THANK YOU! message appears in the SubmitEmail Box")
    public Boolean verifyThankYouMessageAppearingSuccessfully(){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(thankYouMessage));
        if(thankYouMessage.getText().equals("THANK YOU!"))
            return true;
        else
            return false;
    }

    @Step("Verify Extra Page1 message appears in the SubmitEmail Box")
    public Boolean verifyExtraPage1Successfully(){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(specialText1));
        if(specialText1.getText().equals("Extra Page 1"))
            return true;
        else
            return false;
    }

    @Step("Verify Sign Up For Our Newsletter header appears on the email box main page")
    public Boolean verifySignUpForNewsLetterHeaderAppers(){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(emailBoxMainTitle));
        if(emailBoxMainTitle.getText().equals("SIGN UP FOR OUR NEWSLETTER"))
            return true;
        else
            return false;
    }

    @Step("Verify Email icon appears in the SubmitEmail Box")
    public Boolean verifyEmailIconAppearsSuccessfully(){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(emailIcon));
        return emailIcon.isDisplayed();
    }

    @Step("Verify validation message:-{0} appears in the SubmitEmail Box")
    public Boolean verifyValidationMessageAppearsSuccessfully(String message){
        WebDriverWait wait = new WebDriverWait(driver,5);
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        if(errorMessage.getText().equals(message))
            return true;
        else
            return false;
    }

    @Step("Click on the close button appearing on the top right corner of the SubmitEmail Box")
    public void closeBox(){
        closeModal.click();
        driver.switchTo().defaultContent();
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.invisibilityOf(emailBoxiFrame));
    }

    @Step("Checking SubmitEmail Box is visible or not")
    public Boolean isBoxVisible(){
        return emailBoxiFrame.isDisplayed();
    }
}
