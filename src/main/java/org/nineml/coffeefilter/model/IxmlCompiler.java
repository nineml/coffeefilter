package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.GrammarCompiler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * An API to compiled grammars.
 */
public class IxmlCompiler {
    /**
     * Compile a grammar.
     * @param grammar the grammar
     * @return the compiled grammar
     */
    public String compile(Grammar grammar) {
        return compile(grammar, null);
    }

    /**
     * Compile a grammar.
     * @param grammar the grammar
     * @param properties the properties to store in that grammar
     * @return the compiled grammar
     */
    public String compile(Grammar grammar, Map<String,String> properties) {
        GrammarCompiler compiler = new GrammarCompiler();
        if (properties != null) {
            for (String name : properties.keySet()) {
                compiler.setProperty(name, properties.get(name));
            }
        }
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
        return new Ixml(compiler.parse(compiled));
    }

    /**
     * Parse a compiled grammar
     * @param input the compiled grammar
     * @return the Ixml parser
     */
    public Ixml parse(String input) {
        GrammarCompiler compiler = new GrammarCompiler();
        return new Ixml(compiler.parse(input));
    }
}
