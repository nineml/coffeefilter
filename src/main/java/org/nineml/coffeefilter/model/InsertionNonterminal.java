package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;

public class InsertionNonterminal extends NonterminalSymbol {
    protected InsertionNonterminal(Grammar grammar, String name, Collection<ParserAttribute> attributes) {
        super(grammar, name, attributes);
    }
}
