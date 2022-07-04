package org.nineml.coffeefilter.util;

import org.nineml.coffeefilter.exceptions.IxmlException;

/**
 * Utility methods for tokens and names.
 */
public class TokenUtils {
    public static int convertHex(String hex) {
        try {
            long cp = Long.parseLong(hex, 16);
            if (cp > 0xffffffffL) {
                throw IxmlException.invalidHexTooLarge(hex);
            }

            // Noncharacters
            if ((cp & 0xfffe) == 0xfffe || (cp & 0xffff) == 0xffff || (cp >= 0xfdd0 && cp <= 0xfdef)) {
                throw IxmlException.invalidHex(hex);
            }

            // Surrogates
            if ((cp >= 0xd800 && cp <= 0xdbff) || (cp >= 0xdc00 && cp <= 0xdfff)) {
                throw IxmlException.invalidHex(hex);
            }

            return (int) cp;
        } catch (NumberFormatException ex) {
            if (hex.matches("^[0-9A-Fa-f]+$")) {
                throw IxmlException.invalidHexTooLarge(hex);
            } else {
                throw IxmlException.invalidHexCharacters(hex);
            }
        }
    }

    /**
     * Is this a valid XML name?
     * <p>Let's take the pedantic position that the valid name characters are the ones
     * in the Fifth Edition. That's not what all parsers actually implement, but
     * [expletive deleted] that.</p>
     * @param name the name
     * @throws NullPointerException if the name is null
     * @throws IxmlException if the name is invalid
     */
    public static void assertXmlName(String name) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }

        if ("".equals(name) || name.charAt(0) == ':') {
            throw IxmlException.invalidXmlName(name);
        }

        boolean first = true;
        boolean colon = false;
        for (int ch : name.codePoints().toArray()) {
            if (ch == ':') {
                if (colon) {
                    throw IxmlException.invalidXmlNameCharacter(name, ch);
                }
                colon = true;
            } else {
                if ((first && !nameStartChar(ch)) || !nameChar(ch)) {
                    throw IxmlException.invalidXmlNameCharacter(name, ch);
                }
            }
            first = false;
        }
    }

    public static boolean nameStartChar(int ch) {
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

    public static boolean nameChar(int ch) {
        // [4a]	NameChar ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
        return nameStartChar(ch)
                || ch == '-'
                || ch == '.'
                || (ch >= '0' && ch <= '9')
                || ch == 0xB7
                || (ch >= 0x0300 && ch <= 0x036F)
                || (ch >= 0x2034 && ch <= 0x2040);
    }

    // Not really about tokens, but this seems like the right place.
    public static void assertXmlChars(String text) {
        if (text == null) {
            throw new NullPointerException("Text cannot be null");
        }
        for (int ch : text.codePoints().toArray()) {
            assertXmlChars(ch);
        }
    }

    // Not really about tokens, but this seems like the right place.
    public static boolean xmlChar(int codepoint) {
        return (codepoint == 0x9 || codepoint == 0xA || codepoint == 0xD || (codepoint >= ' ' && codepoint <= 0xD7FF)
                || (codepoint >= 0xE000 & codepoint <= 0xFFFD)
                || (codepoint >= 0x10000 && codepoint <= 0x10FFFF));
    }

    public static void assertXmlChars(int codepoint) {
        if (!xmlChar(codepoint)) {
            throw IxmlException.invalidXmlCharacter(""+codepoint);
        }
    }
}
