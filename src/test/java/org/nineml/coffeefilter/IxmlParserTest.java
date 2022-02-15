package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeegrinder.parser.Grammar;

import java.io.File;

import static org.junit.Assert.fail;

public class IxmlParserTest {
    @Test
    public void testParseIxml() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/ixml.ixml"));
            Grammar grammar = parser.getGrammar();
            Assert.assertNotNull(grammar);
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testParseExceptions() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/test/resources/exceptions.ixml"));
            Grammar grammar = parser.getGrammar();
            Assert.assertNotNull(grammar);
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }


    /*
    @Test
    public void testParseProgram() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("test-suite/correct/program.ixml"));
            System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
     */
}
