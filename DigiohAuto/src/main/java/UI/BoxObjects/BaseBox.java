package UI.BoxObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class BaseBox {
    protected WebDriver driver;

    public BaseBox(WebDriver driver){
        this.driver = driver;
        initElements();
    }

    private void initElements(){
        PageFactory.initElements(driver,this);
    }
}
