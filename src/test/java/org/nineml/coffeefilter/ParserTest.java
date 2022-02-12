package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.DocumentBuilder;
import org.junit.Assert;
import org.junit.Test;

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

        InvisibleXmlParser parser = InvisibleXml.parserFromString(input);

        System.out.println(parser.getCompiledParser());

        input = "16 June '67";
        InvisibleXmlDocument doc = parser.parse(input);

        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();

        try {
            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();
            System.out.println(node);
        } catch (SaxonApiException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void hash() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("ixml/tests/correct/hash.ixml");

            String input = "#12.";
            InvisibleXmlDocument doc = parser.parse(input);

            Processor processor = new Processor(false);
            DocumentBuilder builder = processor.newDocumentBuilder();

            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();
            System.out.println(node);

        } catch (Exception ex) {
            System.err.println(ex);
            fail();
        }
    }

    @Test
    public void vcard() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("ixml/tests/correct/vcard.ixml");

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
            System.out.println(node);

        } catch (Exception ex) {
            System.err.println(ex);
            fail();
        }
    }

    @Test
    public void css() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("ixml/tests/ambiguous/css.ixml");
            System.out.println(parser.getCompiledParser());
            String input = "body { color: blue;}";

            InvisibleXmlDocument doc = parser.parse(input);

            //doc.getEarleyResult().getForest().parse().serialize("css.xml");

            System.out.println(doc.getTree());

        } catch (Exception ex) {
            System.err.println(ex);
            fail();
        }
    }

    @Test
    public void ambig2() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("ixml/tests/ambiguous/ambig2.ixml");

            String input = "";

            InvisibleXmlDocument doc = parser.parse(input);

            //doc.getEarleyResult().getForest().parse().serialize("css.xml");

            System.out.println(doc.getTree());

        } catch (Exception ex) {
            System.err.println(ex);
            fail();
        }
    }

    @Test
    public void comments() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("src/test/resources/comments.ixml");
            String input = "hello";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
