package org.nineml.coffeefilter;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeefilter.model.IxmlCompiler;
import org.nineml.coffeefilter.model.IxmlContentHandler;
import org.nineml.coffeefilter.utils.CharacterIterator;
import org.nineml.coffeefilter.utils.CommonBuilder;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.ParseTree;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.Iterator;

/**
 * A parser for a particular Invisible XML grammar.
 */
public class InvisibleXmlParser {
    private final Ixml ixml;
    private final InvisibleXmlDocument failedParse;
    private final long parseTime;
    private EarleyParser parser = null;
    private ParserOptions options = new ParserOptions();

    protected InvisibleXmlParser(Ixml ixml) {
        this.ixml = ixml;
        this.parseTime = -1;
        failedParse = null;
    }

    protected InvisibleXmlParser(Ixml ixml, long parseMillis) {
        this.ixml = ixml;
        this.parseTime = parseMillis;
        failedParse = null;
    }

    protected InvisibleXmlParser(InvisibleXmlDocument failed, long parseMillis) {
        ixml = null;
        parseTime = parseMillis;
        failedParse = failed;
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
     * Get the time spent parsing the input grammar.
     * <p>This returns the number of milliseconds of "wall clock time" spent by the processor
     * constructing this parser.</p>
     * @return the time in milliseconds
     */
    public long getParseTime() {
        return parseTime;
    }

    /**
     * Did the grammar parse succeed?
     * @return true if the parse succeeded
     */
    public boolean constructed() {
        return failedParse == null;
    }

    /**
     * If the attempt to parse the grammar failed, return a representation of that failure.
     * @return The failed parse, or null if the parse succeeded.
     */
    public InvisibleXmlDocument getFailedParse() {
        return failedParse;
    }

    /**
     * Get a document from a URI.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * Assumes the input is in UTF-8.</p>
     * @param source the source
     * @return a document
     * @throws IOException If the source cannot be read
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(URI source) throws IOException {
        return parse(source, "UTF-8");
    }

    /**
     * Get a document from a file.
     * <p>Assumes the input is in UTF-8.</p>
     * @param source the source
     * @return a document
     * @throws IOException If the source cannot be read
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(File source) throws IOException {
        return parse(source, "UTF-8");
    }

    /**
     * Get a document from a URI with an explicit encoding.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * </p>
     * @param source the source
     * @param encoding the encoding
     * @return a document
     * @throws IOException If the stream cannot be read or if the character set is unsupported.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(URI source, String encoding) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return parse(conn.getInputStream(), encoding);

    }

    /**
     * Get a document from a file with an explicit encoding.
     * @param source the source
     * @param encoding the encoding
     * @return a document
     * @throws IOException If the stream cannot be read or if the character set is unsupported.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(File source, String encoding) throws IOException {
        return parse(new FileInputStream(source), encoding);
    }

    /**
     * Get a document from a stream.
     * @param stream The input.
     * @param encoding The input encoding.
     * @return A document.
     * @throws IOException If the stream cannot be read or if the character set is unsupported.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(InputStream stream, String encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, encoding);
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int len = reader.read(buffer);
        while (len >= 0) {
            sb.append(buffer, 0, len);
            len = reader.read(buffer);
        }
        return parse(sb.toString());
    }

    /**
     * Get a document from a string.
     * @param input The input.
     * @return A document.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(String input) {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }

        Grammar grammar = ixml.getGrammar();
        grammar.setParserOptions(options);

        CharacterIterator iterator = new CharacterIterator(input);
        EarleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
        EarleyResult result = parser.parse(iterator);

        InvisibleXmlDocument doc;
        if (!result.succeeded() && result.prefixSucceeded() && options.ignoreTrailingWhitespace) {
            boolean ok = true;
            Iterator<Token> remaining = result.getContinuingIterator();
            while (remaining.hasNext()) {
                Token token = remaining.next();
                ok = ok && (token instanceof TokenCharacter) && Character.isWhitespace(((TokenCharacter) token).getValue());
            }
            doc = new InvisibleXmlDocument(result, options, ok);
        } else {
            doc = new InvisibleXmlDocument(result, options);
        }

        doc.setLocation(iterator.offset, iterator.lineNumber, iterator.columnNumber);
        return doc;
    }

    /**
     * Get a string representation of the compiled grammar
     * @return the XML serialization of the compiled grammar
     * @throws NullPointerException if this parser has no grammar
     */
    public String getCompiledParser() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        IxmlCompiler compiler = new IxmlCompiler();
        return compiler.compile(ixml.getGrammar());
    }

    /**
     * Get the underlying grammar
     * @return the underlying CoffeeGrinder grammar
     * @throws NullPointerException if this parser has no grammar
     */
    public Grammar getGrammar() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        return ixml.getGrammar();
    }
}
