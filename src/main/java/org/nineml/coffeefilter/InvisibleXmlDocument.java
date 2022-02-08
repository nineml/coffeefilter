package org.nineml.coffeefilter;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.DefaultTreeWalker;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.nineml.coffeefilter.utils.CommonBuilder;
import org.nineml.coffeefilter.utils.TrivialContentHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;

/**
 * An InvisibleXmlDocument represents a document created with an {@link InvisibleXmlParser}.
 *
 * <p>From this object, you can obtain the parsed representation(s) of the document. For ambiguous grammars,
 * there may be more than one.</p>
 */
public class InvisibleXmlDocument {
    private final EarleyResult result;
    private final boolean prefixOk;
    private final ParserOptions options;
    private final TreeWalker treeWalker;
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
     * Return the underlying {@link EarleyResult} result for this parse.
     * @return the result
     */
    public EarleyResult getEarleyResult() {
        return result;
    }

    /**
     * Return the number of successful parses of this document.
     *
     * <p>Will return 0 if there are no successful parses.</p>
     *
     * @return The number of parses.
     */
    public long numberOfParses() {
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
    public BigInteger exactNumberOfParses() {
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
        CommonBuilder builder = new CommonBuilder(tree);
        TrivialContentHandler handler = new TrivialContentHandler(options.prettyPrint);
        realize(builder, handler);
        return handler.getXml();
    }

    /**
     * Write an XML representation of the current parse to the stream.
     * @param output the output stream.
     */
    public void getTree(PrintStream output) {
        ParseTree tree = getParseTree();
        CommonBuilder builder = new CommonBuilder(tree);
        TrivialContentHandler handler = new TrivialContentHandler(output, options.prettyPrint);
        realize(builder, handler);
    }

    /**
     * Write an XML representation of the current parse to a file.
     * @param filename the output filename.
     */
    public void getTree(String filename) {
        try {
            PrintStream stream = new PrintStream(new FileOutputStream(filename));
            getTree(stream);
        } catch (IOException ex) {
            throw new IxmlException("Failed to write file: " + filename, ex);
        }
    }

    /**
     * Write an XML representation of the current parse to a SAX ContentHandler.
     * @param handler the content handler.
     */
    public void getTree(ContentHandler handler) {
        ParseTree tree = getParseTree();
        CommonBuilder builder = new CommonBuilder(tree);
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

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("http://invisiblexml.org/NS", "ixml:state", "failed");
            handler.startElement("", "failed", "failed", attrs);

            attrs = new AttributeBuilder();
            if (lineNumber > 0) {
                attrs.addAttribute("line", ""+lineNumber);
            }
            if (columnNumber > 0) {
                attrs.addAttribute("column", ""+columnNumber);
            }
            attrs.addAttribute("token-count", ""+result.getTokenCount());
            handler.startElement("",  "last-token", "last-token", attrs);
            writeString(handler, result.getLastToken().toString());
            handler.endElement("", "last-token", "last-token");

            attrs = new AttributeBuilder();
            handler.startElement("", "chart", "chart", attrs);

            for (int row = 0; row < result.getChart().size(); row++) {
                if (!result.getChart().get(row).isEmpty()) {
                    attrs = new AttributeBuilder();
                    attrs.addAttribute("n", ""+row);
                    handler.startElement("", "row", "row", attrs);

                    attrs = new AttributeBuilder();
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
            handler.endElement("", "failed", "failed");
            handler.endDocument();
        } catch (SAXException ex) {
            throw new IxmlException("Failed to create XML: " + ex.getMessage(), ex);
        }
    }

    private void writeString(ContentHandler handler, String str) throws SAXException {
        handler.characters(str.toCharArray(), 0, str.length());
    }
}
