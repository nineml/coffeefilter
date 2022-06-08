package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.ParserType;

import java.io.File;

import static org.junit.Assert.fail;

public class UnicodeTest {
    private InvisibleXml invisibleXml;
    private ParserOptions options;

    @Before
    public void setup() {
        options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void small() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/unicode.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/TestData.txt"));
            Assert.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
