package org.nineml.coffeefilter;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.Ambiguity;
import org.nineml.coffeegrinder.parser.EarleyItem;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.io.File;

import static org.junit.Assert.fail;

public class PragmasTest {
    private ParserOptions options;
    private InvisibleXml invisibleXml;

    @Before
    public void setup() {
        options = new ParserOptions();
        //options.getLogger().setDefaultLogLevel("debug");
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void renameNonterminal() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
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

    @Test
    public void anotherTest() {
        try {
            // FIXME: test that the syntax error is reported
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/malformed-test.ixml"));
            InvisibleXmlDocument doc;
            String xml;
            /*
            doc = invisibleXml.getParser().parse(new File("src/test/resources/malformed-test.ixml"));
            xml = doc.getTree();
            System.err.println(xml);
             */
            doc = parser.parse("b");
            xml = doc.getTree();
            Assertions.assertEquals("<S><A>a'c</A></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardEmpty() {
        try {
            String grammar1 = "S = A, B, C. A = 'a'. B='b'?. C='c'.";
            String grammar2 = "{[+ixmlns:n \"https://nineml.org/ns\"]} S = A, {[n:discard empty]} B, C. A = 'a'. B='b'?. C='c'.";
            String input = "ac";

            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar1);
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<B"));

            parser = invisibleXml.getParserFromIxml(grammar2);
            doc = parser.parse(input);
            xml = doc.getTree();
            Assertions.assertFalse(xml.contains("<B"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardNone() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("abcde");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D=\"d\" E=\"e\"><A>a</A><B>b</B><C>c</C></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardA() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("cde");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D=\"d\" E=\"e\"><B/><C>c</C></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardACD() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("");
            String xml = doc.getTree();
            Assertions.assertEquals("<S E=\"\"><B/></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    /*
    @Test
    public void unicodeData() {
        try {
            ParserOptions options = new ParserOptions();
            options.setPrettyPrint(true);
            invisibleXml = new InvisibleXml(options);
            InvisibleXmlParser parser = invisibleXml.getParser(new File("../pot/scraps/chars/unicode.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("../pot/scraps/chars/MediumData.txt"));
            //String xml = doc.getTree();
            System.out.println(doc.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }
     */
}
