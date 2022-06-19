package org.nineml.coffeefilter;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.StringTreeBuilder;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.nineml.coffeefilter.utils.EventBuilder;
import org.nineml.coffeegrinder.gll.GllResult;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.PrintStream;
import java.util.*;

/**
 * An InvisibleXmlDocument represents a document created with an {@link InvisibleXmlParser}.
 *
 * <p>From this object, you can obtain the parsed representation(s) of the document. For ambiguous grammars,
 * there may be more than one.</p>
 */
public class InvisibleXmlDocument {
    private final GearleyResult result;
    private final boolean prefixOk;
    private final String parserVersion;
    private final EventBuilder eventBuilder;
    private final ParserOptions options;
    private int lineNumber = -1;
    private int columnNumber = -1;
    private int offset = -1;

    protected InvisibleXmlDocument(GearleyResult result, String parserVersion, ParserOptions options) {
        this.result = result;
        this.prefixOk = false;
        this.options = options;
        this.parserVersion = parserVersion;
        this.eventBuilder = new EventBuilder(parserVersion, options);
    }

    protected InvisibleXmlDocument(GearleyResult result, String parserVersion, ParserOptions options, boolean prefixOk) {
        this.result = result;
        this.prefixOk = prefixOk;
        this.options = options;
        this.parserVersion = parserVersion;
        this.eventBuilder = new EventBuilder(parserVersion, options);
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
        realize(handler);
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler
     * using a different set of options.
     * @param handler the content handler.
     * @param options the options.
     */
    public void getTree(ContentHandler handler, ParserOptions options) {
        ParserOptions saveOpts = eventBuilder.getOptions();
        eventBuilder.setOptions(options);
        realize(handler);
        eventBuilder.setOptions(saveOpts);
    }

    /**
     * Process the result with your own {@link TreeBuilder}.
     * @param builder the tree builder.
     */
    public void getTree(TreeBuilder builder) {
        if (result.succeeded() || (result.prefixSucceeded() && prefixOk)) {
            result.getTree(builder);
        }
    }

    public void reset() {
        eventBuilder.reset();
    }

    public boolean moreParses() {
        return eventBuilder.moreTrees();
    }

    public boolean nextTree() {
        if (moreParses()) {
            realize(new NopHandler());
        }
        return moreParses();
    }

    private void realize(ContentHandler handler) {
        if (result.succeeded() || (result.prefixSucceeded() && prefixOk)) {
            eventBuilder.setHandler(handler);
            result.getTree(eventBuilder);
        } else {
            realizeErrorDocument(handler);
        }
    }

    private void realizeErrorDocument(ContentHandler handler) {
        try {
            handler.startDocument();

            handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);

            AttributeBuilder attrs = new AttributeBuilder(options);
            attrs.addAttribute(InvisibleXml.ixml_ns, InvisibleXml.ixml_prefix + ":state", "failed");
            handler.startElement("", "fail", "failed", attrs);

            if (lineNumber > 0) {
                atomicValue(handler, "line", ""+lineNumber);
            }

            if (columnNumber > 0) {
                atomicValue(handler, "column", ""+columnNumber);
            }

            atomicValue(handler, "pos", ""+result.getTokenCount());

            if (result.getLastToken() == TokenEOF.EOF) {
                // This only happens for the GLL parser.
                atomicValue(handler, "end-of-input", "true");
            } else {
                TokenCharacter tchar = (TokenCharacter) result.getLastToken();
                if (tchar != null) {
                    if (result.getParser().hasMoreInput()) {
                        atomicValue(handler, "unexpected", ""+tchar.getValue());
                    } else {
                        atomicValue(handler, "end-of-input", "true");
                    }
                }
            }

            boolean predictedSome = false;
            List<Token> oknext = couldBeNext(result.getPredictedTerminals());
            if (!oknext.isEmpty()) {
                predictedSome = true;
                tokenList(handler, oknext, "permitted");
            }

            if (result instanceof EarleyResult) {
                EarleyResult eresult = (EarleyResult) result;
                oknext = couldBeNext(eresult.getChart(), result.getParser().getGrammar());
                if (!oknext.isEmpty()) {
                    String elemName = "permitted";
                    if (predictedSome) {
                        elemName = "also-predicted";
                    }
                    tokenList(handler, oknext, elemName);
                }

                if (options.getShowChart()) {
                    handler.startElement("", "chart", "chart", AttributeBuilder.EMPTY_ATTRIBUTES);

                    for (int row = 0; row < eresult.getChart().size(); row++) {
                        if (!eresult.getChart().get(row).isEmpty()) {
                            attrs = new AttributeBuilder(options);
                            attrs.addAttribute("n", ""+row);
                            handler.startElement("", "row", "row", attrs);

                            attrs = new AttributeBuilder(options);
                            for (EarleyItem item : eresult.getChart().get(row)) {
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

    private static class NopHandler implements ContentHandler {
        @Override
        public void setDocumentLocator(Locator locator) {
            // nop
        }

        @Override
        public void startDocument() throws SAXException {
            // nop
        }

        @Override
        public void endDocument() throws SAXException {
            // nop
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            // nop
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            // nop
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            // nop
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            // nop
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // nop
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // nop
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            // nop
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            // nop
        }
    }
}
