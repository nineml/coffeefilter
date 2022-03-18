package org.nineml.coffeefilter;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.DefaultTreeWalker;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.nineml.coffeefilter.utils.CommonBuilder;
import org.nineml.coffeefilter.trees.StringTreeBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An InvisibleXmlDocument represents a document created with an {@link InvisibleXmlParser}.
 *
 * <p>From this object, you can obtain the parsed representation(s) of the document. For ambiguous grammars,
 * there may be more than one.</p>
 */
public class InvisibleXmlDocument {
    private final EarleyResult result;
    private final boolean prefixOk;
    private final TreeWalker treeWalker;
    private ParserOptions options;
    private boolean selectedFirst = false;
    private int lineNumber = -1;
    private int columnNumber = -1;
    private int offset = -1;

    protected InvisibleXmlDocument(EarleyResult result, ParserOptions options) {
        this.result = result;
        this.prefixOk = false;
        this.options = options;
        if (result.succeeded() || result.prefixSucceeded()) {
            treeWalker = new DefaultTreeWalker(result.getForest(), new ParseTreeBuilder());
        } else {
            treeWalker = null;
        }
    }

    protected InvisibleXmlDocument(EarleyResult result, ParserOptions options, boolean prefixOk) {
        this.result = result;
        this.prefixOk = prefixOk;
        this.options = options;
        if (result.succeeded() || result.prefixSucceeded()) {
            treeWalker = new DefaultTreeWalker(result.getForest(), new ParseTreeBuilder());
        } else {
            treeWalker = null;
        }
    }

    protected void setLocation(int offset, int line, int col) {
        this.offset = offset;
        lineNumber = line;
        columnNumber = col;
    }

    /**
     * Get the parser options.
     * @return the parser options
     */
    public ParserOptions getOptions() {
        return options;
    }

    /**
     * Set the parser options.
     * @param options the parser options
     */
    public void setOptions(ParserOptions options) {
        this.options = options;
    }

    /**
     * Get the line number of the last line processed.
     * <p>If the parse failed, this will be the line on which the error occurred. Lines
     * are counted by occurrence of '\n'.</p>
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Get the column number of the last line processed.
     * <p>If the parse failed, this will be the position of the character in the last
     * line where the error occurred.</p>
     * @return the column number
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Get the offset of the last character processed.
     * <p>If the parse failed, this will be the position of the character in the
     * input stream.</p>
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Return the underlying {@link EarleyResult} result for this parse.
     * @return the result
     */
    public EarleyResult getEarleyResult() {
        return result;
    }

    /**
     * Did the parse succeed?
     * @return true if the parse was successful
     */
    public boolean succeeded() {
        return getNumberOfParses() > 0;
    }

    /**
     * Return the number of successful parses of this document.
     *
     * <p>Will return 0 if there are no successful parses.</p>
     *
     * @return The number of parses.
     */
    public long getNumberOfParses() {
        if (result.succeeded() || result.prefixSucceeded()) {
            return result.getForest().getTotalParses();
        }
        return 0;
    }

