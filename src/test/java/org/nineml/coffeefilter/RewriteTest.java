package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.Grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.fail;

public class RewriteTest {
    @Test
    public void aStar() {
        String g = "S=A,B*. A='a'. B='b'.";
        String s = "ab";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());
    }

    @Test
    public void aStarSep() {
        String g = "S=A,B**C. A='a'. B='b'. C='c'.";
        String s = "abcb";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());
    }

    @Test
    public void aPlus() {
        String g = "S=A,B+. A='a'. B='b'.";
        String s = "ab";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());
    }

    @Test
    public void aPlusSep() {
        String g = "S=A,B++C. A='a'. B='b'. C='c'.";
        String s = "abcb";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());
    }

    @Test
    public void propertyFile() {
        try {
            InvisibleXml ixml = new InvisibleXml();
            InvisibleXmlParser parser = ixml.getParserFromIxml(new FileInputStream("src/test/resources/property-file.ixml"), "UTF-8");
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/short-example.properties"));

            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }
}
