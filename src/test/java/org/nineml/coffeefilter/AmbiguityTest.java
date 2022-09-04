package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;

import static org.junit.Assert.fail;

public class AmbiguityTest {
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
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "{[+pragma n \"https://nineml.org/ns/pragma/\"]} S = 'x', (A | {[n priority 2]} B), 'y'.  {[n priority 1]} A = 'a' | B. B = 'b' | A.";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = "xay";
        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            //System.out.println(xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void aplus() {
        String input = "S = s, alts. alts = alt++(';', s). alt = term**(',', s). term = ('a' ; 'b'), s. -s = (-' '|comment)*. comment = -'{', ~[{}]*, -'}'.";


        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = " a;{comment} b";

        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            //System.out.println(xml);
        } catch (Exception ex) {
            fail();
        }
    }

}
