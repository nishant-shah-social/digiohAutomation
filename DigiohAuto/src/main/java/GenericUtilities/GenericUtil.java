package GenericUtilities;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class GenericUtil {

    public static String getTodaysDate(){
        DateFormat format = new SimpleDateFormat("MM/dd/YYYY");
        format.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String strDate = format.format(new Date());
        return strDate;
    }

    public static String getCurrentTimestamp(){
        Date current = new Date();
        return String.valueOf(current.getTime());
    }
}
