package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.Ambiguity;

import java.io.File;

import static org.junit.Assert.fail;

public class PragmasTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void renameNonterminal() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/two-dates.ixml"));
            InvisibleXmlDocument doc = parser.parse("1999-12-31");
            String xml = doc.getTree();
            Assertions.assertEquals("<input><year>1999</year><month>12</month><day>31</day></input>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void renameTerminal() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/two-dates.ixml"));
            InvisibleXmlDocument doc = parser.parse("12 February 2022");
            String xml = doc.getTree();
            Assertions.assertEquals("<input month=\"Febtacular\"><day>12</day><year>2022</year></input>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void defaultNamespace() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/xmlns.ixml"));
            InvisibleXmlDocument doc = parser.parse("2022-03-01");
            String xml = doc.getTree();
            Assertions.assertEquals("<date xmlns=\"http://example.com/\"><year>2022</year><month>03</month><day>01</day></date>", xml);
        } catch (Exception ex) {
            fail();
        }
    }
}
