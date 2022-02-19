package org.nineml.coffeefilter.utils;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.tokens.CharacterSet;

/**
 * Utility methods for tokens and names.
 */
public class TokenUtils {
    public static int convertHex(String hex) {
        try {
            long cp = Long.parseLong(hex, 16);
            if (cp > 0xffffffffL) {
                throw IxmlException.invalidHex(hex);
            }

            // Noncharacters
            if ((cp&0xfffe) == 0xfffe || (cp&0xffff) == 0xffff || (cp >= 0xfdd0 && cp <= 0xfdef)) {
                throw IxmlException.invalidHex(hex);
            }

            // Surrogates
            if ((cp >= 0xd800 && cp <= 0xdbff) || (cp >= 0xdc00 && cp <= 0xdfff)) {
                throw IxmlException.invalidHex(hex);
            }

            return (int) cp;
        } catch (NumberFormatException ex) {
            // Can only have been too large because the ixml parse will only
            // have provided hexidecimal digits.
            throw IxmlException.invalidHex(hex);
        }
    }

    /**
     * Is this a valid XML name?
     * @param name the name
     * @return true if it is a valid XML 1.0 5th edition XML name.
     */
    public static boolean xmlName(String name) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }

        if ("".equals(name)) {
            return false;
        }

        // I didn't use regex for this because I wasn't sure how to represent
        // the supplementary characters that are allowed in names.

        if (name.length() == 1) {
            return nameStartChar(name.codePointAt(0));
        } else {
            if (!nameStartChar(name.codePointAt(0))) {
                return false;
            }
            for (int pos = 1; pos < name.length(); pos++) {
                if (!nameChar(name.codePointAt(pos))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean nameStartChar(int ch) {
        // [4] NameStartChar ::= [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF]
        //                       | [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F]
        //                       | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD]
        //                       | [#x10000-#xEFFFF]
        // Technically, also ":", but no.

        // This doesn't feel like the most efficient way to do this, but ...
        return ch == '_'
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z')
                || (ch >= 0x00C0 && ch <= 0x00D6)
                || (ch >= 0x00D8 && ch <= 0x00F6)
                || (ch >= 0x00F8 && ch <= 0x02FF)
                || (ch >= 0x0370 && ch <= 0x037D)
                || (ch >= 0x037F && ch <= 0x1FFF)
                || (ch >= 0x200C && ch <= 0x200D)
                || (ch >= 0x2070 && ch <= 0x218F)
                || (ch >= 0x2C00 && ch <= 0x2FEF)
                || (ch >= 0x3001 && ch <= 0xD7FF)
                || (ch >= 0xF900 && ch <= 0xFDCF)
                || (ch >= 0xFDF0 && ch <= 0xFFFD)
                || (ch >= 0x10000 && ch <= 0xEFFFF);
    }

    private static boolean nameChar(int ch) {
        // [4a]	NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
        return nameStartChar(ch)
                || ch == '-'
                || ch == '.'
                || (ch >= '0' && ch <= '9')
                || ch == 0xB7
                || (ch >= 0x0300 && ch <= 0x036F)
                || (ch >= 0x2034 && ch <= 0x2040);
    }
}
