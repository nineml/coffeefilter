package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.utils.TokenUtils;

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
        Assertions.assertTrue(!xml.contains("<end-of-input>"));
        System.out.println(xml);
    }

    @Test
    public void outOfInput() {
        String input = "S: (A; B), '.'+. A: 'a', '.'+ . B: 'b', '.'+ .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        InvisibleXmlDocument doc = parser.parse("a.");
        String xml = doc.getTree();
        Assertions.assertTrue(xml.contains("<end-of-input>true</end-of-input>"));
        System.out.println(xml);
    }
}
