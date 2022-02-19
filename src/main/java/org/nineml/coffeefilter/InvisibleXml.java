package org.nineml.coffeefilter;

import org.nineml.coffeefilter.utils.CommonBuilder;
import org.nineml.coffeefilter.utils.Sniff;
import org.nineml.coffeegrinder.exceptions.CoffeeGrinderException;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.ParseTree;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.xml.sax.SAXException;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeefilter.model.IxmlContentHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * A static class for constructing instances of Invisible XML grammars.
 */
public class InvisibleXml {
    private static InvisibleXmlParser xixmlForIxml = null;

    /**
     * The parser options.
     * <p>These options are passed to the parsers constructed. Constructed parsers
     * get a copy of the options, changing them on the <code>InvisibleXml</code> class after
     * construction will have no effect.</p>
     */
    public static final ParserOptions options = new ParserOptions();

    private InvisibleXml() {
        // no one makes one of these
    }

    private InvisibleXmlParser getIxmlParser() {
        if (xixmlForIxml == null) {
            try {
                String name = "/org/nineml/coffeefilter/ixml.cxml";
                URL resource = getClass().getResource(name);
                InputStream stream = getClass().getResourceAsStream(name);

                if (stream == null || resource == null) {
                    name = "/org/nineml/coffeefilter/ixml.xml";
                    resource = getClass().getResource(name);
                    stream = getClass().getResourceAsStream(name);

                    if (stream == null || resource == null) {
                        throw IxmlException.failedToLoadIxmlGrammar(name);
                    }

                    xixmlForIxml = getParserFromVxml(stream, resource.toString());
                } else {
                    xixmlForIxml = getParserFromCxml(stream, resource.toString());
                }
            } catch (IOException ex) {
                throw IxmlException.failedToLoadIxmlGrammar(ex);
            }
        }
        return xixmlForIxml;
    }

    /**
     * The parser for ixml grammars.
     * @return A parser for the ixml specification grammar.
     */
    public static InvisibleXmlParser getParser() {
        if (xixmlForIxml == null) {
            return new InvisibleXml().getIxmlParser();
        }
        return xixmlForIxml;
    }

    /**
     * Get a parser from a URI.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * Sniffs the first 4095 bytes of the input to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar. Assumes the input is in UTF-8.</p>
     * @param source the grammar source.
     * @return a parser for that grammar
     * @throws IOException if attempting to open or read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(URI source) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return getParser(conn.getInputStream(), source.toString(), "UTF-8");
    }

    /**
     * Get a parser from a file.
     * <p>Sniffs the first 4095 bytes of the input to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar. Assumes the input is in UTF-8.</p>
     * @param source the grammar source.
     * @return a parser for that grammar
     * @throws IOException if attempting to open or read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(File source) throws IOException {
        return getParser(new FileInputStream(source), source.toURI().toString(), "UTF-8");
    }

    /**
     * Get a parser from a URI with an explicit encoding.
     * <p>Attempts to read from the URI with <code>source.toURL().openConnection()</code>.
     * Sniffs the first 4095 bytes of the input to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar. The encoding is irrelevant for vxml or
     * compiled grammars.</p>
     * @param source the grammar source.
     * @param encoding the character encoding.
     * @return a parser for that grammar
     * @throws IOException if attempting to open or read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(URI source, String encoding) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return getParser(conn.getInputStream(), encoding);
    }

    /**
     * Get a parser from a file with an explicit encoding.
     * <p>Sniffs the first 4095 bytes of the input to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar.The encoding is irrelevant for vxml or
     * compiled grammars.</p>
     * @param source the grammar source.
     * @param encoding the encoding
     * @return a parser for that grammar
     * @throws IOException if attempting to open or read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(File source, String encoding) throws IOException {
        return getParser(new FileInputStream(source), source.toURI().toString(), encoding);
    }

    /**
     * Get a parser from an input stream.
     * <p>Sniffs the first 4095 bytes of the input stream to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar. Assumes the input is in UTF-8.</p>
     * @param stream the grammar source
     * @param systemId the system identifier (for XML grammars)
     * @return a parser for that grammar
     * @throws IOException if attempting to read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(InputStream stream, String systemId) throws IOException {
        return getParser(stream, systemId, "UTF-8");
    }

    /**
     * Get a parser from an input stream with an explicit encoding.
     * <p>Sniffs the first 4095 bytes of the input stream to identify the input as an ixml grammar,
     * a vxml grammar, or a compiled grammar. The encoding is irrelevant for vxml or
     * compiled grammars.</p>
     * @param stream the grammar source
     * @param systemId the system identifier (for XML grammars)
     * @param encoding the character encoding.
     * @return a parser for that grammar
     * @throws IOException if attempting to open or read the source fails
     * @throws IxmlException if the source cannot be identified or is not a valid grammar
     */
    public static InvisibleXmlParser getParser(InputStream stream, String systemId, String encoding) throws IOException {
        BufferedInputStream bufstream = new BufferedInputStream(stream, 8192);
        bufstream.mark(4096);
        byte[] buf = new byte[4095];
        int len = bufstream.read(buf, 0, buf.length);
        bufstream.reset();

        int sourceType = Sniff.identify(buf, 0, buf.length);
        switch (sourceType) {
            case Sniff.VXML_SOURCE:
                return getParserFromVxml(bufstream, systemId);
            case Sniff.CXML_SOURCE:
                return getParserFromCxml(bufstream, systemId);
            case Sniff.IXML_SOURCE:
                return getParserFromIxml(bufstream, encoding);
            default:
                throw IxmlException.sniffingFailed(systemId);
        }
    }

