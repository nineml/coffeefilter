package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.nio.file.Files;

import static org.junit.Assert.fail;

public class OutputTest {
    @Test
    public void getTreeTest() {
        String ixml = "S = A, B. A = 'a'. B = 'b'.";
        InvisibleXmlParser parser = new InvisibleXml().getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("ab");
        String xml = doc.getTree();
        Assertions.assertEquals("<S><A>a</A><B>b</B></S>", xml);
    }

    @Test
    public void showMarksTest() {
        String ixml = "S = (A | B)+. A = 'a'. B = 'b'.";

        ParserOptions options = new ParserOptions();
        options.setShowMarks(true);
        InvisibleXmlParser parser = new InvisibleXml(options).getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("aa");
        String xml = doc.getTree();
        //System.out.println(xml);
        Assertions.assertEquals("<S xmlns:ixml=\"http://invisiblexml.org/NS\" ixml:mark=\"^\"><A ixml:mark=\"^\">a</A><A ixml:mark=\"^\">a</A></S>", xml);

        options.setShowBnfNonterminals(true);
        parser = new InvisibleXml(options).getParserFromIxml(ixml);
        doc = parser.parse("aa");
        xml = doc.getTree();
        //System.out.println(xml);
        Assertions.assertTrue(xml.startsWith("<n:symbol"));
    }

    @Test
    public void getTreeOutputTest() {
        String ixml = "S = A, B. A = 'a'. B = 'b'.";
        InvisibleXmlParser parser = new InvisibleXml().getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("ab");

        try {
            File output = File.createTempFile("coffeefilter", "xml");
            output.deleteOnExit();
            try (PrintStream out = new PrintStream(output)) {
                doc.getTree(out);
                out.close();
                BufferedReader stream = new BufferedReader(new InputStreamReader(Files.newInputStream(output.toPath())));
                Assertions.assertEquals("<S><A>a</A><B>b</B></S>", stream.readLine());
                stream.close();
                if (!output.delete()) {
                    fail();
                }
            } catch (Exception ex) {
                fail();
            }
        } catch (Exception ex) {
            fail();
        }
    }


}
