package org.nineml.coffeefilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class PriorityTest {
    private InvisibleXml invisibleXml;
    private ParserOptions options;

    @Before
    public void setup() {
        options = new ParserOptions();
        options.setPedantic(false);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void ambiguity1() {
        try {
            // The default priority of C is highest
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/prio1.ixml"));
            String input = "xay";
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("x<C>a</C>y"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ambiguity2() {
        try {
            // The explicit priority of B is highest
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/prio2.ixml"));
            String input = "xay";
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("x<B>a</B>y"));
        } catch (Exception ex) {
            fail();
        }
    }

}
