package org.nineml.coffeefilter;

import org.nineml.coffeefilter.trees.StringTreeBuilder;
import org.nineml.coffeefilter.trees.XmlTreeBuilder;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeegrinder.gll.GllResult;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.GearleyResult;
import org.nineml.coffeegrinder.parser.ParserType;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.PriorityTreeSelector;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.trees.TreeSelector;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An InvisibleXmlDocument represents a document created with an {@link InvisibleXmlParser}.
 *
 * <p>From this object, you can obtain the parsed representation(s) of the document. For ambiguous grammars,
 * there may be more than one.</p>
 */
public class InvisibleXmlDocument {
    protected final GearleyResult result;
    protected final boolean prefixOk;
    protected final String parserVersion;
    protected final ParserOptions options;

    protected InvisibleXmlDocument(GearleyResult result, String parserVersion, ParserOptions options) {
        this.result = result;
        this.prefixOk = false;
        this.options = options;
        this.parserVersion = parserVersion;
        result.setTreeSelector(new PriorityTreeSelector());
    }

    protected InvisibleXmlDocument(GearleyResult result, String parserVersion, ParserOptions options, boolean prefixOk) {
        this.result = result;
        this.prefixOk = prefixOk;
        this.options = options;
        this.parserVersion = parserVersion;
        result.setTreeSelector(new PriorityTreeSelector());
    }

    /**
     * Get the parser options.
     * @return the parser options
     */
    public ParserOptions getOptions() {
        return options;
    }

    /**
     * Get the line number of the last line processed.
     * <p>If the parse failed, this will be the line on which the error occurred. Lines
     * are counted by occurrence of '\n'.</p>
     * @return the line number
     */
    public int getLineNumber() {
        return result.getLineNumber();
    }

    /**
     * Get the column number of the last line processed.
     * <p>If the parse failed, this will be the position of the character in the last
     * line where the error occurred.</p>
     * @return the column number
     */
    public int getColumnNumber() {
        return result.getColumnNumber();
    }

    /**
     * Get the offset of the last character processed.
     * <p>If the parse failed, this will be the position of the character in the
     * input stream.</p>
     * @return the offset
     */
    public int getOffset() {
        return result.getOffset();
    }

    /**
     * Return the underlying {@link EarleyResult} result for this parse.
     * @return the result
     */
    public GearleyResult getResult() {
        return result;
    }

    /**
     * Return the parser type.
     * @return the parser type
     */
    public ParserType getParserType() {
        if (result instanceof GllResult) {
            return ParserType.GLL;
        }
        return ParserType.Earley;
    }

    /**
     * Did the parse succeed?
     * @return true if the parse was successful
     */
    public boolean succeeded() {
        return getNumberOfParses() > 0;
    }

    /**
     * Return the number of available parses of this document.
     *
     * <p>Will return 0 if there are no successful parses.</p>
     *
     * @return The number of parses.
     */
    public long getNumberOfParses() {
        if (result.succeeded() || result.prefixSucceeded()) {
            return result.getForest().getParseTreeCount();
        }
        return 0;
    }

    /**
     * Is this document ambiguous?
     * <p>The return value is arbitrary if the parse did not succeed.</p>
     * @return true, if the document is ambiguous
     */
    public boolean isAmbiguous() {
        return result.getForest() == null || result.getForest().isAmbiguous();
    }

    /**
     * Is this document infinitely ambiguous?
     * <p>The return value is arbitrary if the parse did not succeed.</p>
     * @return true, if the document is ambiguous
     */
    public boolean isInfinitelyAmbiguous() {
        return result.getForest() == null || result.getForest().isInfinitelyAmbiguous();
    }

    /**
     * Returns the amount of time (roughly) spent parsing.
     *
     * @return The number of milliseconds spent parsing.
     */
    public long parseTime() {
        return result.getParseTime();
    }

    /**
     * Return an XML representation of the current parse.
     * @return the XML.
     */
    public String getTree() {
        StringTreeBuilder handler = new StringTreeBuilder(options);
        getTree(handler);
        return handler.getXml();
    }

    /**
     * Write an XML representation of the current parse to the stream.
     * @param output the output stream.
     */
    public void getTree(PrintStream output) {
        StringTreeBuilder handler = new StringTreeBuilder(options, output);
        getTree(handler);
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler.
     * @param handler the content handler.
     */
    public void getTree(ContentHandler handler) {
        XmlTreeBuilder builder = new XmlTreeBuilder(parserVersion, getOptions(), handler);
        getTree(builder);
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler
     * using a different set of options.
     * @param handler the content handler.
     * @param options the options.
     */
    public void getTree(ContentHandler handler, ParserOptions options) {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder(parserVersion, options, handler);
        getTree(treeBuilder);
    }

    /**
     * Process the result with your own {@link TreeBuilder}.
     * <p>This API needs work.</p>
     * @param builder the tree builder.
     */
    public void getTree(TreeBuilder builder) {
        result.getTree(builder);
    }

    public void setTreeSelector(TreeSelector selector) {
        result.setTreeSelector(selector);
    }

    public void resetTrees() {
        result.resetTrees();;
    }
}
