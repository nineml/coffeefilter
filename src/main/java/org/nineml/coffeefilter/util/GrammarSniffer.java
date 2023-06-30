package org.nineml.coffeefilter.util;

public class GrammarSniffer {
    public static final int UNK_SOURCE = -1;
    public static final int IXML_SOURCE = 0;
    public static final int VXML_SOURCE = 1;

    public static int identify(byte[] buf, int offset, int count) {
        boolean couldBeIxml = true;

        int pos = offset;
        while (pos < count) {
            if (Character.isWhitespace(buf[pos])) {
                pos++;
                continue;
            }

            if (lookingAt(buf, pos, count, "<!--")) {
                couldBeIxml = false;
                pos += 4;
                while (pos < count) {
                    pos = skipTo(buf, pos, count, '-');
                    if (lookingAt(buf, pos, count, "-->")) {
                        pos += 3;
                        break;
                    }
                    pos++;
                }
                continue;
            }

            if (lookingAt(buf, pos, count, "<?")) {
                couldBeIxml = false;
                pos += 2;
                while (pos < count) {
                    pos = skipTo(buf, pos, count, '?');
                    if (lookingAt(buf, pos, count, "?>")) {
                        pos += 2;
                        break;
                    }
                    pos++;
                }
                continue;
            }

            if (buf[pos] == '<') {
                if (lookingAt(buf, pos, count, "<ixml")) {
                    return VXML_SOURCE;
                }
                return UNK_SOURCE;
            } else {
                if (couldBeIxml) {
                    return IXML_SOURCE;
                }
                return UNK_SOURCE;
            }
        }

        return UNK_SOURCE;
    }

    private static int skipTo(byte[] buf, int pos, int count, char seek) {
        while (pos < count) {
            if (buf[pos] == seek) {
                return pos;
            }
            pos++;
        }
        return pos;
    }

    private static boolean lookingAt(byte[] buf, int pos, int len, String target) {
        if (pos + target.length() >= len) {
            return false;
        }

        String peek = new String(buf, pos, target.length());
        return target.equals(peek);
    }
}
