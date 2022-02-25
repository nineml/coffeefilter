package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.HygieneReport;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;

import java.io.File;

public class ReportTest {
    @Test
    public void testHygieneReport() {
        try {
            InvisibleXmlParser parser = InvisibleXml.getParser(new File("src/test/resources/unproductive.ixml"));
            Assertions.assertTrue(parser.constructed());

            HygieneReport report = parser.getHygieneReport();
            Assertions.assertFalse(report.isClean());
            Assertions.assertEquals(3, report.getUnproductiveRules().size());
            Assertions.assertEquals(2, report.getUnproductiveSymbols().size());
            Assertions.assertEquals(1, report.getUnreachableSymbols().size());

            NonterminalSymbol _F = parser.getGrammar().getNonterminal("F");
            NonterminalSymbol _G = parser.getGrammar().getNonterminal("G");

            Assertions.assertTrue(report.getUnreachableSymbols().contains(_G));
            Assertions.assertTrue(report.getUnproductiveSymbols().contains(_F));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }
}
