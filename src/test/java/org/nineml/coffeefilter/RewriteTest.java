package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        g = "<ixml xmlns:ixml=\"http://invisiblexml.org/NS\">\n" +
                "   <rule name=\"S\">\n" +
                "      <alt>\n" +
                "         <nonterminal name=\"A\"/>\n" +
                "         <repeat0>\n" +
                "            <nonterminal name=\"B\"/>\n" +
                "         </repeat0>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"A\">\n" +
                "      <alt>\n" +
                "         <literal string=\"a\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"B\">\n" +
                "      <alt>\n" +
                "         <literal string=\"b\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"C\">\n" +
                "      <alt>\n" +
                "         <literal string=\"c\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "</ixml>";

        ByteArrayInputStream bais = new ByteArrayInputStream(g.getBytes(StandardCharsets.UTF_8));

        try {
            parser = ixml.getParserFromVxml(bais, null);
            doc = parser.parse(s);
            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void aStarTerminal() {
        String g = "S=A,'b'*. A='a'.";
        String s = "ab";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());

        g = "S=A,#20*. A='a'.";
        s = "a";

        ixml = new InvisibleXml();
        parser = ixml.getParserFromIxml(g);
        doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());

        g = "S=A,' '*. A='a'.";
        s = "a  ";

        ixml = new InvisibleXml();
        parser = ixml.getParserFromIxml(g);
        doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());
    }

    @Test
    public void aStarGroup() {
        String g = "S=A,('b','c')*. A='a'.";
        String s = "abcbc";

        InvisibleXml ixml = new InvisibleXml();
        ixml.getOptions().setParserType("GLL");
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

        g = "<ixml xmlns:ixml=\"http://invisiblexml.org/NS\">\n" +
                "   <rule name=\"S\">\n" +
                "      <alt>\n" +
                "         <nonterminal name=\"A\"/>\n" +
                "         <repeat0>\n" +
                "            <nonterminal name=\"B\"/>\n" +
                "            <sep>\n" +
                "               <nonterminal name=\"C\"/>\n" +
                "            </sep>\n" +
                "         </repeat0>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"A\">\n" +
                "      <alt>\n" +
                "         <literal string=\"a\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"B\">\n" +
                "      <alt>\n" +
                "         <literal string=\"b\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"C\">\n" +
                "      <alt>\n" +
                "         <literal string=\"c\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "</ixml>";

        ByteArrayInputStream bais = new ByteArrayInputStream(g.getBytes(StandardCharsets.UTF_8));

        try {
            parser = ixml.getParserFromVxml(bais, null);
            doc = parser.parse(s);
            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void aPlus() {
        String g = "S=A,B+. A='a'. B='b'.";
        String s = "ab";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());

        g = "<ixml xmlns:ixml=\"http://invisiblexml.org/NS\">\n" +
                "   <rule name=\"S\">\n" +
                "      <alt>\n" +
                "         <nonterminal name=\"A\"/>\n" +
                "         <repeat1>\n" +
                "            <nonterminal name=\"B\"/>\n" +
                "         </repeat1>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"A\">\n" +
                "      <alt>\n" +
                "         <literal string=\"a\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"B\">\n" +
                "      <alt>\n" +
                "         <literal string=\"b\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"C\">\n" +
                "      <alt>\n" +
                "         <literal string=\"c\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "</ixml>";

        ByteArrayInputStream bais = new ByteArrayInputStream(g.getBytes(StandardCharsets.UTF_8));

        try {
            parser = ixml.getParserFromVxml(bais, null);
            doc = parser.parse(s);
            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void aPlusSep() {
        String g = "S=A,B++C. A='a'. B='b'. C='c'.";
        String s = "abcb";

        InvisibleXml ixml = new InvisibleXml();
        InvisibleXmlParser parser = ixml.getParserFromIxml(g);
        InvisibleXmlDocument doc = parser.parse(s);

        Assert.assertTrue(doc.succeeded());

        g = "<ixml xmlns:ixml=\"http://invisiblexml.org/NS\">\n" +
                "   <rule name=\"S\">\n" +
                "      <alt>\n" +
                "         <nonterminal name=\"A\"/>\n" +
                "         <repeat1>\n" +
                "            <nonterminal name=\"B\"/>\n" +
                "            <sep>\n" +
                "               <nonterminal name=\"C\"/>\n" +
                "            </sep>\n" +
                "         </repeat1>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"A\">\n" +
                "      <alt>\n" +
                "         <literal string=\"a\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"B\">\n" +
                "      <alt>\n" +
                "         <literal string=\"b\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "   <rule name=\"C\">\n" +
                "      <alt>\n" +
                "         <literal string=\"c\"/>\n" +
                "      </alt>\n" +
                "   </rule>\n" +
                "</ixml>";

        ByteArrayInputStream bais = new ByteArrayInputStream(g.getBytes(StandardCharsets.UTF_8));

        try {
            parser = ixml.getParserFromVxml(bais, null);
            doc = parser.parse(s);
            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void propertyFile() {
        try {
            InvisibleXml ixml = new InvisibleXml();
            InvisibleXmlParser parser = ixml.getParserFromIxml(Files.newInputStream(Paths.get("src/test/resources/property-file.ixml")), "UTF-8");
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/short-example.properties"));

            Assert.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }
}
