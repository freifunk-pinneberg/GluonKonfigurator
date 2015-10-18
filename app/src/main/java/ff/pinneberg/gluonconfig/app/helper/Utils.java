package ff.pinneberg.gluonconfig.app.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xilent on 18.10.15.
 */
public class Utils {


    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    public static boolean isIpAddress(String ipAddress) {

        Matcher m1 = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE).matcher(ipAddress);
        if (m1.matches()) {
            return true;
        }
        Matcher m2 = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE).matcher(ipAddress);
        return m2.matches();
    }


}
