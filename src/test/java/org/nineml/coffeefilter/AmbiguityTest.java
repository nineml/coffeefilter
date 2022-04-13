package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.Ambiguity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;

public class AmbiguityTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Ignore
    public void parsePragmas() {
        // What was this for? These files don't even exist anymore...
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/pragmas.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/pragmas-test.ixml"));
            Ambiguity ambiguity = doc.getEarleyResult().getForest().getAmbiguity();
            Assertions.assertFalse(ambiguity.getAmbiguous());
            Assertions.assertFalse(ambiguity.getInfinitelyAmbiguous());

            InvisibleXmlParser parser2 = invisibleXml.getParser(new File("src/test/resources/pragmas.cxml"));
            InvisibleXmlDocument doc2 = parser2.parse(new File("src/test/resources/pragmas-test.ixml"));
            ambiguity = doc2.getEarleyResult().getForest().getAmbiguity();
            Assertions.assertFalse(ambiguity.getAmbiguous());
            Assertions.assertFalse(ambiguity.getInfinitelyAmbiguous());

            Assertions.assertEquals(parser.getGrammar().getRules().size(), parser2.getGrammar().getRules().size());
        } catch (Exception ex) {
            fail();
        }
    }
}
