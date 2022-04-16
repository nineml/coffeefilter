package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class InsertionsTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void parseText() {
        String input = "demo: A, ^'between', B, C, D. A: 'a', ^'FromA'. B: ^'FromB', 'b'. @C: ^'attrvalue'. D: ^'gentext'.";

        InvisibleXmlDocument xdoc = invisibleXml.getParser().parse(input);
        String xxml = xdoc.getTree();

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "ab";
        InvisibleXmlDocument doc = parser.parse(input);
        String xml = doc.getTree();

        Assertions.assertEquals("<demo C=\"attrvalue\"><A>aFromA</A>between<B>FromBb</B><D>gentext</D></demo>", xml);
    }

    @Test
    public void parseData() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/insdata.ixml"));
            InvisibleXmlDocument doc = parser.parse("100,200,(300),400");
            String xml = doc.getTree();
            Assertions.assertEquals("<data xmlns=\"http://example.com/data\"><value>+100</value><value>+200</value><value>-300</value><value>+400</value></data>", xml);
            System.err.println(xml);
        } catch (IOException ex) {
            fail();
        }
    }
}