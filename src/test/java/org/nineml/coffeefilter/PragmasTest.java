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
            InvisibleXmlDocument doc = parser.parse("b");
            String xml = doc.getTree();
            Assertions.assertEquals("<S><A>a'c</A></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardEmpty() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser();
            InvisibleXmlDocument doc = parser.parse(new File("src/main/resources/org/nineml/coffeefilter/pragmas.ixml"));
            String xml = doc.getTree();
            Assertions.assertFalse(xml.contains("<prolog"));
            //xml = parser.getCompiledParser();
            //System.out.println(xml);
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
