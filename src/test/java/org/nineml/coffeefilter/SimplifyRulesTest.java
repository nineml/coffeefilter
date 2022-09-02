package org.nineml.coffeefilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.fail;

public class SimplifyRulesTest {
    private InvisibleXml invisibleXml;

    @Before
    public void setup() {
        ParserOptions options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void testSimplifyRep0Sep() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'**',' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        System.err.println(parser);
    }

    @Test
    public void testSimplifyRepeat0() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'* .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        System.err.println(parser);

    }

    @Test
    public void testSimplifyRepeat1() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: f+ | g+. f: 'a'. g: 'a'.";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        InvisibleXmlDocument doc = parser.parse("a");
        if (doc.succeeded()) {
            String tree = doc.getTree();
            System.err.println(tree);
        } else {
            fail();
        }
    }

}