    /**
     * Construct a parser from a compiled CoffeeGrinder grammar.
     * @param stream The grammar
     * @param systemId A system identifier, which may be null
     * @return The parser
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser getParserFromCxml(InputStream stream, String systemId) throws IOException {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            long startMillis = Calendar.getInstance().getTimeInMillis();
            Grammar grammar = compiler.parse(stream, systemId);
            Ixml ixml = new Ixml(grammar);
            long parseMillis = Calendar.getInstance().getTimeInMillis() - startMillis;
            InvisibleXmlParser parser = new InvisibleXmlParser(ixml, parseMillis);
            parser.setOptions(new ParserOptions(options));
            return parser;
        } catch (CoffeeGrinderException ex) {
            throw IxmlException.failedtoParse(systemId, ex);
        }
    }

    /**
     * Construct a parser from an XML representation (vxml) of an ixml grammar
     * @param stream the input stream
     * @param systemId The system ID of an XML document containing an Invisible XML vxml grammar.
     * @return A parser for that grammar.
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser getParserFromVxml(InputStream stream, String systemId) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        IxmlContentHandler handler = new IxmlContentHandler();
        try {
            SAXParser parser = factory.newSAXParser();
            long startMillis = Calendar.getInstance().getTimeInMillis();
            parser.parse(stream, handler, systemId);
            Ixml ixml = handler.getIxml();
            long parseMillis = Calendar.getInstance().getTimeInMillis() - startMillis;
            InvisibleXmlParser iparser = new InvisibleXmlParser(ixml, parseMillis);
            iparser.setOptions(new ParserOptions(options));
            return iparser;
        } catch (ParserConfigurationException|SAXException ex) {
            throw IxmlException.failedtoParse(systemId, ex);
        } catch (CoffeeGrinderException ex) {
            throw IxmlException.failedtoParse(systemId, ex);
        }
    }

    /**
     * Constructs a parser from an ixml grammar.
     * @param stream A stream returning an ixml grammar.
     * @param charset The character set of the grammar file.
     * @return A parser for the grammar.
     * @throws IOException If an error occurs reading the stream or if the character set is unsupported.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser getParserFromIxml(InputStream stream, String charset) throws IOException {
        ParserOptions opts = new ParserOptions(options);

        InvisibleXmlParser ixmlParser = getParser();
        ixmlParser.setOptions(opts);

        InvisibleXmlDocument doc = ixmlParser.parse(stream, charset);
        if (doc.getNumberOfParses() == 0) {
            return new InvisibleXmlParser(doc, doc.parseTime());
        }

        ParseTree tree = doc.getEarleyResult().getForest().parse();
        CommonBuilder builder = new CommonBuilder(tree, doc.getEarleyResult(), opts);

        try {
            IxmlContentHandler handler = new IxmlContentHandler();
            builder.build(handler);
            Ixml ixml = handler.getIxml();
            return new InvisibleXmlParser(ixml, doc.getEarleyResult().getParseTime());
        } catch (Exception ex) {
            return new InvisibleXmlParser(doc, ex, doc.parseTime());
        }
    }

    /**
     * Constructs a parser from an ixml grammar.
     * @param input An input string that contains an ixml grammar.
     * @return A parser for the grammar.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser getParserFromIxml(String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        try {
            return getParserFromIxml(bais, "UTF-8");
        } catch (IOException ex) {
            throw IxmlException.internalError("I/O error parsing a string in memory (!?)");
        }
    }
}
