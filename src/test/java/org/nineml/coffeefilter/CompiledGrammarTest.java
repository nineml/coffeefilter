package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;

public class CompiledGrammarTest {
    private static final InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void parseIxmlGrammar() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/date.ixml"));
            //System.err.println(parser.getCompiledParser());
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assert.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }
}
