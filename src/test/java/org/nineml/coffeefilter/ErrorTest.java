package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.utils.TokenUtils;

import static junit.framework.TestCase.fail;

public class ErrorTest {
    @Test
    public void invalidXmlNames() {
        Assert.assertTrue(TokenUtils.xmlName("test"));
        Assert.assertFalse(TokenUtils.xmlName("x:y")); // no qnames allowed
        Assert.assertFalse(TokenUtils.xmlName("3a"));
        Assert.assertTrue(TokenUtils.xmlName("_-.·‿⁀"));
        // ixml allows all of [L] as name start characters; this is broader than XML
        Assert.assertFalse(TokenUtils.xmlName("\u00AA")); // "ª" the feminine ordinal
    }

    @Test
    public void invalidXmlName() {
        String grammar = "\u00AA: 'a' .";
        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(grammar);

        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);

        Assert.assertTrue(doc.succeeded());

        try {
            doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E003", ex.getCode());
        }

        try {
            doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E003", ex.getCode());
        }
    }

    @Test
    public void validJsonName() {
        String grammar = "\u00AA: 'a' .";
        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(grammar);

        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);

        Assert.assertTrue(doc.succeeded());

        try {
            ParserOptions options = new ParserOptions();
            options.assertValidXmlNames = false;
            DataTreeBuilder builder = new DataTreeBuilder(options);
            doc.getTree(builder);
            String json = builder.getTree().asJSON();
            Assert.assertEquals("{\"ª\":\"a\"}", json);
        } catch (IxmlException ex) {
            fail();
        }
    }

    @Test
    public void repeatedAttribute() {
        String input = "S: @a, @a . a: 'a'; 'b' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());

        InvisibleXmlDocument doc = parser.parse("aa");

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E001", ex.getCode());
        }
    }

    @Test
    public void redefinedNonterminal() {
        String input = "date: ['0123'], ['0'-'9'] .\n" +
                "date: 'January' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E004", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void badHex() {
        String input = "date: [#00decafbad00badbadbad] .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E007", ((IxmlException) parser.getException()).getCode());

        input = "date: [#ffffffff0] .";

        parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E007", ((IxmlException) parser.getException()).getCode());

        input = "date: [#fffe] .";

        parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E007", ((IxmlException) parser.getException()).getCode());

        input = "date: [#1fffe] .";

        parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E007", ((IxmlException) parser.getException()).getCode());

        input = "date: [#d801] .";

        parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E007", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void badUnicodeClass() {
        String input = "s: [Xq] .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);

        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E002", ((IxmlException) parser.getException()).getCode());
    }

    @Test
    public void rootAttribute() {
        String input = "@s: 'a' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E008", ex.getCode());
        }
    }

    @Test
    public void alsoRootAttribute() {
        String input = "-s: t. @t: 'a' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E008", ex.getCode());
        }
    }

    @Test
    public void multipleRoots() {
        String input = "-s: t, u. t: 't' . u: 'u' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("tu");
        Assert.assertTrue(doc.succeeded());

        try {
            String tree = doc.getTree();
            fail();
        } catch (IxmlException ex) {
            Assert.assertEquals("E009", ex.getCode());
        }
    }

    @Test
    public void emptyOutput() {
        String input = "s: -'a' .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertTrue(parser.constructed());
        InvisibleXmlDocument doc = parser.parse("a");
        Assert.assertTrue(doc.succeeded());

        String tree = doc.getTree();
        Assert.assertEquals("<s/>", tree);
    }

    @Test
    public void invalidRange() {
        String input = "s: ['Z'-'A'] .";

        InvisibleXmlParser parser = InvisibleXml.getParserFromIxml(input);
        Assert.assertFalse(parser.constructed());
        Assert.assertNotNull(parser.getException());
        Assert.assertEquals("E010", ((IxmlException) parser.getException()).getCode());
    }

}
