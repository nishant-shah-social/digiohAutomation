package UI.DigiohDashboardPages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage extends BasePage {
    public DashboardPage(WebDriver driver){
        super(driver);
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.visibilityOf(profileNameLink));
    }

    @FindBy(id="profile-name")
    private WebElement profileNameLink;

    @FindBy(linkText="Analytics")
    private WebElement analyticsMenu;

    @FindBy(linkText="Submissions")
    private WebElement submissionsMenuOption;

    @Step("Navigate to the Analytics page")
    public AnalyticsSubmissionsPage navigateToAnalyticsPage(){
        analyticsMenu.click();
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.visibilityOf(submissionsMenuOption));
        submissionsMenuOption.click();
        return new AnalyticsSubmissionsPage(driver);
    }

}
