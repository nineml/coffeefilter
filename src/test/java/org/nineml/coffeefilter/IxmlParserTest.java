package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.ParseTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class IxmlParserTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void testParseIxml() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/ixml.ixml"));
            Grammar grammar = parser.getGrammar();
            Assert.assertNotNull(grammar);
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testParsePragmasIxml() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/pragmas.ixml"));
            Grammar grammar = parser.getGrammar();
            Assert.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/main/resources/org/nineml/coffeefilter/pragmas.ixml"));
            //doc.getResult().getForest().serialize("/tmp/out.xml");
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }


    @Test
    public void testParseExceptions() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/exceptions.ixml"));
            Grammar grammar = parser.getGrammar();
            Assert.assertNotNull(grammar);
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void versionDeclaration() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(Files.newInputStream(Paths.get("src/test/resources/version-decl.ixml")), "UTF-8");
            Assert.assertEquals("1.0", parser.getIxmlVersion());
            String input = "abc\uD83D\uDE3A";
            InvisibleXmlDocument doc = parser.parse(input);
            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void unknownVersion() {
        String grammar = "ixml version '13.3'. S='a'.";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar);
        Assert.assertEquals("13.3", parser.getIxmlVersion());
        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);
        String xml = doc.getTree();
        Assert.assertTrue(xml.contains("version-mismatch"));
    }
}
