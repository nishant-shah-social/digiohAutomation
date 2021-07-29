import GenericUtilities.CsvUtil;
import GenericUtilities.GenericUtil;
import GenericUtilities.UrlPaths;
import UI.BoxObjects.BasicBox;
import UI.BoxObjects.EmailBox;
import UI.DigiohDashboardPages.AnalyticsSubmissionsPage;
import UI.DigiohDashboardPages.DashboardPage;
import UI.DigiohDashboardPages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTests extends BaseTest {

    @Test(dataProvider = "DataProviderIterator", dataProviderClass = CsvUtil.class,
            description = "Verifying basic digioh box appears successfully under different conditions controlled by the url path")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifying basic digioh box appears successfully under different conditions controlled by the url path")
    public void basicBoxTest(String urlPath) throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+urlPath);
        String textInsideBox = basicBox.getTextInsideBox();
        basicBox.closeBox();
        Boolean isBoxVisible = basicBox.isBoxVisible();

        Assert.assertTrue(isBoxVisible.equals(false),"Basic Box is not disappearing after clicking close button");
    }

    @Test(description = "Verifying email form submission works successfully and the email gets captured in the digioh portal")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifying email form submission works successfully and the email gets captured in the digioh portal")
    public void emailFormSubmissionSuccessful() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);
        emailBox.enterEmailAddress(email);
        emailBox.clickGo();

        Assert.assertTrue(emailBox.verifyKeepAnEyeOutMessageAppearingSuccessfully(),
                "Message:- Keep an eye out in your inbox! not appearing");
        Assert.assertTrue(emailBox.verifyThankYouMessageAppearingSuccessfully(),
                "Message:- THANK YOU! not appearing");
        Assert.assertTrue(emailBox.verifyEmailIconAppearsSuccessfully(),"Email icon not appearing");

        driver.get(portalBaseUrl);
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = loginPage.login(username,password);
        AnalyticsSubmissionsPage analyticsSubmissionsPage = dashboardPage.navigateToAnalyticsPage();

        Assert.assertTrue(analyticsSubmissionsPage.isEmailPresentInTheTable(email), "Email:- "+email+" not appearing in Analytics page");
        Assert.assertTrue(analyticsSubmissionsPage.verifyDetailsOfEmail(email),"Details of the email are not appearing correctly.");
    }

    @Test(description = "Verifying proper validation message is displayed when invalid email is entered in the Email box")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifying proper validation message is displayed when invalid email is entered in the Email box")
    public void emailFormSubmissionInvalidEmail() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp();

        emailBox.waitForBoxToAppear(15);
        emailBox.enterEmailAddress(email);
        emailBox.clickGo();

        Assert.assertTrue(emailBox.verifyValidationMessageAppearsSuccessfully("Your Email is in an invalid format."),
                "Message:- Your Email is in an invalid format. not appearing");
    }

    @Test(description = "Verifying proper validation message is displayed when blank email is entered in the Email box")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifying proper validation message is displayed when blank email is entered in the Email box")
    public void emailFormSubmissionBlankEmail() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);
        emailBox.clickGo();

        Assert.assertTrue(emailBox.verifyValidationMessageAppearsSuccessfully("Your Email is required."),
                "Message:- Your Email is required. not appearing");
    }

    @Test(description = "Verifying email box closes successfully on clicking the close button")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifying email box closes successfully on clicking the close button")
    public void emailFormSubmissionCloseModal() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);

        emailBox.waitForBoxToAppear(15);
        emailBox.closeBox();
        Boolean isBoxVisible = emailBox.isBoxVisible();

        Assert.assertTrue(isBoxVisible.equals(false),"Email Box is not disappearing after clicking close button");
    }
}
