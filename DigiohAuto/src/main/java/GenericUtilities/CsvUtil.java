package GenericUtilities;

import com.opencsv.CSVReader;
import org.testng.annotations.DataProvider;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CsvUtil {
    private CSVReader reader = null;
    private Object[][] data = null;

    private String getPath(){
        File file = new File("src/main/resources/BasicBoxCsv.csv");
        return file.getAbsolutePath();
    }

    @DataProvider(name="DataProviderIterator")
    public Iterator<Object[]> provider() throws IOException {
        String fileName = getPath();
        List<Object []> urlPaths = new ArrayList<>();
        String[] data= null;

        //this loop is pseudo code
        BufferedReader br = new BufferedReader(new FileReader(fileName));

        String line;
        while ((line = br.readLine()) != null) {
            // use comma as separator
            data= line.split(",");
            urlPaths.add(data);
        }
        return urlPaths.iterator();
    }
}
