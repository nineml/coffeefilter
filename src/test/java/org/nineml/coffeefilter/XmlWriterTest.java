package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeefilter.exceptions.IxmlTreeException;
import org.nineml.coffeefilter.util.XmlStringWriter;

import static org.junit.Assert.fail;

public class XmlWriterTest {

    @Test
    public void testElement() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("doc");
        writer.endElement();
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<doc></doc>\n", xml);
    }

    @Test
    public void testNestedElements() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("div");
        writer.startElement("a");
        writer.startElement("b");
        writer.text("Spoon!");
        writer.endElement("b");
        writer.endElement();
        writer.endElement("div");

        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<div><a><b>Spoon!</b></a></div>\n", xml);
    }

    @Test
    public void testComment() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("div");
        writer.comment(" Spoon! ");
        writer.endElement("div");

        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<div><!-- Spoon! --></div>\n", xml);
    }

    @Test
    public void testPI() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("div");
        writer.processingInstruction("piname");
        writer.endElement("div");

        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<div><?piname?></div>\n", xml);
    }

    @Test
    public void testPIdata() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("div");
        writer.processingInstruction("piname", "a=\"a\" b='b'");
        writer.endElement("div");

        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<div><?piname a=\"a\" b='b'?></div>\n", xml);
    }

    @Test
    public void testText() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startDocument();
        writer.startElement("div");
        writer.text("<test>");
        writer.endElement("div");

        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<div>&lt;test&gt;</div>\n", xml);
    }

    @Test
    public void testAttributes() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.startElement("doc");
        writer.addAttribute("first", "1");
        writer.addAttribute("second", "2");
        writer.endElement();
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<doc first='1' second='2'></doc>\n", xml);
    }

    @Test
    public void testNamespaces() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.declareNamespace("one", "http://example.com/one");
        writer.declareNamespace("two", "http://example.com/two");
        writer.startElement("one:doc");
        writer.addAttribute("two:first", "1");
        writer.text("");
        writer.endElement();
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<one:doc xmlns:one='http://example.com/one' xmlns:two='http://example.com/two' two:first='1'></one:doc>\n", xml);
    }

    @Test
    public void testDefaultNamespace() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.declareNamespace("", "http://example.com/one");
        writer.startElement("doc");
        writer.endElement();
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<doc xmlns='http://example.com/one'></doc>\n", xml);
    }

    @Test
    public void testNestedNamespace() {
        XmlStringWriter writer = new XmlStringWriter();
        writer.declareNamespace("", "http://example.com/one");
        writer.startElement("doc");
        writer.declareNamespace("test", "http://example.com/test");
        writer.startElement("test:test");
        writer.endElement();
        writer.endElement();
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<doc xmlns='http://example.com/one'><test:test xmlns:test='http://example.com/test'></test:test></doc>\n", xml);
    }

    @Test
    public void testNestedNamespaceRedeclared() {
        XmlStringWriter writer = new XmlStringWriter();
        Assertions.assertEquals(0, writer.getInScopeNamespaces().size());
        writer.declareNamespace("", "http://example.com/one");
        Assertions.assertEquals(1, writer.getInScopeNamespaces().size());
        writer.startElement("doc");
        writer.declareNamespace("test", "http://example.com/test1");
        Assertions.assertEquals(2, writer.getInScopeNamespaces().size());
        writer.startElement("test:test");
        writer.endElement();
        writer.declareNamespace("test", "http://example.com/test2");
        Assertions.assertEquals(2, writer.getInScopeNamespaces().size());
        writer.startElement("test:test");
        writer.endElement();
        writer.endElement();
        Assertions.assertEquals(0, writer.getInScopeNamespaces().size());
        writer.endDocument();
        String xml = writer.getXml();
        Assertions.assertEquals("<doc xmlns='http://example.com/one'><test:test xmlns:test='http://example.com/test1'></test:test><test:test xmlns:test='http://example.com/test2'></test:test></doc>\n", xml);
    }

    @Test
    public void testXB01() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startDocument();
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB01", ex.getCode());
        }
    }

    @Test
    public void testXB02() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.endDocument();
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB02", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startElement("doc");
            writer.endElement();
            writer.endDocument();
            writer.endDocument();
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB02", ex.getCode());
        }
    }

    @Test
    public void testXB04() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.declareNamespace("prefix", "https://example.com/");
            writer.declareNamespace("prefix", "https://example.com/");
            writer.startElement("doc");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB04", ex.getCode());
        }
    }

    @Test
    public void testXB05() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.addAttribute("test", "thing");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB05", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.endElement();
            writer.addAttribute("test", "thing");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB05", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.text("text");
            writer.addAttribute("test", "thing");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB05", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.comment("comment");
            writer.addAttribute("test", "thing");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB05", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.processingInstruction("name");
            writer.addAttribute("test", "thing");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB05", ex.getCode());
        }
    }

    @Test
    public void testXB06() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.addAttribute("test", "this");
            writer.addAttribute("test", "this");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB06", ex.getCode());
        }
    }

    @Test
    public void testXB07() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.endElement();
            writer.endElement("other");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB07", ex.getCode());
        }
    }

    @Test
    public void testXB15() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.endElement();
            writer.endElement();
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB15", ex.getCode());
        }
    }

    @Test
    public void testXB08() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.endElement("other");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB08", ex.getCode());
        }
    }

    // I don't think XB09 can actually occur...

    @Test
    public void testXB10() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.comment("comment");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB10", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.endDocument();
            writer.comment("comment");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB10", ex.getCode());
        }
    }

    @Test
    public void testXB11() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.processingInstruction("pi");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB11", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.endDocument();
            writer.processingInstruction("pi");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB11", ex.getCode());
        }
    }

    @Test
    public void testXB12() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.text("text");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB12", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.endDocument();
            writer.text("text");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB12", ex.getCode());
        }
    }

    @Test
    public void testXB13() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB13", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("13");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB13", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("test's");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB13", ex.getCode());
        }
    }

    @Test
    public void testXB14() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.declareNamespace("13", "http://foo");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB14", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.declareNamespace("test's", "http://foo");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB14", ex.getCode());
        }
    }

    @Test
    public void testXB16() {
        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.text("\u0000");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB16", ex.getCode());
        }

        try {
            XmlStringWriter writer = new XmlStringWriter();
            writer.startDocument();
            writer.startElement("doc");
            writer.text("\u0007");
            fail();
        } catch (IxmlTreeException ex) {
            Assertions.assertEquals("XB16", ex.getCode());
        }
    }
}
