package GenericUtilities;
import io.ipgeolocation.api.Geolocation;
import io.ipgeolocation.api.GeolocationParams;
import io.ipgeolocation.api.IPGeolocationAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    public static String getFutureDate(int daysToAdd){
        LocalDate newLocalDate = LocalDate.now().plusDays(daysToAdd);
        return newLocalDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public static String getCurrentTimestamp(){
        Date current = new Date();
        return String.valueOf(current.getTime());
    }

    public static String getPublicIpAddress() throws IOException {
        URL whatIsMyIp = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatIsMyIp.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }

    public static Geolocation getGeoLocationOfClient() throws IOException {
        IPGeolocationAPI api = new IPGeolocationAPI(PropertyUtil.getInstance().getValue("geolocation_api_key"));
        String ipAddress = getPublicIpAddress();
        GeolocationParams geoParams = new GeolocationParams();
        geoParams.setIPAddress(ipAddress);
        //geoParams.setFields("geo,time_zone,currency");

        Geolocation geolocation = api.getGeolocation(geoParams);

        if(geolocation.getStatus() == 200) {
            return geolocation;
        } else {
            return null;
        }
    }

}
