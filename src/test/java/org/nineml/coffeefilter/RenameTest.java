package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;

public class RenameTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void renameAtoB_10() {
        String ixml = "S: A .\n" +
                "A>B: 'b' .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        Assert.assertFalse(parser.constructed());
    }

    @Test
    public void renameAtoB_11() {
        String ixml = "ixml version '1.1-nineml' .\n" +
                "S: A .\n" +
                "A>B: 'b' .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc;
        String input = "b";
        doc = parser.parse(input);
        Assert.assertTrue(doc.succeeded());
        Assert.assertEquals("<S><B>b</B></S>", doc.getTree());
    }

    @Test
    public void renameAtoBCD_11() {
        String ixml = "ixml version '1.1-nineml' .\n" +
                "S: @A, B>C, C>D, A, B, @C .\n" +
                "A>B: 'b' .\n"+
                "B: 'c' .\n"+
                "C: 'd' .\n";
        InvisibleXmlDocument doc; //= invisibleXml.getParser().parse(ixml);
        //System.err.println(doc.getTree());
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        String input = "bcdbcd";
        doc = parser.parse(input);
        Assert.assertTrue(doc.succeeded());
        //System.err.println(doc.getTree());
        Assert.assertEquals("<S B='b' C='d'><C>c</C><D>d</D><B>b</B><B>c</B></S>", doc.getTree());
    }

    @Test
    public void renameAtoBCD_pragma_simple() {
        String ixml = "ixml version '1.1-nineml' ." +
                "{[+pragma n 'https://nineml.org/ns/pragma/']}\n" +
                "S: X, {[n rename Y]} B, C .\n" +
                "X: 'x' .\n"+
                "B: 'y' .\n"+
                "{[n rename Z]} C: 'z' .\n";
        InvisibleXmlDocument doc; //= invisibleXml.getParser().parse(ixml);
        //System.err.println(doc.getTree());
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        String input = "xyz";
        doc = parser.parse(input);
        Assert.assertTrue(doc.succeeded());
        //System.err.println(doc.getTree());
        Assert.assertEquals("<S><X>x</X><Y>y</Y><Z>z</Z></S>", doc.getTree());
    }

    @Test
    public void renameAtoBCD_pragma_combined() {
        String ixml = "ixml version '1.1-nineml' ." +
                "{[+pragma n 'https://nineml.org/ns/pragma/']}\n" +
                "S: @A, {[n rename Y]} B>C, C>D, C, {[n rename W]} A, B, @C .\n" +
                "A>B: 'b' .\n"+
                "B: 'c' .\n"+
                "{[n rename Z]} C: 'd' .\n";
        InvisibleXmlDocument doc; //= invisibleXml.getParser().parse(ixml);
        //System.err.println(doc.getTree());
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        String input = "bcddbcd";
        doc = parser.parse(input);
        Assert.assertTrue(doc.succeeded());
        //System.err.println(doc.getTree());
        // N.B. Z in both cases because the pragma always wins.
        Assert.assertEquals("<S B='b' Z='d'><Y>c</Y><Z>d</Z><Z>d</Z><W>b</W><B>c</B></S>", doc.getTree());
    }

    @Test
    public void dupRenameRule() {
        String ixml = "{[+pragma n 'https://nineml.org/ns/pragma/']}\n" +
                "S: A, B, C .\n" +
                "A: 'a' .\n"+
                "{[n rename X]} {[n rename Y]} B: 'b' .\n"+
                "C: 'c' .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        Assert.assertFalse(parser.constructed());
        Exception ex = parser.getException();
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IxmlException);
        IxmlException iex = (IxmlException) ex;
        Assert.assertEquals("E016", iex.getCode());
    }

    @Test
    public void dupRenameSymbol() {
        String ixml = "{[+pragma n 'https://nineml.org/ns/pragma/']}\n" +
                "S: A, {[n rename X]} {[n rename Y]} B, C .\n" +
                "A: 'a' .\n"+
                "B: 'b' .\n"+
                "C: 'c' .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        Assert.assertFalse(parser.constructed());
        Exception ex = parser.getException();
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof IxmlException);
        IxmlException iex = (IxmlException) ex;
        Assert.assertEquals("E016", iex.getCode());
    }

}
