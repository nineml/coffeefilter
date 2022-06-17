import org.junit.Assert;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.logging.Logger;

import java.io.File;
import java.io.IOException;

public class MainTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    public static void main(String[] args) {
        try {
            invisibleXml.getOptions().getLogger().setDefaultLogLevel(Logger.DEBUG);
            for (int count = 0 ; count < 1; count++) {
                InvisibleXmlParser parser = invisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/ixml.ixml"));
                Grammar grammar = parser.getGrammar();
                System.err.println("Got grammar? " + (grammar == null ? "false" : "true"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}