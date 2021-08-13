package UI.DigiohDashboardPages;

import GenericUtilities.GenericUtil;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyticsSubmissionsPage extends BasePage {

    public AnalyticsSubmissionsPage(WebDriver driver){
        super(driver);
        WebDriverWait wait = new WebDriverWait(driver,15);
        wait.until(ExpectedConditions.visibilityOf(emailTable));
    }

    @FindBy(xpath="//form[@id='submissionsForm']/following-sibling::table")
    private WebElement emailTable;

    @FindBy(id="profile-name")
    private WebElement profileNameLink;

    @FindBy(linkText="Logout")
    private WebElement logoutLink;

    GenericUtil genericUtil = new GenericUtil();

    @Step("Check Email:-{0} is appearing correctly on the Analytics page")
    public Boolean isEmailPresentInTheTable(String email){
        List<WebElement> rows = emailTable.findElements(By.xpath("tbody/tr"));
        Boolean emailExists = rows.
                            stream().
                            anyMatch(row -> row.findElement(By.xpath("td[2]")).getText().equals(email));
        return emailExists;
    }

    @Step("Verify other details on the Email row are appearing correctly for the email:-{0}")
    public Boolean verifyDetailsOfEmail(String email){
        List<WebElement> rows = emailTable.findElements(By.xpath("tbody/tr"));
        WebElement rw = rows.
                        stream().
                        filter(row -> row.findElement(By.xpath("td[2]")).getText().equals(email)).
                        collect(Collectors.toList()).get(0);
        String geoLocation = rw.findElement(By.xpath("td[4]")).getText();
        String date = rw.findElement(By.xpath("td[5]")).getText();

        if(date.contains(genericUtil.getTodaysDate()))
            return true;
        else
            return false;

    }

    @Step("Logout from the Digioh Portal")
    public LoginPage logout(){
        profileNameLink.click();
        logoutLink.click();
        return new LoginPage(driver);
    }

}
