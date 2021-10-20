import GenericUtilities.PropertyUtil;
import UI.DigiohDashboardPages.DashboardPage;
import UI.DigiohDashboardPages.LoginPage;
import UI.SeleniumUtils.SeleniumUtil;
import UI.TestAppPages.TestAppHomePage;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class BaseTest {
    protected WebDriver driver;
    protected SeleniumUtil seleniumUtil;
    String testPageBaseUrl;
    String portalBaseUrl;
    String username;
    String password;
    int setupExecutionCount = 0;

    @BeforeClass
    public void setUp() throws Exception {
        String chromeDriverRelativePath = "src/main/resources/chromedriver";
        File chromeDriver = new File(chromeDriverRelativePath);
        System.setProperty("webdriver.chrome.driver",chromeDriver.getAbsolutePath());

        driver = setBrowser(PropertyUtil.getInstance().getValue("execution.browser"));
        testPageBaseUrl = PropertyUtil.getInstance().getValue("testWebsiteBaseUrl");
        portalBaseUrl = PropertyUtil.getInstance().getValue("digiohPortalBaseUrl");
        username = PropertyUtil.getInstance().getValue("digiohPortalUsername");
        password = PropertyUtil.getInstance().getValue("digiohPortalPassword");
        seleniumUtil =  new SeleniumUtil();
        driver.manage().window().maximize();

        if(setupExecutionCount == 0) {
            driver.get(portalBaseUrl);
            LoginPage loginPage = new LoginPage(driver);
            DashboardPage dashboardPage = loginPage.login(username, password);
            dashboardPage.publishBoxes(driver);
            dashboardPage.logoutPage(driver);
        }
        setupExecutionCount++;
    }

    private WebDriver setBrowser(String browserName) throws MalformedURLException {
        if(browserName.equals("chrome")) {
            return new ChromeDriver();
        }
        else if(browserName.equals("firefox")){
            return new FirefoxDriver();
        }
        else {
            return new SafariDriver();
        }
    }

    @AfterMethod
    public void testTearDown(ITestResult result) throws Exception {
        if(ITestResult.FAILURE == result.getStatus()){
            TakesScreenshot screenshot = (TakesScreenshot)driver;
            File screenshotFile = screenshot.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, new File("Screenshots/"+result.getMethod().getMethodName()+".png"));
        }
    }

    @AfterClass
    public void classTearDown() throws Exception {
        driver.quit();
    }
}
