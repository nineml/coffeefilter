package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class ParserFailTest {

    @Test
    public void parseDate() {
        String input = "date: s?, day, -s, month, (-s, year)? .\n" +
                "-s: -\" \"+ .\n" +
                "day: digit, digit? .\n" +
                "-digit: \"0\"; \"1\"; \"2\"; \"3\"; \"4\"; \"5\"; \"6\"; \"7\"; \"8\"; \"9\".\n" +
                "month: \"January\"; \"February\"; \"March\"; \"April\";\n" +
                "       \"May\"; \"June\"; \"July\"; \"August\";\n" +
                "       \"September\"; \"October\"; \"November\"; \"December\".\n" +
                "year: ((digit, digit); -\"'\")?, digit, digit .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);

        input = "16 Jinglebells 1992";
        InvisibleXmlDocument doc = parser.parse(input);

        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();

        try {
            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            String str = node.toString();
            Assert.assertTrue(str.contains("<line>1</line>"));
            Assert.assertTrue(str.contains("<column>5</column>"));
            Assert.assertTrue(str.contains("<pos>4</pos>"));
            Assert.assertTrue(str.contains("<unexpected>J</unexpected>"));
            Assert.assertTrue(str.contains("<permitted>' ', 'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</permitted>"));

            //System.out.println(node);
        } catch (SaxonApiException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
