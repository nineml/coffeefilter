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
        String input = "demo: A, +'between', B, C, D. A: 'a', +'FromA'. B: +'FromB', 'b'. @C: +'attrvalue'. D: +'gentext'.";

        InvisibleXmlDocument xdoc = invisibleXml.getParser().parse(input);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "ab";
        InvisibleXmlDocument doc = parser.parse(input);
        String xml = doc.getTree();

        Assertions.assertEquals("<demo C='attrvalue'><A>aFromA</A>between<B>FromBb</B><D>gentext</D></demo>", xml);
    }

    @Test
    public void parseData() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/insdata.ixml"));
            InvisibleXmlDocument doc = parser.parse("100,200,(300),400");
            String xml = doc.getTree();
            Assertions.assertEquals("<data><value>+100</value><value>+200</value><value>-300</value><value>+400</value></data>", xml);
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void ambiguous() {
        String input = " S: +'A' ; +'B'.";

        InvisibleXmlDocument xdoc = invisibleXml.getParser().parse(input);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "";
        InvisibleXmlDocument doc = parser.parse(input);

        //doc.getResult().getForest().serialize("/tmp/insert.xml");

        String xml = doc.getTree();

        Assertions.assertTrue("<S xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'>A</S>".equals(xml)
            || "<S xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'>B</S>".equals(xml));
    }

    @Test
    public void astralPlane() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/astral.ixml"));
            InvisibleXmlDocument doc = parser.parse("aaabgagbfafb");
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<transcode-fraktur>\uD835\uDD1F</transcode-fraktur>"));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            fail();
        }
    }

}
