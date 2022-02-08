package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.GrammarCompiler;

import java.io.File;
import java.io.IOException;

public class IxmlCompiler {
    public String compile(Grammar grammar) {
        GrammarCompiler compiler = new GrammarCompiler();
        return compiler.compile(grammar);
    }

    public Ixml parse(File compiled) throws IOException {
        GrammarCompiler compiler = new GrammarCompiler();
        return new Ixml(compiler.parse(compiled));
    }

    public Ixml parse(String input) {
        GrammarCompiler compiler = new GrammarCompiler();
        return new Ixml(compiler.parse(input));
    }
}
