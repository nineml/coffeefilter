package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;

public class CompiledGrammarTest {
    @Test
    public void parseIxmlGrammar() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/test/resources/date.ixml"));
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assert.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void parseCompiledGrammar() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/test/resources/date.cxml"));
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assert.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void compileHashGrammar() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("ixml/tests/correct/hash.ixml"));
            InvisibleXmlDocument doc = parser.parse("#12.");
            //doc.getEarleyResult().getForest().parse().serialize("hash.xml");
            //doc.getEarleyResult().getForest().serialize("graph.xml");
            String compiled = parser.getCompiledParser();
            //System.err.println(compiled);

            ByteArrayInputStream bais = new ByteArrayInputStream(compiled.getBytes(StandardCharsets.UTF_8));

            parser = InvisibleXml.getParser(bais, null);
            doc = parser.parse("#12.");
            //doc.getEarleyResult().getForest().parse().serialize("hash2.xml");
            //doc.getEarleyResult().getForest().serialize("graph2.xml");
        } catch (Exception ex) {
            fail();
        }
    }

}
