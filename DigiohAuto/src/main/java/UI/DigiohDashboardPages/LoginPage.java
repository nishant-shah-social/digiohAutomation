package UI.DigiohDashboardPages;

import UI.BoxObjects.BaseBox;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage extends BasePage {
    public LoginPage(WebDriver driver){
        super(driver);
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.visibilityOf(emailEditText));
    }

    @FindBy(id="Email")
    private WebElement emailEditText;

    @FindBy(name="Password")
    private WebElement passwordEditText;

    @FindBy(id="login_button")
    private WebElement submitButton;

    @Step("Login into Digioh Portal")
    public DashboardPage login(String username, String password){
        emailEditText.sendKeys(username);
        passwordEditText.sendKeys(password);
        submitButton.click();
        return new DashboardPage(driver);
    }

}
