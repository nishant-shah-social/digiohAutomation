package UI.TestAppPages;

import UI.SeleniumUtils.SeleniumUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class TestAppHomePage {
    protected WebDriver driver;


    @FindBy(xpath="//iframe[@title='Blank Lightbox']")
    private List<WebElement> basicBoxiFrames;

    public TestAppHomePage(WebDriver driver){
        this.driver = driver;
        initElements();
    }
    private void initElements(){
        PageFactory.initElements(driver,this);
    }

    public void scrollDown() throws InterruptedException {
        SeleniumUtil.scrollDownABit(driver);
    }

    public Boolean basicBoxShownOnThePage(){
        if(basicBoxiFrames.size()>0)
            return true;
        else
            return false;
    }
}
