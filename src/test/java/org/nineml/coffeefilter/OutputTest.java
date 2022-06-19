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
