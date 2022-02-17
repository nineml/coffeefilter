package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.DocumentBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class ParserTest {

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

        input = "16 June '67";
        InvisibleXmlDocument doc = parser.parse(input);

        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();

        try {
            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            Assert.assertEquals("16June67", node.getStringValue());

            String str = node.toString();
            Assert.assertTrue(str.contains("<date>"));
            Assert.assertTrue(str.contains("<day>16</day>"));
            Assert.assertTrue(str.contains("<month>June</month>"));
            Assert.assertTrue(str.contains("<year>67</year>"));
        } catch (SaxonApiException ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void hash() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("ixml/tests/correct/hash.ixml"));

            String input = "#12.";
            InvisibleXmlDocument doc = parser.parse(input);

            Processor processor = new Processor(false);
            DocumentBuilder builder = processor.newDocumentBuilder();

            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            Assert.assertEquals("#.", node.getStringValue());

            String str = node.toString();
            Assert.assertTrue(str.contains("<hash d6=\"12\">"));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void vcard() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("ixml/tests/correct/vcard.ixml"));

            String input = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "N:Lastname;Surname\n" +
                    "FN:Displayname\n" +
                    "ORG:EVenX\n" +
                    "URL:http://www.evenx.com/\n" +
                    "EMAIL:info@evenx.com\n" +
                    "TEL;TYPE=voice,work,pref:+49 1234 56788\n" +
                    "ADR;TYPE=intl,work,postal,parcel:;;Wallstr. 1;Berlin;;12345;Germany\n" +
                    "END:VCARD\n";

            InvisibleXmlDocument doc = parser.parse(input);

            Processor processor = new Processor(false);
            DocumentBuilder builder = processor.newDocumentBuilder();

            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();
            Assert.assertEquals("", node.getStringValue());

            String str = node.toString();

            Assert.assertTrue(str.contains("<property name=\"ORG\">"));      // random spotchecks
            Assert.assertTrue(str.contains("<attribute value=\"EVenX\"/>"));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void css() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("ixml/tests/ambiguous/css.ixml"));
            String input = "body { color: blue;}";

            InvisibleXmlDocument doc = parser.parse(input);

            String str = doc.getTree();
            Assert.assertEquals("<css><rule><selector><name>body</name></selector><block><property name=\"color\"><value name=\"blue\"/></property><property/></block></rule></css>",
                    str);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void ambig2() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("ixml/tests/ambiguous/ambig2.ixml"));
            String input = "";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertEquals("<a/>", doc.getTree());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void comments() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/test/resources/comments.ixml"));
            String input = "hello";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
}
