package org.nineml.coffeefilter;

import com.saxonica.ee.schema.Assertion;
import net.sf.saxon.s9api.*;
import net.sf.saxon.s9api.DocumentBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class ParserTest {
    private InvisibleXml invisibleXml;

    @Before
    public void setup() {
        ParserOptions options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void parseDate() {
        String input = "date: s?, day, -s, month, (-s, year)? .\n" +
                "-s: -' '+ .\n" +
                "day: digit, digit? .\n" +
                "-digit: '0'; '1'; '2'; '3'; '4'; '5'; '6'; '7'; '8'; '9'.\n" +
                "month: 'January'; 'February'; 'March'; 'April';\n" +
                "       'May'; 'June'; 'July'; 'August';\n" +
                "       'September'; 'October'; 'November'; 'December'.\n" +
                "year: ((digit, digit); -\"'\")?, digit, digit .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "16 June '67";
        InvisibleXmlDocument doc = parser.parse(input);

        //doc.getEarleyResult().getForest().serialize("date.xml");

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
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/tests/correct/hash.ixml"));

            String input = "#12.";
            InvisibleXmlDocument doc = parser.parse(input);

            Processor processor = new Processor(false);
            DocumentBuilder builder = processor.newDocumentBuilder();

            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            String str = node.toString();
            Assert.assertEquals("#.", node.getStringValue());

            Assert.assertTrue(str.contains("<hash d6='12'>") || str.contains("<hash d6=\"12\">"));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void vcard() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/tests/correct/vcard.ixml"));

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

            Assert.assertTrue(str.contains("<property name='ORG'>") || str.contains("<property name=\"ORG\">"));      // random spotchecks
            Assert.assertTrue(str.contains("<attribute value='EVenX'/>") || str.contains("<attribute value=\"EVenX\"/>"));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void css() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/tests/ambiguous/css.ixml"));
            String input = "body { color: blue;}";

            InvisibleXmlDocument doc = parser.parse(input);

            String str = doc.getTree();
            Assert.assertEquals("<css><rule><selector><name>body</name></selector><block><property name='color'><value name='blue'/></property><property/></block></rule></css>",
                    str);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void ambig2() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/tests/ambiguous/ambig2.ixml"));
            String input = "";
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assert.assertTrue(xml.startsWith("<a"));
            Assert.assertTrue(xml.endsWith("/>"));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void ambig3() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a', sep, sep, sep, sep, sep, 'b' .\n" +
                "sep: ['.';',']? .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "a.b";
        InvisibleXmlDocument doc = parser.parse(input);
        String xml = doc.getTree();

        Assertions.assertTrue(xml.startsWith("<S xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'>a<sep"));
        Assertions.assertTrue(xml.contains("<sep>.</sep>") || xml.contains("<sep>,</sep>"));
        Assertions.assertTrue(xml.endsWith(">b</S>"));
    }

    @Test
    public void versionDeclaration() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "ixml version '1.0-nineml'. S: 'a', 'b' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        Assertions.assertEquals("1.0-nineml", parser.getIxmlVersion());

        input = "ab";
        InvisibleXmlDocument doc = parser.parse(input);

        Assertions.assertEquals("<S>ab</S>",
                doc.getTree());
    }

    @Test
    public void balisageExample() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        String grammar = "S = A|B. A = 'a'. B = 'b'.";
        InvisibleXmlParser parser = ixml.getParserFromIxml(grammar);

        String input = "b";
        InvisibleXmlDocument document = parser.parse(input);

        String xml = document.getTree();

        //System.out.println(xml);

        Assertions.assertEquals("<S><B>b</B></S>", xml);
    }

    @Test
    public void simpleGrammar54bis() {
        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/sg54bis.ixml"));
            String input = "32.5e+1";
            InvisibleXmlDocument document = parser.parse(input);
            String xml = document.getTree();
            Assertions.assertEquals("<Number><Integer>32</Integer><Fraction>.5</Fraction><Scale1>e<Sign>+</Sign><Integer>1</Integer></Scale1></Number>", xml);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void insertMultipleAttribute() {
        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");
        InvisibleXml ixml = new InvisibleXml(options);

        InvisibleXmlParser parser = ixml.getParserFromIxml("S: 'a', b, @b, b. b: +'xml'.");
        //System.err.println(parser.getCompiledParser());
        String input = "a";
        InvisibleXmlDocument document = parser.parse(input);
        String xml = document.getTree();
        //System.out.println(xml);
        Assertions.assertEquals("<S b='xml'>a<b>xml</b><b>xml</b></S>", xml);
    }

    @Test
    public void ignoreBomWhenPresentInBoth() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(true);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-with-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-with-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void ignoreBomWhenPresentInGrammar() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(true);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-with-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-without-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void ignoreBomWhenPresentInInput() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(true);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-without-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-with-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void ignoreBomWhenPresentInNeither() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(true);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-without-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-without-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void dontIgnoreBomWhenPresentInGrammar() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(false);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-with-bom.ixml"));
            Assertions.assertFalse(parser.constructed());
            InvisibleXmlDocument document = parser.getFailedParse();
            String xml = document.getTree();
            Assertions.assertTrue(xml.startsWith("<fail"));
            Assertions.assertTrue(xml.contains("#FEFF"));
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void dontIgnoreBomWhenPresentInInput() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(false);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-without-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-with-bom.txt"));
            String xml = document.getTree();
            Assertions.assertTrue(xml.startsWith("<fail"));
            Assertions.assertTrue(xml.contains("#FEFF"));
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void dontIgnoreBomWhenPresentInNeither() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(false);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-without-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-without-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void dontIgnoreBomWhenExplicitlyInGrammar() {
        ParserOptions options = new ParserOptions();
        options.setIgnoreBOM(false);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/bom-test-grammar-with-explicit-bom.ixml"));
            InvisibleXmlDocument document = parser.parse(new File("src/test/resources/bom-test-with-bom.txt"));
            String xml = document.getTree();
            Assertions.assertEquals("<S><BOM/>bom test</S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void encodeCR() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/cr.ixml"));
            InvisibleXmlDocument document = parser.parse("abc");
            String xml = document.getTree();
            Assertions.assertEquals("<S><a>a&#xD;</a><b x='x&#xD;z'>b</b><c>c</c></S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void encodeCRLF() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/crlf.ixml"));
            InvisibleXmlDocument document = parser.parse("abc");
            String xml = document.getTree();
            Assertions.assertEquals("<S><a>a&#xD;\n</a><b x='x&#xD;&#xA;z'>b</b><c>c</c></S>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void encodeCRLF2() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/crlf2.ixml"));
            InvisibleXmlDocument document = parser.parse("abcd");
            String xml = document.getTree();
            Assertions.assertEquals("<S><a>a&#xD;\n</a><b x='x&#xD;&#xA;z'>b&#xD;b</b><c y='a&#xD;g'>c</c><d>&#xD;</d></S>", xml);
        } catch (IOException err) {
            fail();
        }
    }
}
