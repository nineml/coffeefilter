package org.nineml.coffeefilter;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.IPragma;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeefilter.model.IxmlContentHandler;
import org.nineml.coffeefilter.util.GrammarSniffer;
import org.nineml.coffeefilter.util.IxmlInputBuilder;
import org.nineml.coffeegrinder.exceptions.CoffeeGrinderException;
import org.nineml.coffeegrinder.parser.HygieneReport;
import org.nineml.coffeegrinder.tokens.Token;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A static class for constructing instances of Invisible XML grammars.
 */
public class InvisibleXml {
    /** The category name used for InvisibleXml log messages. */
    public static final String logcategory = "InvisibleXml";

    /** The internal name for the xmlns parser attribute. */
    public static final String XMLNS_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/xmlns";
    /** The internal name for the name parser attribute. */
    public static final String NAME_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/name";
    /** The internal name for the mark parser attribute. */
    public static final String MARK_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/mark";
    /** The internal name for the tmark parser attribute. */
    public static final String TMARK_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/tmark";
    /** The internal name for the insertion parser attribute. */
    public static final String INSERTION_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/insertion";
    /** The internal name for the discard parser attribute. */
    public static final String DISCARD_ATTRIBUTE = "https://coffeefilter.nineml.org/attr/discard";

    /** The namespace prefix for the Invisilble XML namespace. */
    public static final String ixml_prefix = "ixml";
    /** The Invisible XML namespace URI. */
    public static final String ixml_ns = "http://invisiblexml.org/NS";
     /** The namespace prefix for the NineML namespace. */
    public static final String nineml_prefix = "n";
     /** THe NineML namespace URI. */
    public static final String nineml_ns = "https://nineml.org/ns/";

    /** The iXML "ambiguous" state .*/
    public static final String AMBIGUOUS = "ambiguous";
    /** The iXML "version-mismatch" state. */
    public static final String VERSION_MISMATCH = "version-mismatch";

    private static final String ixml_ixml = "/org/nineml/coffeefilter/ixml2.xml";
    private static final String pragmas_ixml = "/org/nineml/coffeefilter/pragmas2.xml";
    private static final String UTF_8 = "UTF-8";

    private final InvisibleXmlParser ixmlForIxml;
    private final ParserOptions options;

    /**
     * Creates a new InvisibleXml object (with default options) from which parsers can be constructed.
     * <p>Attempts to load the Invisible XML parser for Invisible XML from
     * resources. This constructor uses default options.</p>
     * @throws IxmlException if the Invisible XML parser for Invisible XML cannot be loaded
     */
    public InvisibleXml() {
        this(new ParserOptions());
    }

    /**
     * Creates a new InvisibleXml object (with custom options) from which parsers can be constructed.
     * <p>Attempts to load the Invisible XML parser for Invisible XML from
     * resources.</p>
     * @param options the parser options
     * @throws IxmlException if the Invisible XML parser for Invisible XML cannot be loaded
     */
    public InvisibleXml(ParserOptions options) {
        String ixml = ixml_ixml;
        if (!options.getPedantic()) {
            ixml = pragmas_ixml;
        }

        this.options = options;
        try {
            URL resource = getClass().getResource(ixml);
            InputStream stream = getClass().getResourceAsStream(ixml);

            if (stream == null || resource == null) {
                options.getLogger().debug(logcategory, "Failed to load %s", ixml);
                throw IxmlException.failedToLoadIxmlGrammar(ixml);
            }

            ixmlForIxml = getParserFromVxml(stream, resource.toString());
        } catch (IOException ex) {
            throw IxmlException.failedToLoadIxmlGrammar(ex);
        }
    }

    /**
     * The known pragma names.
     * @return the list of pragma names known to the processor
     */
    public static List<String> knownPragmas() {
        ArrayList<String> list = new ArrayList<>();
        for (IPragma.PragmaType ptype : IPragma.pragmaTypeNames.keySet()) {
            if (ptype != IPragma.PragmaType.UNDEFINED) {
                list.add(IPragma.pragmaTypeNames.get(ptype));
            }
        }
        return list;
    }

    /**
     * The known states.
     * @return the list of states known to the processor.
     */
    public static List<String> knownStates() {
        ArrayList<String> list = new ArrayList<>();
        list.add(AMBIGUOUS);
        list.add(VERSION_MISMATCH);
        return list;
    }

    /**
     * Get the parser options currently being used to construct parsers.
     * <p>Changing properties on the options will only effect subsequently constructed
     * parsers. Each parser gets its own copy of the options.</p>
     * @return the current options
     */
    public ParserOptions getOptions() {
        return options;
    }

