package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.GrammarCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * An API to compiled grammars.
 */
public class IxmlCompiler {
    private final ParserOptions options;

    public IxmlCompiler(ParserOptions options) {
        this.options = options;
    }

    /**
     * Compile a grammar.
     * @param grammar the grammar
     * @return the compiled grammar
     */
    public String compile(Grammar grammar) {
        GrammarCompiler compiler = new GrammarCompiler();
        return compiler.compile(grammar);
    }

    /**
     * Parse a compiled grammar.
     * @param compiled the compiled grammar
     * @return the Ixml parser
     * @throws IOException if the file cannot be read
     */
    public Ixml parse(File compiled) throws IOException {
        GrammarCompiler compiler = new GrammarCompiler();
        return new Ixml(options, compiler.parse(compiled));
    }

    /**
     * Parse a compiled grammar
     * @param input the compiled grammar
     * @return the Ixml parser
     */
    public Ixml parse(String input) {
        GrammarCompiler compiler = new GrammarCompiler();
        return new Ixml(options, compiler.parse(input));
    }
}
