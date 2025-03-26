package sdk.base;

public class StringUtils {

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty() || string.equals("null");
    }

}
