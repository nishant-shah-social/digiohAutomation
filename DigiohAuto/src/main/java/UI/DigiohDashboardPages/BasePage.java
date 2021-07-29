package UI.DigiohDashboardPages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class BasePage {
    protected WebDriver driver;

    public BasePage(WebDriver driver){
        this.driver = driver;
        initElements();
    }

    private void initElements(){
        PageFactory.initElements(driver,this);
    }
}
