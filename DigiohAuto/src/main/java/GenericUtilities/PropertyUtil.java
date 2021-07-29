package GenericUtilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertyUtil {
    private static PropertyUtil propertyUtil;
    private static final Properties properties = new Properties();
    private static final String PROPERTY_FILE_PATH = "src/main/resources/config.properties";

    private PropertyUtil() throws Exception {
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(PROPERTY_FILE_PATH));
            properties.load(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Config properties not found at location:- "+PROPERTY_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PropertyUtil getInstance(){
        if(propertyUtil == null){
            synchronized (PropertyUtil.class){
                try{
                    propertyUtil = new PropertyUtil();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return propertyUtil;
    }

    public String getValue(String key){
        return properties.getProperty(key);
    }

}
