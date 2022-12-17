package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public class CharacterClassTests {

    @Test
    public void lTest() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        String grammar = "input: char*. -char: L. L: [L].";
        InvisibleXmlParser parser = ixml.getParserFromIxml(grammar);

        String input = "a";
        InvisibleXmlDocument document = parser.parse(input);

        String xml = document.getTree();

        //System.out.println(xml);

        Assertions.assertEquals("<input><L>a</L></input>", xml);
    }

    @Test
    public void lcTest() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/char1.ixml"));

            String input = "a";
            InvisibleXmlDocument document = parser.parse(input);

            String xml = document.getTree();

            //System.out.println(xml);

            Assertions.assertEquals("<input><LC>a</LC></input>", xml);

        } catch (IOException ioe) {
            fail();
        }
    }

}
