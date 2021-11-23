import GenericUtilities.CsvUtil;
import GenericUtilities.GenericUtil;
import GenericUtilities.UrlPaths;
import UI.BoxObjects.BasicBox;
import UI.BoxObjects.EmailBox;
import UI.DigiohDashboardPages.AnalyticsSubmissionsPage;
import UI.DigiohDashboardPages.DashboardPage;
import UI.DigiohDashboardPages.LoginPage;
import UI.SeleniumUtils.SeleniumUtil;
import UI.TestAppPages.TestAppHomePage;
import io.ipgeolocation.api.Geolocation;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.apache.tika.utils.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class SmokeTests extends BaseTest {

    @Test(dataProvider = "DataProviderIterator", dataProviderClass = CsvUtil.class,
            description = "Verifying basic digioh box appears successfully under different conditions controlled by the url path")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifying basic digioh box appears successfully under different conditions controlled by the url path")
    public void basicBoxTest(String urlPath) throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        TestAppHomePage testAppHomePage = new TestAppHomePage(driver);

        if(urlPath.contains("HTTP_PROTOCOL")){
            testPageBaseUrl = testPageBaseUrl.replace("https","http");
        }

        driver.get(testPageBaseUrl + urlPath);

        //if tag version contains speed then we need
        //to perform some action for the digioh box to appear
        if(urlPath.contains("tv=speed")){
            SeleniumUtil.waitForSpecifiedSeconds(5);
            Assert.assertFalse(testAppHomePage.basicBoxShownOnThePage(),"Basic box is appearing without any action been performed");
            SeleniumUtil.scrollDownABit(driver);
        }

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

        dashboardPage.logoutPage(driver);

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

    @Test(description = "Verify Digioh API cookie functions work correctly")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the get, set and delete cookie functions of Digioh API operates correctly")
    public void digohAPICookieFunctions() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?HOST_EQUALS");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsCreateCookie = "return DIGIOH_API.setCookie('automation_testing_cookie', 'AmazingCookie', {expirationDays:30});";
        SeleniumUtil.executeJavascript(driver, jsCreateCookie);

        String jsGetCookie = "return DIGIOH_API.getCookie('automation_testing_cookie');";
        String createdCookie = SeleniumUtil.executeJavascript(driver, jsGetCookie);
        Assert.assertEquals(createdCookie,"AmazingCookie","Cookie value does not match. Expected:- AmazingCookie but Actual:- "+createdCookie);

        Cookie automationTestingCookie = driver.manage().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("automation_testing_cookie"))
                .findFirst()
                .orElse(null);

        Date expiry = automationTestingCookie.getExpiry();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String cookieExpiryDate = formatter.format(expiry);
        String cookieExpectedExpiryDate = GenericUtil.getFutureDate(30);
        Assert.assertEquals(cookieExpiryDate,cookieExpectedExpiryDate,"The expiration date of cookie is not as expected");

        String jsDeleteCookie = "return DIGIOH_API.deleteCookie('automation_testing_cookie');";
        SeleniumUtil.executeJavascript(driver, jsDeleteCookie);

        Assert.assertNull(SeleniumUtil.executeJavascript(driver, jsGetCookie));
    }

    @Test(description = "Verify api.getUrlParameter(key)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getUrlParameter(key) returns correct value")
    public void digiohAPIGetUrlParameter() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetUrlParam = "return DIGIOH_API.getUrlParameter('tv');";
        String paramValue = SeleniumUtil.executeJavascript(driver, jsGetUrlParam);

        Assert.assertEquals(paramValue,"inline","Prameter value does not match. Expected:- inline but Actual:- "+paramValue);
    }

    @Test(description = "Verify api.hideUrlParameter(key)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.hideUrlParameter(key) hides the param")
    public void digiohAPIHideUrlParameter() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsHideUrlParam = "return DIGIOH_API.hideUrlParameter('tv');";
        String paramValue = SeleniumUtil.executeJavascript(driver, jsHideUrlParam);

        String url = driver.getCurrentUrl();
        Assert.assertFalse(url.contains("tv=inline"), "The Param:- tv=inline is not getting hidden");
    }

    @Test(description = "Verify api.getCurrentUrl()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getCurrentUrl() fetches the current url correctly")
    public void digiohAPIgetCurrentUrl() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetCurrentUrl = "return DIGIOH_API.getCurrentUrl();";
        String currentUrl = SeleniumUtil.executeJavascript(driver, jsGetCurrentUrl);

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.equals(currentUrl),
                "Incorrect url fetched by getCurrentUrl() method. Expected was:- "+ url + " but found to be:- "+ currentUrl);
    }

    @Test(description = "Verify api.getHostname()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getHostname() fetches the hostname correctly")
    public void digiohAPIgetHostName() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetHostName = "return DIGIOH_API.getHostname();";
        String hostName = SeleniumUtil.executeJavascript(driver, jsGetHostName);

        Assert.assertTrue(hostName.equals("digiohautotest.wpengine.com"),
                "Incorrect Domain name fetched by getHostname(). Expected:- digiohautotest.wpengine.com, Actual:- "+hostName );
    }

    @Test(description = "Verify api.getPagePath()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getPagePath() fetches the path correctly")
    public void digiohAPIgetPagePath() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetPagePath = "return DIGIOH_API.getPagePath();";
        String path = SeleniumUtil.executeJavascript(driver, jsGetPagePath);

        Assert.assertTrue(path.equals("/"),
                "Incorrect Domain name fetched by getPagePath(). Expected:- /, Actual:- "+path );
    }

    @Test(description = "Verify api.getQueryString()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getQueryString() fetches the path correctly")
    public void digiohAPIgetQueryString() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetQueryString = "return DIGIOH_API.getQueryString();";
        String queryString = SeleniumUtil.executeJavascript(driver, jsGetQueryString);

        Assert.assertTrue(queryString.equals("?CURRENT_PAGE_URL_CONTAINS&tv=inline"),
                "Incorrect Domain name fetched by getQueryString(). Expected:- /, Actual:- "+queryString );
    }

    @Test(description = "Verify api.getUrlHash()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getUrlHash() fetches the url hash correctly")
    public void digiohAPIgetUrlHash() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/#test");
        Thread.sleep(3000);
        String jsGetUrlHash = "return DIGIOH_API.getUrlHash();";
        String urlHash = SeleniumUtil.executeJavascript(driver, jsGetUrlHash);

        Assert.assertTrue(urlHash.equals("#test"),
                "Incorrect Domain name fetched by getUrlHash(). Expected:- #test, Actual:- "+urlHash );
    }

    @Test(description = "Verify api.getLandingPageUrl()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getLandingPageUrl() fetches the url hash correctly")
    public void digiohAPIgetLandingPageUrl() throws Exception {
        driver.quit();
        setUp();

        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");
        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();
        String expectedUrl = driver.getCurrentUrl();

        driver.findElement(By.linkText("Test Automation")).click();
        Thread.sleep(2000);
        String jsGetLandingPageUrl = "return DIGIOH_API.getLandingPageUrl();";
        String landingPageUrl = SeleniumUtil.executeJavascript(driver, jsGetLandingPageUrl);

        Assert.assertTrue(landingPageUrl.equals(expectedUrl),
                "Incorrect Domain name fetched by getLandingPageUrl(). Expected:- "+expectedUrl+", Actual:- "+landingPageUrl );
    }

    @Test(description = "Verify api.getReferrer()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getReferrer() fetches the referrer correctly")
    public void digiohAPIgetReferrer() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        driver.findElement(By.linkText("Test Automation")).click();
        Thread.sleep(2000);
        String jsGetReferrer = "return DIGIOH_API.getReferrer();";
        String referrer = SeleniumUtil.executeJavascript(driver, jsGetReferrer);

        Assert.assertTrue(referrer.equals("https://digiohautotest.wpengine.com/?CURRENT_PAGE_URL_CONTAINS&tv=inline"),
                "Incorrect referrer name fetched by getReferrer(). Expected:- https://digiohautotest.wpengine.com/?CURRENT_PAGE_URL_CONTAINS&tv=inline, Actual:- "+referrer );
    }

    @Test(description = "Verify api.getLandingPageReferrer()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getLandingPageReferrer() fetches the referrer correctly")
    public void digiohAPIgetLandingPageReferrer() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        driver.findElement(By.linkText("Test Automation")).click();
        Thread.sleep(2000);
        String jsGetLandingReferrer = "return DIGIOH_API.getLandingPageReferrer();";
        String landingReferrer = SeleniumUtil.executeJavascript(driver, jsGetLandingReferrer);

        Assert.assertTrue(landingReferrer.equals(""),
                "Incorrect landing page referrer name fetched by getLandingPageReferrer(). Expected:- \"\", Actual:- "+landingReferrer );
    }

    @Test(description = "Verify api.getClientIpAddress()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientIpAddress() fetches the client's Ip Address correctly")
    public void digiohAPIgetClientIpAddress() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientIpAddress = "return DIGIOH_API.getClientIpAddress();";
        String ipAddress = SeleniumUtil.executeJavascript(driver, jsGetClientIpAddress);

        String expectedIp = GenericUtil.getPublicIpAddress();
        Assert.assertTrue(ipAddress.equals(expectedIp),
                "Incorrect Ip Address fetched by getClientIpAddress(). Expected:- "+expectedIp+", Actual:- "+ipAddress );
    }

    @Test(description = "Verify api.getClientId()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientId() fetches the client Id correctly")
    public void digiohAPIgetClientId() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientId = "return DIGIOH_API.getClientId();";
        String clientId = SeleniumUtil.executeJavascript(driver, jsGetClientId);

        String clientIdRegex = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b-\\b[0-9]{13}\\b";

        Assert.assertTrue(clientId.matches(clientIdRegex),
                "The format of the clientId does not match. Actual Client Id returned is:- "+clientId+", whereas" +
                        "expected regex format is :- "+ clientIdRegex );
    }

    @Test(description = "Verify api.getClientContinentCode()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientContinentCode() fetches the correct continent code")
    public void digiohAPIgetClientContinentCode() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientContinentCode = "return DIGIOH_API.getClientContinentCode();";
        String actualClientContinentCode = SeleniumUtil.executeJavascript(driver, jsGetClientContinentCode);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        Assert.assertTrue(actualClientContinentCode.equals(geo.getContinentCode()),
                "The continent code does not match. Actual continent code returned is:- "+actualClientContinentCode+", whereas" +
                        "expected continent code :- "+ geo.getContinentCode() );
    }

    @Test(description = "Verify api.getClientContinentName()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientContinentName() fetches the correct continent name")
    public void digiohAPIgetClientContinentName() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientContinentName = "return DIGIOH_API.getClientContinentName();";
        String actualClientContinentName = SeleniumUtil.executeJavascript(driver, jsGetClientContinentName);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        Assert.assertTrue(actualClientContinentName.equals(geo.getContinentName()),
                "The continent name does not match. Actual continent name returned is:- "+actualClientContinentName+", whereas" +
                        "expected continent name :- "+ geo.getContinentName() );
    }

    @Test(description = "Verify api.isClientInEU()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isClientInEU() verifies whether client is in EU")
    public void digiohAPIisClientinEU() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsIsClientInEu = "return DIGIOH_API.isClientInEU();";
        Boolean actualIsClientInEu = SeleniumUtil.executeJavascript_Boolean(driver, jsIsClientInEu);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        Boolean expectedIsClientInEu = false;
        if(geo.getContinentCode() == "EU")
            expectedIsClientInEu = true;

        Assert.assertEquals(actualIsClientInEu, expectedIsClientInEu,"Value of isClientInEU() is not correct. Actual "+ actualIsClientInEu + " expected:- "+ expectedIsClientInEu);
    }

    @Test(description = "Verify api.getClientCountryCode()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientCountryCode() gives us the correct client country code successfully")
    public void digiohAPIgetClientCountryCode() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientCountryCode = "return DIGIOH_API.getClientCountryCode();";
        String actualClientCountryCode = SeleniumUtil.executeJavascript(driver, jsGetClientCountryCode);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        String expectedCountryCode = geo.getCountryCode2();
        Assert.assertEquals(actualClientCountryCode, expectedCountryCode,"Value of getClientCountryCode() is not correct. Actual "+ actualClientCountryCode + " expected:- "+ expectedCountryCode);
    }

    @Test(description = "Verify api.getClientCountryName()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientCountryName() gives us the correct client country name successfully")
    public void digiohAPIgetClientCountryName() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientCountryName = "return DIGIOH_API.getClientCountryName();";
        String actualClientCountryName = SeleniumUtil.executeJavascript(driver, jsGetClientCountryName);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        String expectedCountryName = geo.getCountryName();
        Assert.assertEquals(actualClientCountryName, expectedCountryName,"Value of getClientCountryName() is not correct. Actual "+ actualClientCountryName + " expected:- "+ expectedCountryName);
    }

    @Test(description = "Verify api.getClientRegionOrStateName()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientRegionOrStateName() gives us the correct client state name successfully")
    public void digiohAPIgetClientRegionOrStateName() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientRegionOrState = "return DIGIOH_API.getClientRegionOrStateName();";
        String actualClientRegionOrStateName = SeleniumUtil.executeJavascript(driver, jsGetClientRegionOrState);

        Assert.assertNotNull(actualClientRegionOrStateName, "getClientRegionOrStateName() is returning Null");
    }

    @Test(description = "Verify api.getClientCity()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientCity() gives us the correct client city name successfully")
    public void digiohAPIgetClientCity() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientCity = "return DIGIOH_API.getClientCity();";
        String actualClientCity = SeleniumUtil.executeJavascript(driver, jsGetClientCity);
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        Assert.assertNotNull(actualClientCity,"getClientCity is returning Null");
    }

    @Test(description = "Verify api.getClientPostalCode()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getClientPostalCode() gives us the correct client postal code successfully")
    public void digiohAPIgetClientPostalCode() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientPostalCode = "return DIGIOH_API.getClientPostalCode();";
        String actualClientPostalCode = SeleniumUtil.executeJavascript(driver, jsGetClientPostalCode);

        Assert.assertNotNull(actualClientPostalCode, "getClientPostalCode() is returning Null");
    }

    @Test(description = "Verify api.getOperatingSystem()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getOperatingSystem() gives us the clients operating system")
    public void digiohAPIgetOperatingSystem() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientOperatingSystem = "return DIGIOH_API.getOperatingSystem();";
        String actualClientOS = SeleniumUtil.executeJavascript(driver, jsGetClientOperatingSystem);
        String expectedOs = "";
        if(SystemUtils.OS_NAME.toLowerCase().contains("mac") || SystemUtils.OS_NAME.toLowerCase().contains("ios"))
        {
            expectedOs="Apple";
        }
        else if(SystemUtils.OS_NAME.toLowerCase().contains("windows")){
            expectedOs="Windows";
        }
        else{
            expectedOs="Android";
        }
        Assert.assertEquals(actualClientOS, expectedOs,"Value of getOperatingSystem() is not correct. Actual "+ actualClientOS + " expected:- "+ expectedOs);
    }


    @Test(description = "Verify api.getBrowserVersion()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getBrowserVersion() gives us the clients browser version")
    public void digiohAPIgetBrowserVersion() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetClientBrowserVersion = "return DIGIOH_API.getBrowserVersion();";
        String actualBrowserVersion = Long.toString(SeleniumUtil.executeJavascript_Long(driver, jsGetClientBrowserVersion));
        String expectedBrowserVersion = ((RemoteWebDriver) driver).getCapabilities().getVersion().split("[.]")[0];

        Assert.assertEquals(actualBrowserVersion, expectedBrowserVersion.toLowerCase(),"Value of digiohAPIgetBrowserVersion() is not correct. Actual "+ actualBrowserVersion + " expected:- "+ expectedBrowserVersion);
    }

    @Test(description = "Verify api.isMobile()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isMobile() shows whether its mobile or not")
    public void digiohAPIIsMobile() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisMobile = "return DIGIOH_API.isMobile();";
        Boolean actualIsMobile = SeleniumUtil.executeJavascript_Boolean(driver, jsisMobile);
        Platform pltfrm = ((RemoteWebDriver) driver).getCapabilities().getPlatform();
        Boolean expectedIsMobile = false;

        if(pltfrm.name().toLowerCase().contains("android") || pltfrm.name().toLowerCase().contains("ios")){
            expectedIsMobile = true;
        }
        Assert.assertEquals(actualIsMobile, expectedIsMobile,"value of DIGIOH_API.isMobile() is incorrect. Expected:- "+expectedIsMobile+". Actual is :-"+actualIsMobile);
    }

    @Test(description = "Verify api.isTablet()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isTablet() shows whether its tablet or not")
    public void digiohAPIIsTablet() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisTablet = "return DIGIOH_API.isTablet();";
        Boolean actualIsTablet = SeleniumUtil.executeJavascript_Boolean(driver, jsisTablet);
        Platform pltfrm = ((RemoteWebDriver) driver).getCapabilities().getPlatform();
        Boolean expectedIsTablet = false;

        Assert.assertEquals(actualIsTablet, expectedIsTablet,"value of DIGIOH_API.isTablet() is incorrect. Expected:- "+expectedIsTablet+". Actual is :-"+actualIsTablet);
    }

    @Test(description = "Verify api.isDesktop()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isDesktop() shows whether its desktop or not")
    public void digiohAPIisDesktop() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisDesktop = "return DIGIOH_API.isDesktop();";
        Boolean actualIsDesktop = SeleniumUtil.executeJavascript_Boolean(driver, jsisDesktop);
        Platform pltfrm = ((RemoteWebDriver) driver).getCapabilities().getPlatform();
        Boolean expectedIsDesktop = true;

        if(pltfrm.name().toLowerCase().contains("android") || pltfrm.name().toLowerCase().contains("ios")){
            expectedIsDesktop = false;
        }

        Assert.assertEquals(actualIsDesktop, expectedIsDesktop,"value of DIGIOH_API.isDesktop() is incorrect. Expected:- "+expectedIsDesktop+". Actual is :-"+actualIsDesktop);
    }

    @Test(description = "Verify api.isAndroid()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isAndroid() shows whether its android or not")
    public void digiohAPIisAndroid() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisAndroid = "return DIGIOH_API.isAndroid();";
        Boolean actualIsAndroid = SeleniumUtil.executeJavascript_Boolean(driver, jsisAndroid);
        Platform pltfrm = ((RemoteWebDriver) driver).getCapabilities().getPlatform();
        Boolean expectedIsAndroid= false;

        if(pltfrm.name().toLowerCase().contains("android")){
            expectedIsAndroid = true;
        }

        Assert.assertEquals(actualIsAndroid, expectedIsAndroid,"value of DIGIOH_API.isAndroid() is incorrect. Expected:- "+expectedIsAndroid+". Actual is :-"+actualIsAndroid);
    }

    @Test(description = "Verify api.isIOS()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isIOS() shows whether its ios or not")
    public void digiohAPIisIOS() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisIOS = "return DIGIOH_API.isIOS();";
        Boolean actualIsIOS = SeleniumUtil.executeJavascript_Boolean(driver, jsisIOS);
        Platform pltfrm = ((RemoteWebDriver) driver).getCapabilities().getPlatform();
        Boolean expectedIsIOS= false;

        if(pltfrm.name().toLowerCase().contains("ios")){
            expectedIsIOS = true;
        }

        Assert.assertEquals(actualIsIOS, expectedIsIOS,"value of DIGIOH_API.isIOS() is incorrect. Expected:- "+expectedIsIOS+". Actual is :-"+actualIsIOS);
    }

    @Test(description = "Verify api.isNewSession()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isNewSession() shows whether its a new session or or not for the page view")
    public void digiohAPIisNewSession() throws Exception {
        driver.quit();
        setUp();

        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisNewSession = "return DIGIOH_API.isNewSession();";
        Boolean actualIsNewSession = SeleniumUtil.executeJavascript_Boolean(driver, jsisNewSession);
        Assert.assertEquals(actualIsNewSession, Boolean.valueOf(true),"value of DIGIOH_API.isNewSession() is incorrect. Expected:- true. Actual is :- "+actualIsNewSession);

        driver.navigate().refresh();
        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        actualIsNewSession = SeleniumUtil.executeJavascript_Boolean(driver, jsisNewSession);
        Assert.assertEquals(actualIsNewSession, Boolean.valueOf(false),"value of DIGIOH_API.isNewSession() is incorrect. Expected:- false. Actual is :- "+actualIsNewSession);

    }

    @Test(description = "Verify api.isNewVisitor()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isNewVisitor() shows whether its a new visitor or or not for the session")
    public void digiohAPIisNewVisitor() throws Exception {
        driver.quit();
        setUp();

        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsisNewVisitor = "return DIGIOH_API.isNewVisitor();";
        Boolean actualIsNewVisitor = SeleniumUtil.executeJavascript_Boolean(driver, jsisNewVisitor);
        Assert.assertEquals(actualIsNewVisitor, Boolean.valueOf(true),"value of DIGIOH_API.isNewVisitor() is incorrect. Expected:- true. Actual is :- "+actualIsNewVisitor);

        driver.navigate().refresh();
        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        actualIsNewVisitor = SeleniumUtil.executeJavascript_Boolean(driver, jsisNewVisitor);
        Assert.assertEquals(actualIsNewVisitor, Boolean.valueOf(false),"value of DIGIOH_API.isNewVisitor() is incorrect. Expected:- false. Actual is :- "+actualIsNewVisitor);

    }

    @Test(description = "Verify api.getPagesNavigatedAllTime()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getPagesNavigatedAllTime() returns all the urls visited by this user in sequence")
    public void digiohAPIgetPagesNavigatedAllTime() throws Exception {
        driver.quit();
        setUp();

        BasicBox basicBox = new BasicBox(driver);
        List<String> expectedUrls = new ArrayList<>();

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();
        expectedUrls.add(driver.getCurrentUrl());

        driver.findElement(By.linkText("Test Automation")).click();
        Thread.sleep(2000);
        expectedUrls.add(driver.getCurrentUrl());

        String jsgetAllUrls = "return DIGIOH_API.getPagesNavigatedAllTime();";
        List<String> actualUrls = SeleniumUtil.executeJavascript_Array(driver, jsgetAllUrls);

        Assert.assertEquals(actualUrls, expectedUrls,"value of DIGIOH_API.getPagesNavigatedAllTime() is incorrect. Expected:- "+expectedUrls+". Actual is :- "+actualUrls);
    }

    @Test(description = "Verify api.getDaysSinceLastPageview()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getDaysSinceLastPageview() returns number of days since last page view")
    public void digiohAPIgetDaysSinceLastPageview() throws Exception {
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        String jsgetDaysSinceLastPageView = "return DIGIOH_API.getDaysSinceLastPageview();";
        Long actualDaysSinceLastPageView= SeleniumUtil.executeJavascript_Long(driver, jsgetDaysSinceLastPageView);

        Assert.assertEquals(actualDaysSinceLastPageView,Long.valueOf(0),"value of DIGIOH_API.getDaysSinceLastPageview() is incorrect. Expected:- 0. Actual is :- "+actualDaysSinceLastPageView);

        driver.navigate().refresh();
        Thread.sleep(3000);
        actualDaysSinceLastPageView= SeleniumUtil.executeJavascript_Long(driver, jsgetDaysSinceLastPageView);

        Assert.assertEquals(actualDaysSinceLastPageView,Long.valueOf(0),"value of DIGIOH_API.getDaysSinceLastPageview() is incorrect. Expected:- 0. Actual is :- "+actualDaysSinceLastPageView);
    }

    @Test(description = "Verify api.getAccountId() & api.getAccountGuid()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getAccountId() & api.getAccountGuid() returns the respective ids of your digioh account")
    public void digiohAPIgetUserAccountIds() throws Exception {
       // List<String> ids = dashboardPage.fetchAccountIds("Blank Lightbox");
        List<String> ids = new ArrayList<>();
        ids.add("43508");
        ids.add("7d49bc08-bf68-4971-901f-1743a41b5417");
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        String jsGetAccountId = "return DIGIOH_API.getAccountId();";
        String actualAccountId = SeleniumUtil.executeJavascript(driver, jsGetAccountId);

        String jsGetAccountGuid = "return DIGIOH_API.getAccountGuid();";
        String actualAccountGuid = SeleniumUtil.executeJavascript(driver, jsGetAccountGuid);

        List<String> actualIds = new ArrayList<>();
        actualIds.add(actualAccountId);
        actualIds.add(actualAccountGuid);

        Assert.assertEquals(actualIds,ids,"value of DIGIOH_API.getAccountGuid() or DIGIOH_API.getAccountId() are incorrect. Expected :-"+ids.get(1)+" Actual is :- "+actualAccountGuid);
    }

    @Test(description = "Verify api.getBoxGUID(string boxId) & api.getBoxID(string box Guid)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getBoxGUID(string boxId) & api.getBoxID(string box Guid) returns the box id and guid respectively")
    public void digiohAPIgetBoxIds() throws Exception {
        driver.get(portalBaseUrl);
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = loginPage.login(username,password);
        List<String> ids = dashboardPage.fetchAccountIds("Blank Lightbox");
        dashboardPage.logoutPage(driver);
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        String jsGetBoxGuid = "return DIGIOH_API.getBoxGUID('"+ids.get(0)+"');";
        String actualBoxGuid = SeleniumUtil.executeJavascript(driver, jsGetBoxGuid);

        String jsGetAccountId = "return DIGIOH_API.getBoxID('"+ids.get(1)+"');";
        String actualBoxId = SeleniumUtil.executeJavascript(driver, jsGetAccountId);

        Assert.assertEquals(actualBoxId,ids.get(0),"value of DIGIOH_API.getBoxID() is incorrect. Expected :-"+ids.get(0)+" Actual is :- "+actualBoxId);
        Assert.assertEquals(actualBoxGuid,ids.get(1),"value of DIGIOH_API.getBoxGUID() is incorrect. Expected :-"+ids.get(1)+" Actual is :- "+actualBoxGuid);
    }

    @Test(description = "Verify api.openBox(string boxId) & api.closeBox(string boxId)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.openBox(string boxId) & api.closeBox successfully opens up and closes the digioh box")
    public void digiohAPIopenAndCloseBox() throws Exception {
        driver.get(portalBaseUrl);
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = loginPage.login(username,password);
        List<String> ids = dashboardPage.fetchAccountIds("Blank Lightbox");
        dashboardPage.logoutPage(driver);
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/newurl");
        Thread.sleep(8000);

        //opening and closing the digioh box via id
        String jsOpenBox = "return DIGIOH_API.openBox('"+ids.get(0)+"');";
        SeleniumUtil.executeJavascript(driver, jsOpenBox);

        String textInsideBox = basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsCloseBox = "return DIGIOH_API.closeBox('"+ids.get(0)+"');";
        SeleniumUtil.executeJavascript(driver, jsCloseBox);

        Boolean isBoxVisible = basicBox.isBoxVisible();
        Assert.assertTrue(isBoxVisible.equals(false),"Basic Box is not disappearing after executing DIGIOH_API.closeBox()");

        //opening and closing the digioh box via GUID
        String jsOpenBox2 = "return DIGIOH_API.openBox('"+ids.get(1)+"');";
        SeleniumUtil.executeJavascript(driver, jsOpenBox2);

        String textInsideBox2 = basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsCloseBox2 = "return DIGIOH_API.closeBox('"+ids.get(1)+"');";
        SeleniumUtil.executeJavascript(driver, jsCloseBox2);

        Boolean isBoxVisible2 = basicBox.isBoxVisible();
        Assert.assertTrue(isBoxVisible2.equals(false),"Basic Box is not disappearing after executing DIGIOH_API.closeBox()");
    }

    @Test(description = "Verify api.writeCustomFlag(string flagId)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.writeCustomFlag(string flagId) creates a div successfully")
    public void digiohAPIwriteCustomFlag() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        String jswriteCustomFlag = "return DIGIOH_API.writeCustomFlag('automation_id');";
        SeleniumUtil.executeJavascript(driver, jswriteCustomFlag);

        WebElement test = driver.findElement(By.id("automation_id"));

        Assert.assertNotNull(test,"new div not created by api.writeCustomFlag()");
    }

    @Test(description = "Verify api.isValidEmail(email)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isValidEmail(email) successfully validates the email")
    public void digiohAPIisValidEmail() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();
        basicBox.closeBox();
        driver.switchTo().defaultContent();

        String jsisValidEmail = "return DIGIOH_API.isValidEmail('automation@digioh.com');";
        Boolean isValid = SeleniumUtil.executeJavascript_Boolean(driver, jsisValidEmail);
        Assert.assertTrue(isValid,"api.isValidEmail() not returning true even for valid email:- automation@digioh.com");

        jsisValidEmail = "return DIGIOH_API.isValidEmail('automation@digioh.co.uk');";
        Boolean isValid2 = SeleniumUtil.executeJavascript_Boolean(driver, jsisValidEmail);
        Assert.assertTrue(isValid2,"api.isValidEmail() not returning true even for valid email:- automation@digoh.co.uk");

        jsisValidEmail = "return DIGIOH_API.isValidEmail('automation@digioh');";
        Boolean isValid3 = SeleniumUtil.executeJavascript_Boolean(driver, jsisValidEmail);
        Assert.assertFalse(isValid3,"api.isValidEmail() returning true even for invalid email:- automation");
    }

    @Test(description = "Verify api.reload()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.reload() successfully closes all box and reinitializes the digioh rule engine")
    public void digiohAPIReload() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl);

        Thread.sleep(3000);

        String jswriteCustomFlag = "return DIGIOH_API.writeCustomFlag('test_reload');";
        SeleniumUtil.executeJavascript(driver, jswriteCustomFlag);

        String jsReload = "return DIGIOH_API.reload();";
        SeleniumUtil.executeJavascript(driver, jsReload);

        basicBox.getTextInsideBox();
        basicBox.closeBox();
    }

    @Test(description = "Verify api.enableReloadOnUrlChange()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.enableReloadOnUrlChange() corretly calls reloadDigioh on url changes")
    public void digiohAPIenableReloadOnUrlChange() throws Exception {
        driver.quit();
        setUp();
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl);

        Thread.sleep(3000);

        String jswriteCustomFlag = "return DIGIOH_API.writeCustomFlag('test_reload');";
        SeleniumUtil.executeJavascript(driver, jswriteCustomFlag);

        String reloadOnUrlChange = "return DIGIOH_API.enableReloadOnUrlChange();";
        SeleniumUtil.executeJavascript(driver, reloadOnUrlChange);

        String newUrl = "return history.pushState(null, '', 'https://digiohautotest.wpengine.com/newUrl');";
        SeleniumUtil.executeJavascript(driver, newUrl);

        basicBox.getTextInsideBox();
        basicBox.closeBox();
    }

    @Test(description = "Verify api.isReady()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.isReady() correctly returns true when digioh has been fully initialized")
    public void digiohApiIsReady() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?abort=1");

        Thread.sleep(3000);

        String jsIsReady = "return DIGIOH_API.isReady();";
        Boolean isReady = SeleniumUtil.executeJavascript_Boolean(driver, jsIsReady);

        Assert.assertFalse(isReady,"Expecting DIGOIOH.isReady() to return false but returned true");

        driver.get(testPageBaseUrl);
        Thread.sleep(3000);
        Boolean isReady2 = true;
        int waitSecs = 0;
        while((!SeleniumUtil.executeJavascript_Boolean(driver, jsIsReady)) && waitSecs < 10) {
            Thread.sleep(1000);
            waitSecs+=1;
        }
        if(waitSecs>=10){
            isReady2 = false;
        }
        Assert.assertTrue(isReady2,"Expecting DIGOIOH.isReady() to return true but returned false");
    }

    @Test(description = "Verify boxapi.getName()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.getName() correctly returns the name of the box")
    public void digiohBoxApiGetName() throws Exception {
        BasicBox basicBox = new BasicBox(driver);
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();

        String jsGetBoxName = "return boxapi.getName();";
        String boxName = SeleniumUtil.executeJavascript(driver, jsGetBoxName);

        Assert.assertEquals(boxName,"Blank Lightbox","Expecting boxapi.getName() not returning Blank Lightbox");
    }

    @Test(description = "Verify boxapi.getId()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.getId() correctly returns the id of the box")
    public void digiohBoxApiGetId() throws Exception {
        driver.get(portalBaseUrl);
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboardPage = loginPage.login(username,password);
        List<String> ids = dashboardPage.fetchAccountIds("Blank Lightbox");
        dashboardPage.logoutPage(driver);
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();

        String jsGetBoxId = "return boxapi.getId();";
        String boxId = SeleniumUtil.executeJavascript(driver, jsGetBoxId);

        Assert.assertEquals(boxId,ids.get(0),"Expecting boxapi.id() not returning box id");
    }

    @Test(description = "Verify boxapi.getBoxType()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.getBoxType() correctly returns the id of the box")
    public void digiohBoxApiGetBoxType() throws Exception {
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();

        String jsGetBoxType = "return boxapi.getBoxType();";
        String boxType = SeleniumUtil.executeJavascript(driver, jsGetBoxType);

        Assert.assertEquals(boxType,"lightbox","Expecting boxapi.getBoxType() to return lightbox but actually it returns:- "+boxType);
    }

    @Test(description = "Verify boxapi.getBoxFrameWrapper()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.getBoxFrameWrapper() correctly returns the BoxFrameWrapper details")
    public void digiohBoxApiGetBoxFrameWrapper() throws Exception {
        BasicBox basicBox = new BasicBox(driver);

        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline");

        basicBox.getTextInsideBox();

        String jsGetBoxFrameWrapper = "return boxapi.getBoxFrameWrapper().length";
        Long boxFrameWrapper = SeleniumUtil.executeJavascript_Long(driver, jsGetBoxFrameWrapper);

        Assert.assertEquals(boxFrameWrapper,Long.valueOf(1),"Expecting boxapi.getBoxFrameWrapper() to return length of array 1, but the actual size of array is :-"+ boxFrameWrapper);
    }

    @Test(description = "Verify boxapi.showPage('PageName')")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.showPage('PageName') correctly redirects to the correct page of the box")
    public void digiohBoxApiShowPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiShowPage = "return boxapi.showPage('thx')";
        String boxApiShowPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowPage);
        Assert.assertTrue(emailBox.verifyThankYouMessageAppearingSuccessfully(),"boxapi.showPage('thx') not redirecting to thank you page");

        String jsGetBoxApiShowPage1 = "return boxapi.showPage('ep1')";
        String boxApiShowPage1 = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowPage1);
        Assert.assertTrue(emailBox.verifyExtraPage1Successfully(),"boxapi.showPage('ep1') not redirecting to special text1 page");

        String jsGetBoxApiShowMainPage = "return boxapi.showPage('main')";
        String boxApiShowMainPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowMainPage);
        Assert.assertTrue(emailBox.verifySignUpForNewsLetterHeaderAppers(),"boxapi.showPage('main') not redirecting to main page");
    }

    @Test(description = "Verify boxapi.showMainPage()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.showMainPage() correctly redirects to the correct main page of the box")
    public void digiohBoxApiShowMainPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiShowPage = "return boxapi.showPage('thx')";
        String boxApiShowPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowPage);
        Assert.assertTrue(emailBox.verifyThankYouMessageAppearingSuccessfully(),"boxapi.showPage('thx') not redirecting to thank you page");

        String jsGetBoxApiShowMainPage = "return boxapi.showMainPage()";
        String boxApiShowMainPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowMainPage);
        Assert.assertTrue(emailBox.verifySignUpForNewsLetterHeaderAppers(),"boxapi.showMainPage() not redirecting to main page");
    }

    @Test(description = "Verify boxapi.showThankYouPage()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.showThankYouPage() correctly redirects to the thank you page")
    public void digiohBoxApiShowThankYouPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiShowThankYouPage= "return boxapi.showThankYouPage()";
        String boxApiShowThankYouPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowThankYouPage);
        Assert.assertTrue(emailBox.verifyThankYouMessageAppearingSuccessfully(),"boxapi.showThankYouPage() not redirecting to thankyou page");
    }

    @Test(description = "Verify boxapi.showExtraPage(number)")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.showExtraPage(number) correctly redirects to the extra page")
    public void digiohBoxApiShowExtraPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiShowExtraPage= "return boxapi.showExtraPage(1)";
        String boxApiShowExtraPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowExtraPage);
        Assert.assertTrue(emailBox.verifyExtraPage1Successfully(),"boxapi.showExtraPage(1) not redirecting to first extra page successfully");
    }

    @Test(description = "Verify boxapi.isMainPage()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.isMainPage() correctly returns whether its main page or not")
    public void digiohBoxApiIsMainPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiIsMainPage= "return boxapi.isMainPage()";
        Boolean boxApiIsMainPage = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsMainPage);
        Assert.assertTrue(boxApiIsMainPage,"boxapi.isMainPage() not returning true even on main page");

        String jsGetBoxApiShowExtraPage= "return boxapi.showExtraPage(1)";
        String boxApiShowExtraPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowExtraPage);
        emailBox.verifyExtraPage1Successfully();

        String jsGetBoxApiIsMainPage1= "return boxapi.isMainPage()";
        Boolean boxApiIsMainPage1 = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsMainPage1);
        Assert.assertFalse(boxApiIsMainPage1,"boxapi.isMainPage() not returning false when NOT on main page");
    }

    @Test(description = "Verify boxapi.isThankYouPage()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.isThankYouPage() correctly returns whether its Thank You page or not")
    public void digiohBoxApiIsThankYouPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiIsThankYouPage= "return boxapi.isThankYouPage()";
        Boolean boxApiIsThankYouPage = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsThankYouPage);
        Assert.assertFalse(boxApiIsThankYouPage,"boxapi.isThankYouPage() returning true even when not on ThankYou page");

        String jsGetBoxApiShowThankYoupage= "return boxapi.showThankYouPage()";
        String boxApiShowThankYoupage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowThankYoupage);
        emailBox.verifyThankYouMessageAppearingSuccessfully();

        String jsGetBoxApiIsThankYouPage1= "return boxapi.isThankYouPage()";
        Boolean boxApiIsThanYouPage1 = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsThankYouPage1);
        Assert.assertTrue(boxApiIsThanYouPage1,"boxapi.isThankYouPage() not returning true when on ThankYou page");
    }

    @Test(description = "Verify boxapi.isExtraPage()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the boxapi.isExtraPage() correctly returns whether its extra page or not")
    public void digiohBoxApiIsExtraPage() throws Exception {
        EmailBox emailBox = new EmailBox(driver);
        driver.get(testPageBaseUrl+UrlPaths.EMAIL_FORM_SUBMIT);
        String email = "nishbluellabel+"+ GenericUtil.getCurrentTimestamp()+"@gmail.com";

        emailBox.waitForBoxToAppear(15);

        String jsGetBoxApiIsExtraPage = "return boxapi.isExtraPage(1)";
        Boolean boxApiIsExtraPage = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsExtraPage);
        Assert.assertFalse(boxApiIsExtraPage,"boxapi.isExtraPage(1) returning true even when not on Extra page");

        String jsGetBoxApiShowExtraPage = "return boxapi.showExtraPage(1)";
        String boxApiShowExtraPage = SeleniumUtil.executeJavascript(driver, jsGetBoxApiShowExtraPage);
        emailBox.verifyExtraPage1Successfully();

        String jsGetBoxApiIsExtraPage1= "return boxapi.isExtraPage(1)";
        Boolean boxApiIsExtraPage1 = SeleniumUtil.executeJavascript_Boolean(driver, jsGetBoxApiIsExtraPage1);
        Assert.assertTrue(boxApiIsExtraPage1,"boxapi.isExtraPage(1) not returning true when on Extra page");
    }
}
