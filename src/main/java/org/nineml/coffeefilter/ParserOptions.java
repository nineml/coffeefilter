package org.nineml.coffeefilter;

public class ParserOptions extends org.nineml.coffeegrinder.parser.ParserOptions {
    /**
     * Ignore trailing whitespace.
     * <p>If a parse fails where it would have succeeded if trailing whitespace was
     * removed from the input, report success.</p>
     */
    public boolean ignoreTrailingWhitespace = false;

    /**
     * Attempt to pretty print the result?
     */
    public boolean prettyPrint = false;

    /**
     * Be verbose about what's going on?
     */
    public boolean verbose = false;

    /**
     * Where's the GraphViz 'dot' command?
     */
    public String graphviz = null;
}
