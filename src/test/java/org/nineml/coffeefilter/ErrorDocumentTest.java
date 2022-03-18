package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.utils.TokenUtils;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.util.GrammarCompiler;

import java.io.File;

import static junit.framework.TestCase.fail;

public class ErrorDocumentTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void wrongInput() {
        String input = "S: (A; B), '.'+. A: 'a', '.'+ . B: 'b', '.'+ .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        InvisibleXmlDocument doc = parser.parse("a!.");
        String xml = doc.getTree();
        Assertions.assertTrue(xml.contains("<unexpected>!</unexpected>"));
        Assertions.assertFalse(xml.contains("<end-of-input>"));
    }

    @Test
    public void outOfInput() {
        String input = "S: (A; B), '.'+. A: 'a', '.'+ . B: 'b', '.'+ .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        InvisibleXmlDocument doc = parser.parse("a.");
        String xml = doc.getTree();
        Assertions.assertTrue(xml.contains("<end-of-input>true</end-of-input>"));
    }

    @Test
    public void testMonths_March() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/month.ixml"));
            InvisibleXmlDocument doc = parser.parse("March");
            Assertions.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testMonths_Max() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/month.ixml"));
            InvisibleXmlDocument doc = parser.parse("Max");
            Assertions.assertFalse(doc.succeeded());
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<permitted>'r', 'y'</permitted>"));
            Assertions.assertTrue(xml.contains("<also-predicted>'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</also-predicted>"));
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testMonths_Marsh() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/month.ixml"));
            InvisibleXmlDocument doc = parser.parse("Marsh");
            Assertions.assertFalse(doc.succeeded());
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<permitted>'c'</permitted>"));
            Assertions.assertTrue(xml.contains("<also-predicted>'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</also-predicted>"));
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void testMonths_XRay() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/month.ixml"));
            InvisibleXmlDocument doc = parser.parse("XRay");
            Assertions.assertFalse(doc.succeeded());
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<permitted>'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</permitted>"));
            Assertions.assertFalse(xml.contains("<also-predicted>"));
        } catch (Exception ex) {
            Assert.fail();
        }
    }


}
