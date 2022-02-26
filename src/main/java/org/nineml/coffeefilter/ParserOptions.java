package org.nineml.coffeefilter;

public class ParserOptions extends org.nineml.coffeegrinder.parser.ParserOptions {
    /**
     * The default constructor.
     * <p>This object is intended to be just a collection of publicly modifiable fields.</p>
     */
    public ParserOptions() {
        super();
    }

    /**
     * A copy constructor.
     * @param copy the options to copy.
     */
    public ParserOptions(ParserOptions copy) {
        super(copy);
        ignoreTrailingWhitespace = copy.ignoreTrailingWhitespace;
        prettyPrint = copy.prettyPrint;
        showChart = copy.showChart;
        graphviz = copy.graphviz;
        suppressIxmlAmbiguous = copy.suppressIxmlAmbiguous;
        suppressIxmlPrefix = copy.suppressIxmlPrefix;
        allowUndefinedSymbols = copy.allowUndefinedSymbols;
        assertValidXmlNames = copy.assertValidXmlNames;
    }

    /**
     * Ignore trailing whitespace.
     * <p>If a parse fails where it would have succeeded if trailing whitespace was
     * removed from the input, report success.</p>
     */
    public boolean ignoreTrailingWhitespace = false;

    /**
     * Allow undefined symbols.
     * <p>A grammar with undefined symbols isn't necessarily unusable. But Invisible XML
     * forbids them.</p>
     */
    public boolean allowUndefinedSymbols = false;

    /**
     * Attempt to pretty print the result?
     */
    public boolean prettyPrint = false;

    /**
     * Show the Earley chart in error results?
     */
    public boolean showChart = false;

    /**
     * Where's the GraphViz 'dot' command?
     */
    public String graphviz = null;

    /**
     * Suppress the ixml:state=ambiguous annotation on the root element.
     */
    public boolean suppressIxmlAmbiguous = false;

    /**
     * Suppress the ixml:state=prefix annotation on the root element.
     */
    public boolean suppressIxmlPrefix = false;

    /**
     * Raise an exception if invalid XML names are used in nonterminals that are serialized.
     */
    public boolean assertValidXmlNames = true;

}
