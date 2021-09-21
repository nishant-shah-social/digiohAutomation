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
        driver.get(testPageBaseUrl+"/?CURRENT_PAGE_URL_CONTAINS&tv=inline#:~:text=Search-,Recent%20Posts,-Test%20Automation");

        basicBox.getTextInsideBox();
        driver.switchTo().defaultContent();

        String jsGetUrlHash = "return DIGIOH_API.getUrlHash();";
        String urlHash = SeleniumUtil.executeJavascript(driver, jsGetUrlHash);

        Assert.assertTrue(urlHash.equals("#:~:text=Search-,Recent%20Posts,-Test%20Automation"),
                "Incorrect Domain name fetched by getUrlHash(). Expected:- #:~:text=Search-,Recent%20Posts,-Test%20Automation, Actual:- "+urlHash );
    }

    @Test(description = "Verify api.getLandingPageUrl()")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify the api.getLandingPageUrl() fetches the url hash correctly")
    public void digiohAPIgetLandingPageUrl() throws Exception {
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
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        String expectedStateName = geo.getStateProvince();
        Assert.assertEquals(actualClientRegionOrStateName, expectedStateName,"Value of getClientRegionOrStateName() is not correct. Actual "+ actualClientRegionOrStateName + " expected:- "+ expectedStateName);
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

        String expectedCity = geo.getCity();
        Assert.assertEquals(actualClientCity, expectedCity,"Value of getClientCity() is not correct. Actual "+ actualClientCity + " expected:- "+ expectedCity);
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
        Geolocation geo = GenericUtil.getGeoLocationOfClient();

        String expectedPostalCode = geo.getZipCode();
        Assert.assertEquals(actualClientPostalCode, expectedPostalCode,"Value of getClientPostalCode() is not correct. Actual "+ actualClientPostalCode + " expected:- "+ expectedPostalCode);
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
        String expectedBrowserVersion = ((RemoteWebDriver) driver).getCapabilities().getVersion();
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

        Assert.assertEquals(actualDaysSinceLastPageView,Long.valueOf(18889),"value of DIGIOH_API.getDaysSinceLastPageview() is incorrect. Expected:- 18889. Actual is :- "+actualDaysSinceLastPageView);

        driver.navigate().refresh();
        actualDaysSinceLastPageView= SeleniumUtil.executeJavascript_Long(driver, jsgetDaysSinceLastPageView);

        Assert.assertEquals(actualDaysSinceLastPageView,Long.valueOf(0),"value of DIGIOH_API.getDaysSinceLastPageview() is incorrect. Expected:- 0. Actual is :- "+actualDaysSinceLastPageView);
    }
}
