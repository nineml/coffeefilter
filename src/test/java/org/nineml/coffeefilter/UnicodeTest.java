package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.ParserType;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;
import org.nineml.logging.Logger;

import java.io.File;

import static org.junit.Assert.fail;

public class UnicodeTest {
    private InvisibleXml invisibleXml;

    @Before
    public void setup() {
        ParserOptions options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void small() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel(Logger.DEBUG);
            //invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/unicode.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/SmallData.txt"));
            Assert.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
