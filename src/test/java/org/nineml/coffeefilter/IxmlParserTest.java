package org.nineml.coffeefilter;

import org.junit.Test;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlParser;

import static org.junit.Assert.fail;

public class IxmlParserTest {
    @Test
    public void testParseIxml() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("src/main/resources/org/nineml/coffeefilter/ixml.ixml");
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testParseExceptions() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("src/test/resources/exceptions.ixml");
            System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }


    /*
    @Test
    public void testParseProgram() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("test-suite/correct/program.ixml");
            System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
     */
}
