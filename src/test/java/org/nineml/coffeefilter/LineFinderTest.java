package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;

public class LineFinderTest {
    @Test
    public void findLine1() {
        String input = "0123456789";
        LineFinder finder = new LineFinder(input);
        Assert.assertEquals("0123456789", finder.line);
        Assert.assertEquals(0, finder.start);
    }

    @Test
    public void findLine2() {
        String input = "0123456789\r\nabcdefghij";
        LineFinder finder = new LineFinder(input);
        finder.nextLine();
        Assert.assertEquals("abcdefghij", finder.line);
        Assert.assertEquals(12, finder.start);
    }

    @Test
    public void findLine3() {
        String input = "0123456789\r\nabcdefghij";
        LineFinder finder = new LineFinder(input);
        finder.nextLine();
        finder.nextLine();
        Assert.assertEquals("", finder.line);
        Assert.assertEquals(22, finder.start);
    }

    @Test
    public void findLine4() {
        String input = "0123456789\r\nabcdefghij";
        LineFinder finder = new LineFinder(input);
        finder.nextLine();
        finder.nextLine();
        finder.nextLine();
        Assert.assertEquals("", finder.line);
        Assert.assertEquals(22, finder.start);
    }

    // N.B. This class is cut-and-pasted into InvisibleXmlDocument. It's just copied
    // here for testing.
    private static class LineFinder {
        private final String input;
        public int start = 0;
        public int next = 0;
        public int last = 0;
        public String line = null;

        public LineFinder(String input) {
            this.input = input + "\n";
            last = input.length();
            if ("".equals(input)) {
                next = -1;
            } else {
                nextLine();
            }
        }

        public void nextLine() {
            start = next;
            char ch = input.charAt(next);
            while ((next < last) && ch != '\n' && ch != '\r') {
                next++;
                ch = input.charAt(next);
            }
            if (next <= last) {
                line = input.substring(start, next);
            } else {
                line = input.substring(start);
            }
            while ((next < last) && (ch == '\n' || ch == '\r')) {
                next++;
                ch = input.charAt(next);
            }
        }
    }
}