    /**
     * Return the exact number of successful parses of this document.
     *
     * <p>Will return 0 if there are no successful parses.</p>
     *
     * @return The number of parses.
     */
    public BigInteger getExactNumberOfParses() {
        if (result.succeeded() || result.prefixSucceeded()) {
            return result.getForest().getExactTotalParses();
        }
        return new BigInteger("0");
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
     * Return the underlying ParseTree for the current parse.
     * @return the parse tree
     */
    public ParseTree getParseTree() {
        if (treeWalker == null) {
            return null;

        }
        if (!selectedFirst) {
            treeWalker.next();
            selectedFirst = true;
        }

        return ((ParseTreeBuilder) treeWalker.getTreeBuilder()).getTree();
    }

    /**
     * Return an XML representation of the current parse.
     * @return the XML.
     */
    public String getTree() {
        ParseTree tree = getParseTree();
        CommonBuilder builder = new CommonBuilder(tree, result, options);
        StringTreeBuilder handler = new StringTreeBuilder(options);
        realize(builder, handler);
        return handler.getXml();
    }

    /**
     * Write an XML representation of the current parse to the stream.
     * @param output the output stream.
     */
    public void getTree(PrintStream output) {
        ParseTree tree = getParseTree();
        CommonBuilder builder = new CommonBuilder(tree, result, options);
        StringTreeBuilder handler = new StringTreeBuilder(options, output);
        realize(builder, handler);
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler.
     * @param handler the content handler.
     */
    public void getTree(ContentHandler handler) {
        getTree(handler, options);
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler.
     * @param handler the content handler.
     * @param options the options to use when constructing the tree
     */
    public void getTree(ContentHandler handler, ParserOptions options) {
        ParseTree tree = getParseTree();
        CommonBuilder builder = new CommonBuilder(tree, result, options);
        realize(builder, handler);
    }

    public boolean nextTree() {
        if (treeWalker.hasNext()) {
            treeWalker.next();
            return true;
        }
        return false;
    }

    private void realize(CommonBuilder builder, ContentHandler handler) {
        if (result.succeeded() || (result.prefixSucceeded() && prefixOk)) {
            builder.build(handler);
            return;
        }

        try {
            handler.startDocument();

            handler.startPrefixMapping(CommonBuilder.ixml_prefix, CommonBuilder.ixml_ns);

            AttributeBuilder attrs = new AttributeBuilder(options);
            attrs.addAttribute(CommonBuilder.ixml_ns, CommonBuilder.ixml_prefix + ":state", "failed");
            handler.startElement("", "fail", "failed", attrs);

            if (lineNumber > 0) {
                atomicValue(handler, "line", ""+lineNumber);
            }

            if (columnNumber > 0) {
                atomicValue(handler, "column", ""+columnNumber);
            }

            atomicValue(handler, "pos", ""+result.getTokenCount());

            TokenCharacter tchar = (TokenCharacter) result.getLastToken();
            if (tchar != null) {
                if (result.getParser().moreInput()) {
                    atomicValue(handler, "unexpected", ""+tchar.getValue());
                } else {
                    atomicValue(handler, "end-of-input", "true");
                }
            }

            boolean predictedSome = false;
            List<Token> oknext = couldBeNext(result.predictedTerminals());
            if (!oknext.isEmpty()) {
                predictedSome = true;
                tokenList(handler, oknext, "permitted");
            }

            oknext = couldBeNext(result.getChart(), result.getParser().getGrammar());
            if (!oknext.isEmpty()) {
                String elemName = "permitted";
                if (predictedSome) {
                    elemName = "also-predicted";
                }
                tokenList(handler, oknext, elemName);
            }

            if (options.getShowChart()) {
                handler.startElement("", "chart", "chart", AttributeBuilder.EMPTY_ATTRIBUTES);

                for (int row = 0; row < result.getChart().size(); row++) {
                    if (!result.getChart().get(row).isEmpty()) {
                        attrs = new AttributeBuilder(options);
                        attrs.addAttribute("n", ""+row);
                        handler.startElement("", "row", "row", attrs);

                        attrs = new AttributeBuilder(options);
                        for (EarleyItem item : result.getChart().get(row)) {
                            writeString(handler,"  ");
                            handler.startElement("", "item", "item", attrs);
                            writeString(handler, item.toString());
                            handler.endElement("", "item", "item");
                        }

                        handler.endElement("", "row", "row");
                    }
                }
                handler.endElement("", "chart", "chart");
            }

            handler.endElement("", "fail", "failed");
            handler.endDocument();
        } catch (SAXException ex) {
            throw IxmlException.parseFailed(ex);
        }
    }

    private void atomicValue(ContentHandler handler, String name, String value) throws SAXException {
        handler.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", name, name);
    }

    private void tokenList(ContentHandler handler, List<Token> oknext, String elemName) throws SAXException {
        // I don't actually care about the order,
        // but let's not just make it HashMap random for testing if nothing else.
        ArrayList<String> chars = new ArrayList<>();
        for (Token next : oknext) {
            chars.add(next.toString());
        }
        Collections.sort(chars);

        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < chars.size(); pos++) {
            if (pos > 0) {
                sb.append(", ");
            }
            sb.append(chars.get(pos));
        }
        atomicValue(handler, elemName, sb.toString());
    }

    private List<Token> couldBeNext(Set<TerminalSymbol> symbols) {
        ArrayList<Token> next = new ArrayList<>();
        for (TerminalSymbol symbol : symbols) {
            if (symbol.getToken() != null) {
                next.add(symbol.getToken());
            }
        }
        return next;
    }

    private List<Token> couldBeNext(EarleyChart chart, Grammar grammar) {
        ArrayList<Token> next = new ArrayList<>();
        List<TerminalSymbol> symbols = couldBeNextSymbols(chart, grammar);
        for (TerminalSymbol symbol : symbols) {
            if (symbol.getToken() != null) {
                next.add(symbol.getToken());
            }
        }
        return next;
    }

    private List<TerminalSymbol> couldBeNextSymbols(EarleyChart chart, Grammar grammar) {
        ArrayList<TerminalSymbol> nextChars = new ArrayList<>();
        HashSet<TerminalSymbol> nextSet = new HashSet<>();

        int lastrow = chart.size() - 1;
        while (lastrow >= 0 && chart.get(lastrow).isEmpty()) {
            lastrow--;
        }

        if (lastrow < 0 || chart.get(lastrow).isEmpty()) {
            return nextChars;
        }

        HashSet<Symbol> nextSymbols = new HashSet<>();
        for (EarleyItem item : chart.get(lastrow)) {
            State state = item.state;
            if (state != null && !state.completed()) {
                if (state.nextSymbol() instanceof TerminalSymbol) {
                    nextSet.add((TerminalSymbol) state.nextSymbol());
                } else {
                    nextSymbols.add(state.nextSymbol());
                }
            }
        }

        for (Symbol s: nextSymbols) {
            for (Rule rule : grammar.getRules()) {
                if (rule.getSymbol().equals(s) && !rule.getRhs().isEmpty()) {
                    if (rule.getRhs().get(0) instanceof TerminalSymbol) {
                        nextSet.add((TerminalSymbol) rule.getRhs().get(0));
                    }
                }
            }
        }

        nextChars.addAll(nextSet);
        return nextChars;
    }

    private void writeString(ContentHandler handler, String str) throws SAXException {
        handler.characters(str.toCharArray(), 0, str.length());
    }
}
