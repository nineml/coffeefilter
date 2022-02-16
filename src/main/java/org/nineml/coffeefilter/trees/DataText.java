package org.nineml.coffeefilter.trees;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A node in a {@link DataTree} that contains a single atomic value.
 */
public class DataText extends DataTree {
    private static final long maxInt = +9007199254740991L;  // 2^53-1
    private static final long minInt = -9007199254740993L; // -(2^53+1)
    private static final Pattern intRegex = Pattern.compile("^[-+]?[0-9]+$");
    private static final Pattern floatRegex = Pattern.compile("^([-+]?)([0-9]+\\.[0-9]*)$");
    private final String text;

    protected DataText(DataTree parent, String text) {
        super(parent, null);
        this.text = text;
    }

    /**
     * Get the value.
     * @return the value.
     */
    @Override
    public String getValue() {
        return text;
    }

    @Override
    public String asXML() {
        return text;
    }

    @Override
    public String asJSON() {
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

        String json = text.replace("\\", "\\\\");
        json = json.replace("\"", "\\\"");

        return '"' + json + '"';
    }

}
