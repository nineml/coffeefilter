package org.nineml.coffeefilter;

import org.nineml.coffeegrinder.exceptions.CoffeeGrinderException;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.xml.sax.SAXException;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.Ixml;
import org.nineml.coffeefilter.model.IxmlContentHandler;
import org.nineml.coffeefilter.utils.URIUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * A static class for constructing instances of Invisible XML grammars.
 */
public class InvisibleXml {
    private static InvisibleXmlParser ixmlForIxml = null;

    private InvisibleXml() {
        if (ixmlForIxml == null) {
            try {
                String name = "/org/nineml/coffeefilter/ixml.cxml";
                URL resource = getClass().getResource(name);
                InputStream stream = getClass().getResourceAsStream(name);

                if (stream == null || resource == null) {
                    name = "/org/nineml/coffeefilter/ixml.xml";
                    resource = getClass().getResource(name);
                    stream = getClass().getResourceAsStream(name);

                    if (stream == null || resource == null) {
                        throw new IxmlException("Failed to load ixml specification grammar: " + name);
                    }

                    ixmlForIxml = parseXml(stream, resource.toString());
                } else {
                    ixmlForIxml = parseCompiledXml(stream, resource.toString());
                }
            } catch (IOException ex) {
                throw new IxmlException("Failed to load ixml specification grammar", ex);
            }
        }
    }

    /**
     * The parser for ixml grammars.
     * @return A parser for the ixml specification grammar.
     */
    public static InvisibleXmlParser invisibleXmlParser() {
        if (ixmlForIxml == null) {
            new InvisibleXml();
        }
        return ixmlForIxml;
    }

    /**
     * Construct a parser from a compiled CoffeeGrinder grammar.
     * @param href A system identifier, which may be null
     * @return The parser
     * @throws URISyntaxException if the href cannot be converted to a URI
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromCxml(String href) throws IOException, URISyntaxException {
        URI systemId = URIUtils.cwd().resolve(href);
        URLConnection conn = systemId.toURL().openConnection();
        InvisibleXml invisibleXml = new InvisibleXml();
        return invisibleXml.parseCompiledXml(conn.getInputStream(), href);
    }

    /**
     * Construct a parser from a compiled CoffeeGrinder grammar.
     * @param stream The grammar
     * @param systemId A system identifier, which may be null
     * @return The parser
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromCxml(InputStream stream, String systemId) throws IOException {
        InvisibleXml invisibleXml = new InvisibleXml();
        return invisibleXml.parseCompiledXml(stream, systemId);
    }

    private InvisibleXmlParser parseCompiledXml(InputStream stream, String systemId) throws IOException {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            long startMillis = Calendar.getInstance().getTimeInMillis();
            Grammar grammar = compiler.parse(stream, systemId);
            Ixml ixml = new Ixml(grammar);
            long parseMillis = Calendar.getInstance().getTimeInMillis() - startMillis;
            return new InvisibleXmlParser(ixml, parseMillis);
        } catch (CoffeeGrinderException ex) {
            throw new IxmlException("Failed to parse " + systemId, ex);
        }
    }

    /**
     * Construct a parser from an XML representation (vxml) of an ixml grammar
     * @param href A system identifier, which may be null
     * @return The parser
     * @throws URISyntaxException if the href cannot be converted to a URI
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromVxml(String href) throws IOException, URISyntaxException {
        URI systemId = URIUtils.cwd().resolve(href);
        URLConnection conn = systemId.toURL().openConnection();
        return parserFromVxml(conn.getInputStream(), href);
    }

    /**
     * Construct a parser from an XML representation (vxml) of an ixml grammar
     * @param stream the input stream
     * @param systemId The system ID of an XML document containing an Invisible XML vxml grammar.
     * @return A parser for that grammar.
     * @throws IOException if the input cannot be read
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromVxml(InputStream stream, String systemId) throws IOException {
        InvisibleXml invisibleXml = new InvisibleXml();
        return invisibleXml.parseXml(stream, systemId);
    }

    /**
     * Construct a parser from an XML representation (vxml) of an ixml grammar
     * @param vxml The vxml string
     * @return A parser for that grammar
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromVxmlString(String vxml) {
        InvisibleXml invisibleXml = new InvisibleXml();
        ByteArrayInputStream bais = new ByteArrayInputStream(vxml.getBytes(StandardCharsets.UTF_8));
        try {
            return invisibleXml.parseXml(bais, null);
        } catch (IOException ex) {
            // this can't happen
            throw new IxmlException("IO exception reading byte array input stream?", ex);
        }
    }

    /**
     * Construct a parser from an ixml grammar.
     * <p>This method assumes the input file is encoded in UTF-8. See {@link #parserFromFile(String, String)}.</p>
     * @param filename The grammar.
     * @return A parser for the grammar.
     * @throws IOException if the file cannot be read or isn't UTF-8.
     * @throws URISyntaxException if the filename cannot be transformed into a valid URI.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromFile(String filename) throws IOException, URISyntaxException {
        return parserFromFile(filename, "UTF-8");
    }

    /**
     * Constructs a parser from an ixml grammar.
     * @param filename The grammar.
     * @param charset The character set of the grammar file.
     * @return A parser for the grammar.
     * @throws URISyntaxException if the filename cannot be transformed into a valid URI
     * @throws IOException If the file cannot be read or if the character set is unsupported.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromFile(String filename, String charset) throws IOException, URISyntaxException {
        URI systemId = URIUtils.cwd().resolve(filename);
        URLConnection conn = systemId.toURL().openConnection();
        return parserFromStream(conn.getInputStream(), charset);
    }

    /**
     * Constructs a parser from an ixml grammar.
     * @param stream A stream returning an ixml grammar.
     * @param charset The character set of the grammar file.
     * @return A parser for the grammar.
     * @throws IOException If an error occurs reading the stream or if the character set is unsupported.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromStream(InputStream stream, String charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, charset);
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int len = reader.read(buffer);
        while (len >= 0) {
            sb.append(buffer, 0, len);
            len = reader.read(buffer);
        }
        return parserFromString(sb.toString());
    }

    /**
     * Constructs a parser from an ixml grammar.
     * @param input An input string that contains an ixml grammar.
     * @return A parser for the grammar.
     * @throws IxmlException if the input is not an ixml grammar
     */
    public static InvisibleXmlParser parserFromString(String input) {
        InvisibleXml invisibleXml = new InvisibleXml();
        return invisibleXml.parse(input);
    }

    private InvisibleXmlParser parse(String input) {
        return ixmlForIxml.getParser(input);
    }

    private InvisibleXmlParser parseXml(InputStream stream, String systemId) throws IOException {
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
            return new InvisibleXmlParser(ixml, parseMillis);
        } catch (ParserConfigurationException ex) {
            throw new IxmlException("Failed to create XML parser", ex);
        } catch (CoffeeGrinderException| SAXException ex) {
            throw new IxmlException("Failed to parse " + systemId, ex);
        }
    }
}
