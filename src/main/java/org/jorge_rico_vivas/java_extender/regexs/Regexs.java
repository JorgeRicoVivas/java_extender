package org.jorge_rico_vivas.java_extender.regexs;

import java.util.regex.Pattern;

public class Regexs {

    public static final Pattern NUMBER_REGEX = Pattern.compile("(?<sign>-)?(?<absFloat>(?<integer>\\d+)(\\.(?<decimal>\\d+))?)");

    public static Number numberFind(String number) {
        var matcher = NUMBER_REGEX.matcher(number);
        if (!matcher.find()) return null;
        if (matcher.group("decimal") != null) {
            return Float.valueOf(number);
        }
        number = matcher.group(0);
        try {
            return Integer.valueOf(number);
        } catch (Exception e) {
            try {
                return Long.valueOf(number);
            } catch (Exception e2) {
                return null;
            }
        }
    }

}