    /**
     * The parser for ixml grammars.
     * @return A parser for the ixml specification grammar.
     */
    public InvisibleXmlParser getParser() {
        return ixmlForIxml;
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
    public InvisibleXmlParser getParser(URI source) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return getParser(conn.getInputStream(), source.toString(), UTF_8);
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
    public InvisibleXmlParser getParser(File source) throws IOException {
        return getParser(Files.newInputStream(source.toPath()), source.toURI().toString(), UTF_8);
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
    public InvisibleXmlParser getParser(URI source, String encoding) throws IOException {
        URLConnection conn = source.toURL().openConnection();
        return getParser(conn.getInputStream(), source.toString(), encoding);
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
    public InvisibleXmlParser getParser(File source, String encoding) throws IOException {
        return getParser(Files.newInputStream(source.toPath()), source.toURI().toString(), encoding);
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
    public InvisibleXmlParser getParser(InputStream stream, String systemId) throws IOException {
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
    public InvisibleXmlParser getParser(InputStream stream, String systemId, String encoding) throws IOException {
        BufferedInputStream bufstream = new BufferedInputStream(stream, 8192);
        bufstream.mark(4096);
        byte[] buf = new byte[4095];
        int len = bufstream.read(buf, 0, buf.length);
        bufstream.reset();

        int sourceType = GrammarSniffer.identify(buf, 0, buf.length);
        switch (sourceType) {
            case GrammarSniffer.VXML_SOURCE:
                options.getLogger().debug(logcategory, "Loading %s (VXML)", systemId);
                return getParserFromVxml(bufstream, systemId);
            case GrammarSniffer.IXML_SOURCE:
                options.getLogger().debug(logcategory, "Loading %s with %s encoding", systemId, encoding);
                return getParserFromIxml(bufstream, encoding);
            default:
                options.getLogger().info(logcategory, "Failed to detect grammar type for %s", systemId);
                throw IxmlException.sniffingFailed(systemId);
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
    public InvisibleXmlParser getParserFromVxml(InputStream stream, String systemId) throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        IxmlContentHandler handler = new IxmlContentHandler(options);
        try {
            SAXParser parser = factory.newSAXParser();
            long startMillis = Calendar.getInstance().getTimeInMillis();
            parser.parse(stream, handler, systemId);
            Ixml ixml = handler.getIxml();
            long parseMillis = Calendar.getInstance().getTimeInMillis() - startMillis;
            return new InvisibleXmlParser(ixml, options, parseMillis);
        } catch (ParserConfigurationException | SAXException | CoffeeGrinderException ex) {
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
    public InvisibleXmlParser getParserFromIxml(InputStream stream, String charset) throws IOException {
        InvisibleXmlParser ixmlParser = getParser();

        Token[] input = IxmlInputBuilder.fromString(ixmlParser.readInputStream(stream, charset));
        InvisibleXmlDocument doc = ixmlParser.parse(input);
        if (doc.getNumberOfParses() == 0) {
            return new InvisibleXmlParser(doc, options, doc.parseTime());
        }

        ParserOptions builderOptions = new ParserOptions(options);
        builderOptions.setShowMarks(false);
        builderOptions.setShowBnfNonterminals(false);
        builderOptions.setAssertValidXmlNames(false);
        builderOptions.setAssertValidXmlCharacters(true);

        try {
            IxmlContentHandler handler = new IxmlContentHandler(builderOptions);
            doc.getTree(handler, builderOptions);
            Ixml ixml = handler.getIxml();

            InvisibleXmlParser parser = new InvisibleXmlParser(ixml, options, doc.getResult().getParseTime());

            HygieneReport report = parser.getHygieneReport();
            if (!report.isClean()) {
                if (!report.getUndefinedSymbols().isEmpty() && !options.getAllowUndefinedSymbols()) {
                    // Treat this like a failed parse.
                    parser = new InvisibleXmlParser(doc, IxmlException.undefinedSymbols(report.getUndefinedSymbols()), doc.parseTime());
                }
                if (!report.getUnreachableSymbols().isEmpty() && !options.getAllowUnreachableSymbols()) {
                    // Treat this like a failed parse.
                    parser = new InvisibleXmlParser(doc, IxmlException.unreachableSymbols(report.getUnreachableSymbols()), doc.parseTime());
                }
                if (!report.getUnproductiveRules().isEmpty() && !(options.getAllowUnproductiveSymbols())) {
                    // Treat this like a failed parse.
                    parser = new InvisibleXmlParser(doc, IxmlException.unproductiveSymbols(report.getUnproductiveSymbols()), doc.parseTime());
                }
            }

            return parser;
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
    public InvisibleXmlParser getParserFromIxml(String input) {
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        final InvisibleXmlParser parser;
        try {
            parser = getParserFromIxml(bais, UTF_8);
        } catch (IOException ex) {
            throw IxmlException.internalError("I/O error parsing a string in memory (!?)");
        }
        return parser;
    }
}
