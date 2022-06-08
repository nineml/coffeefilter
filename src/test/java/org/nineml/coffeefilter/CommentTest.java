package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class CommentTest {
    private InvisibleXml invisibleXml;
    private ParserOptions options;

    @Before
    public void setup() {
        options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void comments() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/comments.ixml"));
            String input = "hello";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Ignore
    public void longComments1() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/long-comments1.ixml"));
            System.err.println(parser.getParseTime());
            String input = "ab";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Ignore
    public void longComments2() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/long-comments2.ixml"));
            System.err.println(parser.getParseTime());
            String input = "ab";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
}
