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
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assert.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void parseCompiledGrammar() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/date.cxml"));
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assert.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void compileHashGrammar() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/tests/correct/hash.ixml"));
            InvisibleXmlDocument doc = parser.parse("#12.");
            Assertions.assertTrue(doc.succeeded());
            String compiled = parser.getCompiledParser();

            ByteArrayInputStream bais = new ByteArrayInputStream(compiled.getBytes(StandardCharsets.UTF_8));

            parser = invisibleXml.getParser(bais, null);
            doc = parser.parse("#12.");
            Assertions.assertTrue(doc.succeeded());
            String recompiled = parser.getCompiledParser();

            Assertions.assertEquals(compiled, recompiled);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void compiledGrammarBug() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/bad-compiled.ixml"));
            String compiled = parser.getCompiledParser();

            ByteArrayInputStream bais = new ByteArrayInputStream(compiled.getBytes(StandardCharsets.UTF_8));
            InvisibleXmlParser parser2 = invisibleXml.getParser(bais, null);
            String recompiled = parser2.getCompiledParser();

            Assertions.assertEquals(compiled, recompiled);
        } catch (Exception ex) {
            fail();
        }
    }

}
