package UI.DigiohDashboardPages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @FindBy(xpath="//table/tbody")
    private WebElement digiohBoxTable;

    @FindBy(css = ".popover-content")
    private WebElement accountGuidPopover;

    @FindBy(xpath = "//a[contains(text(),'Nishant Shah')]")
    private WebElement username;

    @FindBy(xpath = "//a[contains(text(),'Logout')]")
    private WebElement logout;

    @Step("Navigate to the Analytics page")
    public AnalyticsSubmissionsPage navigateToAnalyticsPage(){
        analyticsMenu.click();
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.visibilityOf(submissionsMenuOption));
        submissionsMenuOption.click();
        return new AnalyticsSubmissionsPage(driver);
    }

    @Step("Returns the account Ids of a specific digiohBox")
    public List<String> fetchAccountIds(String digiohBoxName){
        List<WebElement> tempRows = digiohBoxTable.findElements(By.xpath("./tr"));
        int len = tempRows.size();

        List<WebElement> rows = digiohBoxTable.findElements(By.xpath("./tr")).stream().filter(tr ->
            tr.findElement(By.xpath("./td[2]")).getText().equals("Blank Lightbox")).
                collect(Collectors.toList());
        List<String> ids = new ArrayList<>();
        WebElement accountIdLink = rows.get(0).findElement(By.xpath("./td/a"));
        String accountId = accountIdLink.getText();
        accountIdLink.click();
        String accountGuid = accountGuidPopover.getText();
        ids.add(accountId);
        ids.add(accountGuid);
        return ids;
    }

    @Step("Logout from the portal")
    public LoginPage logoutPage(WebDriver driver){
        username.click();
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
        return new LoginPage(driver);
    }

}
