package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.util.GrammarSniffer;

import java.nio.charset.StandardCharsets;

public class SniffTest {
    @Test
    public void testVxml() {
        String input = "<ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testIxml() {
        String input = "s: not, xml.";
        Assert.assertEquals(GrammarSniffer.IXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipWhitespaceVxml() {
        String input = "      \t\n\n\t    \t\n  <ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipWhitespaceIxml() {
        String input = "      \t\n\n\t    \t\n  s: not, xml.";
        Assert.assertEquals(GrammarSniffer.IXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipCommentVxml() {
        String input = "<!-- comment --><ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipIxmlCommentIxml() {
        String input = "{sure, this is a comment. Not that it matters since it isn't XML}";
        Assert.assertEquals(GrammarSniffer.IXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipXmlCommentIxml() {
        String input = "<!-- bad is about to happen -->s: not, xml, or, ixml.";
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipPiVxml() {
        String input = "<?xml this is the most likely one ?><ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipPiIxml() {
        String input = "<? bad is about to happen ?>s: not, xml, or, ixml.";
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipLotsVxml() {
        String input = "\n<?xml?><!-- comment -->\t\n<!--comment--><!--another--><?pi?><?another?>\n\t<?done?><ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testSkipLotsIxml() {
        String input = "<?xml?><!-- comment -->\t\n<!--comment--><!--another--><?pi?><?another?>\n\t<?done?>s: bad.";
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testUnterminatedComment() {
        String input = "<!-- s: bad.";
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testUnterminatedPi() {
        String input = "<? s: bad.";
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testTooMuchSpace() {
        StringBuilder sb = new StringBuilder();
        for (int count = 0; count < 1024; count++) {
            sb.append("     ");
        }
        Assert.assertEquals(GrammarSniffer.UNK_SOURCE, GrammarSniffer.identify(sb.toString().getBytes(StandardCharsets.UTF_8), 0, sb.length()));
    }

    @Test
    public void testTrickyComment() {
        String input = "<!-- Testing <?pi?> - more - --><ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

    @Test
    public void testTrickyPi() {
        String input = "<?test?thing -- \"?\"?><ixml>";
        Assert.assertEquals(GrammarSniffer.VXML_SOURCE, GrammarSniffer.identify(input.getBytes(StandardCharsets.UTF_8), 0, input.length()));
    }

}
