package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.util.TokenUtils;

import static junit.framework.TestCase.fail;

public class ErrorTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void invalidXmlNames() {
        try {
            TokenUtils.assertXmlName("test");
            TokenUtils.assertXmlName("x:y");
            TokenUtils.assertXmlName("_-.·‿⁀");
        } catch (IxmlException ex) {
            fail();

        }

        try {
            TokenUtils.assertXmlName("3a");
            fail();
        } catch (IxmlException ex) {
            // ok
        }

        try {
            // ixml allows all of [L] as name start characters; this is broader than XML
            TokenUtils.assertXmlName("\u00AA"); // "ª" the feminine ordinal
            fail();
        } catch (IxmlException ex) {
            // ok
        }
    }

    @Test
    public void invalidXmlName() {
        String grammar = "\u00AA: 'a' .";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar);

        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);

        Assert.assertTrue(doc.succeeded());

        try {
            doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D03", ex.getCode());
        }

        try {
            doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D03", ex.getCode());
        }
    }

    @Test
    public void validJsonName() {
        String grammar = "\u00AA: 'a' .";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar);

        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);

        Assert.assertTrue(doc.succeeded());

        try {
            ParserOptions options = new ParserOptions();
            options.setAssertValidXmlNames(false);
            DataTreeBuilder builder = new DataTreeBuilder(options);
            doc.getTree(builder, options);
            String json = builder.getTree().asJSON();
            Assert.assertEquals("{\"ª\":\"a\"}", json);
        } catch (IxmlException ex) {
            fail();
        }
    }

    @Test
    public void repeatedAttribute() {
        String input = "S: @a, @a . a: 'a'; 'b' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());

        InvisibleXmlDocument doc = parser.parse("aa");

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D02", ex.getCode());
        }
    }

    @Test
    public void badCharacterClass() {
        String input = "S: [Xq] .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertFalse(parser.constructed());
        Assert.assertTrue(parser.getException() instanceof IxmlException);
        Assert.assertEquals("S10", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void redefinedNonterminal() {
        String input = "date: ['0123'], ['0'-'9'] .\n" +
                "date: 'January' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S03", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void badHex() {
        String input = "date: [#00decafbad00badbadbad] .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S07", ((IxmlException) parser.getException()).getCode());

        input = "date: [#ffffffff0] .";

        parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S07", ((IxmlException) parser.getException()).getCode());

        input = "date: [#fffe] .";

        parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S08", ((IxmlException) parser.getException()).getCode());

        input = "date: [#1fffe] .";

        parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S08", ((IxmlException) parser.getException()).getCode());

        input = "date: [#d801] .";

        parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S08", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void badUnicodeClass() {
        String input = "s: [Xq] .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S10", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void rootAttribute() {
        String input = "@s: 'a' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D05", ex.getCode());
        }
    }

    @Test
    public void alsoRootAttribute() {
        String input = "-s: t. @t: 'a' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D05", ex.getCode());
        }
    }

    @Test
    public void multipleRoots() {
        String input = "-s: t, u. t: 't' . u: 'u' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("tu");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("D06", ex.getCode());
        }
    }

    @Test
    public void emptyOutput() {
        String input = "s: -'a' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        String tree = doc.getTree();
        Assert.assertEquals("<s/>", tree);
    }

    @Test
    public void invalidRange() {
        String input = "s: ['Z'-'A'] .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("S09", ((IxmlException) parser.getException()).getCode());
    }

}
