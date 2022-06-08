package org.nineml.coffeefilter;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeefilter.model.IxmlCompiler;
import org.nineml.coffeefilter.model.IxmlContentHandler;
import org.nineml.coffeefilter.utils.CharacterIterator;
import org.nineml.coffeefilter.utils.CommonBuilder;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.gll.GllResult;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * A parser for a particular Invisible XML grammar.
 */
public class InvisibleXmlParser {
    private final Ixml ixml;
    private final InvisibleXmlDocument failedParse;
    private final long parseTime;
    private final ParserOptions options;
    private Exception exception = null;
    private HygieneReport hygieneReport = null;

    protected InvisibleXmlParser(Ixml ixml) {
        this.ixml = ixml;
        this.parseTime = -1;
        failedParse = null;
        options = ixml.getOptions();
    }

    protected InvisibleXmlParser(Ixml ixml, long parseMillis) {
        this.ixml = ixml;
        this.parseTime = parseMillis;
        failedParse = null;
        options = ixml.getOptions();
    }

    protected InvisibleXmlParser(InvisibleXmlDocument failed, long parseMillis) {
        ixml = null;
        parseTime = parseMillis;
        failedParse = failed;
        options = failed.getOptions();
    }

    protected InvisibleXmlParser(InvisibleXmlDocument failed, Exception exception, long parseMillis) {
        this(failed, parseMillis);
        this.exception = exception;
    }

    public String getIxmlVersion() {
        return ixml.getIxmlVersion();
    }

    /**
     * Get the parser options.
     *
     * @return the parser options
     */
    public ParserOptions getOptions() {
        return options;
    }

    /**
     * Set the parser options.
     *
     * @param options the parser options
    public void setOptions(ParserOptions options) {
        this.options = options;
    }
     */

    /**
     * Return the exception that caused an attempt to build a parser to fail.
     * <p>If the attempt was successful, or if failure did not raise an exception, <code>null</code>
     * will be returned.</p>
     *
     * @return the exception, or null if no exception is available
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get the time spent parsing the input grammar.
     * <p>This returns the number of milliseconds of "wall clock time" spent by the processor
     * constructing this parser.</p>
     *
     * @return the time in milliseconds
     */
    public long getParseTime() {
        return parseTime;
    }

    /**
     * Did the grammar parse succeed?
     *
     * @return true if the parse succeeded
     */
    public boolean constructed() {
        return failedParse == null;
    }

    /**
     * Get the hygiene report for this parser's grammar
     *
     * @return the hygiene report
     */
    public HygieneReport getHygieneReport() {
        if (hygieneReport == null && getGrammar() != null) {
            hygieneReport = getGrammar().checkHygiene("$$");
        }
        return hygieneReport;
    }

    /**
     * If the attempt to parse the grammar failed, return a representation of that failure.
     *
     * @return The failed parse, or null if the parse succeeded.
     */
    public InvisibleXmlDocument getFailedParse() {
        return failedParse;
    }

    /**
     * Get a document from a URI.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * Assumes the input is in UTF-8.</p>
     *
     * @param source the source
     * @return a document
     * @throws IOException          If the source cannot be read
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(URI source) throws IOException {
        return parse(source, "UTF-8");
    }

    /**
     * Get a document from a file.
     * <p>Assumes the input is in UTF-8.</p>
     *
     * @param source the source
     * @return a document
     * @throws IOException          If the source cannot be read
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(File source) throws IOException {
        return parse(source, "UTF-8");
    }

    /**
     * Get a document from a URI with an explicit encoding.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * </p>
     *
     * @param source   the source
     * @param encoding the encoding
     * @return a document
     * @throws IOException          If the stream cannot be read or if the character set is unsupported.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(URI source, String encoding) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return parse(conn.getInputStream(), encoding);

    }

    /**
     * Get a document from a file with an explicit encoding.
     *
     * @param source   the source
     * @param encoding the encoding
     * @return a document
     * @throws IOException          If the stream cannot be read or if the character set is unsupported.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(File source, String encoding) throws IOException {
        return parse(Files.newInputStream(source.toPath()), encoding);
    }

    /**
     * Get a document from a stream.
     *
     * @param stream   The input.
     * @param encoding The input encoding.
     * @return A document.
     * @throws IOException          If the stream cannot be read or if the character set is unsupported.
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
     *
     * @param input The input.
     * @return A document.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(String input) {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }

        Grammar grammar = ixml.getGrammar(options);

        ParserType parserType = "Earley".equals(options.getParserType()) ? ParserType.Earley : ParserType.GLL;

        CharacterIterator iterator = new CharacterIterator(input);
        GearleyParser parser = grammar.getParser(parserType, grammar.getNonterminal("$$"));
        GearleyResult result = parser.parse(iterator);

        InvisibleXmlDocument doc;
        if (parser.getParserType() == ParserType.Earley
            && !result.succeeded() && result.prefixSucceeded() && options.getIgnoreTrailingWhitespace()) {
            boolean ok = true;
            Iterator<Token> remaining = ((EarleyResult) result).getContinuingIterator();
            while (remaining.hasNext()) {
                Token token = remaining.next();
                ok = ok && (token instanceof TokenCharacter) && Character.isWhitespace(((TokenCharacter) token).getCodepoint());
            }
            doc = new InvisibleXmlDocument(result, ixml.getIxmlVersion(), options, ok);
        } else {
            doc = new InvisibleXmlDocument(result, ixml.getIxmlVersion(), options);
        }

        doc.setLocation(iterator.offset, iterator.lineNumber, iterator.columnNumber);
        return doc;
    }

    /**
     * Get a string representation of the compiled grammar.
     * @return the XML serialization of the compiled grammar
     * @throws NullPointerException if this parser has no grammar
     */
    public String getCompiledParser() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        IxmlCompiler compiler = new IxmlCompiler(options);
        return compiler.compile(ixml.getGrammar(options));
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
        return ixml.getGrammar(options);
    }
}

