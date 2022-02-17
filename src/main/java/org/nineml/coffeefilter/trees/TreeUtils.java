package org.nineml.coffeefilter.trees;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TreeUtils {
    public static final long maxInt = +9007199254740991L;  // 2^53-1
    public static final long minInt = -9007199254740993L; // -(2^53+1)
    public static final Pattern intRegex = Pattern.compile("^[-+]?[0-9]+$");
    public static final Pattern floatRegex = Pattern.compile("^([-+]?)([0-9]+\\.[0-9]*)$");

    public static String xmlEscape(String text) {
        String xml = text.replace("&", "&amp;");
        xml = xml.replace("<", "&lt;").replace(">", "&gt;");
        return xml;
    }

    public static String xmlEscapeAttribute(String text) {
        String xml = xmlEscape(text);
        xml = xml.replace("\"", "&quot;");
        return xml;
    }

    public static String jsonEscape(String text) {
        String json = text.replace("\\", "\\\\");
        json = json.replace("\"", "\\\"");
        return json;

    }

    public static String jsonValue(String text) {
        if ("true".equals(text) || "false".equals(text) || "null".equals(text)) {
            return text;
        }

        Matcher match = intRegex.matcher(text);
        if (match.matches()) {
            long value = Long.parseLong(text, 10);
            if (value >= minInt && value <= maxInt) {
                return ""+value;
            }
        }

        match = floatRegex.matcher(text);
        if (match.matches()) {
            if ("+".equals(match.group(1))) {
                return match.group(2);
            } else {
                return text;
            }
        }

        return "\"" + jsonEscape(text) + "\"";
    }

    public static String csvEscape(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
