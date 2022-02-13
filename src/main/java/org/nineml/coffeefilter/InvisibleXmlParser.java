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
     * Constructs a document.
     *
     * <p>This method constructs a document by parsing the file
     * provided.</p>
     *
     * @param filename The input.
     * @return A document.
     * @throws IOException If the file cannot be read or isn't UTF-8.
     */
    public InvisibleXmlDocument parseFromFile(String filename) throws IOException {
        return parseFromFile(filename, "UTF-8");
    }

    /**
     * Constructs a document.
     *
     * <p>This method constructs a document by parsing the file
     * provided.</p>
     *
     * @param filename The input.
     * @param charset The input character set.
     * @return A document.
     * @throws IOException If the file cannot be read or if the character set is unsupported.
     */
    public InvisibleXmlDocument parseFromFile(String filename, String charset) throws IOException {
        return parseFromStream(new FileInputStream(filename), charset);
    }

    /**
     * Constructs a document.
     *
     * <p>This method constructs a document by parsing the stream
     * provided.</p>
     *
     * @param stream The input.
     * @param charset The input character set.
     * @return A document.
     * @throws IOException If the stream cannot be read or if the character set is unsupported.
     */
    public InvisibleXmlDocument parseFromStream(InputStream stream, String charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, charset);
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
     * Constructs a document.
     *
     * <p>This method constructs a document by parsing the stream
     * provided.</p>
     *
     * @param input The input.
     * @return A document.
     * @throws NullPointerException if this parser has no grammar (if it failed to construct one)
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
     * Get a parser from a string.
     * <p>The string is parsed as an Invisible XML grammar.</p>
     * @param input the grammar
     * @return the parser
     */
    protected InvisibleXmlParser getParser(String input) {
        InvisibleXmlDocument doc = parse(input);
        if (doc.getNumberOfParses() == 0) {
            return new InvisibleXmlParser(doc, doc.parseTime());
        }

        ParseTree tree = doc.getEarleyResult().getForest().parse();
        CommonBuilder builder = new CommonBuilder(tree);

        try {
            IxmlContentHandler handler = new IxmlContentHandler();
            builder.build(handler);
            Ixml ixml = handler.getIxml();
            return new InvisibleXmlParser(ixml, doc.getEarleyResult().getParseTime());
        } catch (Exception ex) {
            throw new IxmlException("Failed to parse grammar: " + ex.getMessage(), ex);
        }
    }

    /**
     * Get a string representation of the compiled grammar
     * @return the XML serialization of the compiled grammar
     */
    public String getCompiledParser() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        IxmlCompiler compiler = new IxmlCompiler();
        return compiler.compile(ixml.getGrammar());
    }

    /**
     * Load a compiled grammar.
     * @param compiled the compiled grammar.
     * @return a parser for the grammar.
     * @throws IOException If the file cannot be read.
     */
    public static InvisibleXmlParser loadCompiledGrammar(File compiled) throws IOException {
        IxmlCompiler compiler = new IxmlCompiler();
        return new InvisibleXmlParser(compiler.parse(compiled));
    }

    /**
     * Construct a parser from a compiled grammar.
     * @param compiled the XML serailization of the compiled grammar
     * @return a parser for the grammar.
     */
    public static InvisibleXmlParser parseCompiledGrammar(String compiled) {
        IxmlCompiler compiler = new IxmlCompiler();
        return new InvisibleXmlParser(compiler.parse(compiled));
    }

    public Grammar getGrammar() {
        if (ixml == null) {
            throw new NullPointerException("No grammar for this parser");
        }
        return ixml.getGrammar();
    }
}

