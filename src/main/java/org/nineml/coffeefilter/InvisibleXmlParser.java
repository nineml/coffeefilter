package org.nineml.coffeefilter;

import org.nineml.coffeefilter.model.IPragma;
import org.nineml.coffeefilter.model.IPragmaMetadata;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;

/**
 * A parser for a particular Invisible XML grammar.
 */
public class InvisibleXmlParser {
    public static final String logcategory = "InvisibleXml";
    private static final int UnicodeBOM = 0xFEFF;
    private final Ixml ixml;
    private final long parseTime;
    private final ParserOptions options;
    private Exception exception = null;
    private HygieneReport hygieneReport = null;
    private InvisibleXmlDocument failedParse;
    private boolean shownMessage = false;

    protected InvisibleXmlParser(Ixml ixml, ParserOptions options) {
        this.ixml = ixml;
        this.parseTime = -1;
        failedParse = null;
        this.options = options;
    }

    protected InvisibleXmlParser(Ixml ixml, ParserOptions options, long parseMillis) {
        this.ixml = ixml;
        this.parseTime = parseMillis;
        failedParse = null;
        this.options = options;
    }

    protected InvisibleXmlParser(InvisibleXmlDocument failed, ParserOptions options, long parseMillis) {
        ixml = null;
        parseTime = parseMillis;
        failedParse = failed;
        this.options = options;
    }

    protected InvisibleXmlParser(InvisibleXmlDocument failed, Exception exception, long parseMillis) {
        this(failed, failed.getOptions(), parseMillis);
        this.exception = exception;
    }

    public String getIxmlVersion() {
        if (ixml == null) {
            return null;
        }
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
     * Get metadata.
     *
     * Pragmas in the prolog that aren't recognized by the processor are returned as
     * "data" pragmas associating a URI with (a list of) values.
     *
     * @return the metadata
     */
    public Map<String, List<String>> getMetadata() {
        if (ixml == null) {
            return Collections.emptyMap();
        }

        HashMap<String, List<String>> metadata = new HashMap<>();
        HashSet<String> seen = new HashSet<>();
        for (IPragma pragma : ixml.getPragmas()) {
            if (pragma instanceof IPragmaMetadata) {
                IPragmaMetadata meta = (IPragmaMetadata) pragma;
                if (!seen.contains(meta.getPragmaURI())) {
                    seen.add(meta.getPragmaURI());
                    ArrayList<String> values = new ArrayList<>();
                    for (IPragma xpragma : ixml.getPragmas()) {
                        if (xpragma instanceof IPragmaMetadata
                                && meta.getPragmaURI().equals(((IPragmaMetadata) xpragma).getPragmaURI())) {
                            values.add(xpragma.getPragmaData());
                        }
                    }
                    metadata.put(meta.getPragmaURI(), values);
                }
            }
        }
        return metadata;
    }

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
        if (hygieneReport != null) {
            return hygieneReport;
        }

        SourceGrammar grammar = getGrammar();
        if (grammar != null) {
            hygieneReport = grammar.getHygieneReport(grammar.getNonterminal("$$"));
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
        if (!shownMessage) {
            options.getLogger().info(logcategory, "Parsing %s ixml grammar from %s", encoding, source);
            shownMessage = true;
        }
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
        if (!shownMessage) {
            options.getLogger().info(logcategory, "Parsing %s ixml grammar from %s", encoding, source);
            shownMessage = true;
        }
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
        if (!shownMessage) {
            options.getLogger().debug(logcategory, "Parsing %s ixml grammar from input stream", encoding);
            shownMessage = true;
        }

        return parse(readInputStream(stream, encoding));
    }

    /**
     * Get a document from a string.
     *
     * @param input The input.
     * @return A document.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(String input) {
        Token[] buffer = new Token[input.codePointCount(0, input.length())];
        int pos = 0;
        for (int cp : input.codePoints().toArray()) {
            buffer[pos] = TokenCharacter.get(cp);
            pos++;
        }
        return parse(buffer);
    }

    /**
     * Get a document from an array of tokens.
     *
     * @param input The input.
     * @return A document.
     * @throws NullPointerException if this parser has no grammar
     */
    public InvisibleXmlDocument parse(Token[] input) {
        shownMessage = false;
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }

        SourceGrammar grammar = ixml.getGrammar(options);
        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("$$"));
        GearleyResult result = parser.parse(input);

        InvisibleXmlDocument doc;
        if (parser.getParserType() == ParserType.Earley
                && !result.succeeded() && result.prefixSucceeded() && options.getIgnoreTrailingWhitespace()) {
            boolean ok = true;
            for (Token token : ((EarleyResult) result).getSuffix()) {
                ok = (token instanceof TokenCharacter) && Character.isWhitespace(((TokenCharacter) token).getCodepoint());
                if (!ok) {
                    break;
                }
            }
            doc = new InvisibleXmlDocument(result, ixml.getIxmlVersion(), options, ok);
        } else {
            doc = new InvisibleXmlDocument(result, ixml.getIxmlVersion(), options);
        }

        if (!doc.succeeded()) {
            failedParse = new InvisibleXmlFailureDocument(result, ixml.getIxmlVersion(), options);
            doc = failedParse;
        }

        return doc;
    }

    /**
     * Get the underlying grammar
     * @return the underlying CoffeeGrinder grammar
     * @throws NullPointerException if this parser has no grammar
     */
    public SourceGrammar getGrammar() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        return ixml.getGrammar(options);
    }

    public String readInputStream(InputStream stream, String charset) throws IOException {
        boolean ignoreBOM = options.getIgnoreBOM() && "utf-8".equalsIgnoreCase(charset);

        InputStreamReader reader = new InputStreamReader(stream, charset);
        StringBuilder sb = new StringBuilder();
        int inputchar = reader.read();

        if (inputchar != -1) {
            if (!ignoreBOM || inputchar != UnicodeBOM) {
                sb.append((char) inputchar);
            }
            inputchar = reader.read();
        }

        while (inputchar != -1) {
            sb.append((char) inputchar);
            inputchar = reader.read();
        }

        return sb.toString();
    }

}

