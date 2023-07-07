package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.ParserOptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Construct a "string tree".
 * <p>This builder can be passed to {@link InvisibleXmlDocument#getTree()} to build a string
 * represention of the document's underlying VXML.</p>
 */
public class StringTreeBuilder extends AbstractTreeBuilder {
    private static final int FIRST = 0;
    private static final int START_TAG = 1;
    private static final int IN_TAG = 2;
    private static final int END_TAG = 3;
    private static final int CHARS = 4;

    private static final int CR = 0x000D;
    private static final int LF = 0x000A;
    private static final int TAB = 0x0009;
    private static final int NEL = 0x0085;
    private static final int LINE_SEPARATOR = 0x2028;

    private String indent = "";
    private int state = FIRST;
    private ByteArrayOutputStream baos = null;
    private HashMap<String,String> prefixMapping = null;

    private final String iunit = "   ";
    private final PrintStream stream;

    /**
     * Create a string tree builder.
     * <p>If pretty printing is requested, additional newlines and whitespace are added
     * to create an indented view of the XML.</p>
     * @param options the parser options
     */
    public StringTreeBuilder(ParserOptions options) {
        super(options);
        baos = new ByteArrayOutputStream();
        try {
          stream = new PrintStream(baos, false, StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
          // this can never happen
          throw new RuntimeException(e);
        }
    }

    /**
     * Create a string tree builder, specifying an output stream.
     * <p>If pretty printing is requested, additional newlines and whitespace are added
     * to create an indented view of the XML.</p>
     * @param options the parser options
     * @param stream the output stream
     */
    public StringTreeBuilder(ParserOptions options, PrintStream stream) {
        super(options);
        this.stream = stream;
    }

    /**
     * Return a string containing the XML serialization.
     * <p>If a stream was provided to the constructor, this method will return null.</p>
     * @return the XML string, or null.
     */
    public String getXml() {
        if (baos == null) {
            return null;
        }
        String xml = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        baos = null;
        return xml;
    }

    @Override
    public void startPrefixMapping (String prefix, String uri)
            throws SAXException
    {
        if (prefixMapping == null) {
            prefixMapping = new HashMap<>();
        }
        prefixMapping.put(prefix,uri);
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        if (state == IN_TAG) {
            stream.print(">");
            state = START_TAG;
        }

        if (options.getPrettyPrint()) {
            switch (state) {
                case FIRST:
                    break;
                case START_TAG:
                case CHARS:
                case END_TAG:
                    stream.printf("%n");
                    stream.print(indent);
            }
            indent += iunit;
        }

        stream.printf("<%s", localName);

        if (prefixMapping != null) {
            for (String prefix : prefixMapping.keySet()) {
                String nsuri = prefixMapping.get(prefix);
                nsuri = nsuri.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
                nsuri = nsuri.replaceAll("'", "&apos;");
                if ("".equals(prefix)) {
                    stream.printf(" xmlns='%s'", nsuri);
                } else {
                    stream.printf(" xmlns:%s='%s'", prefix, nsuri);
                }
            }
            prefixMapping = null;
        }

        for (int pos = 0; pos < attributes.getLength(); pos++) {
            stream.print(" ");

            String qname = attributes.getQName(pos);
            String value = attributes.getValue(pos);

            StringBuilder sb = new StringBuilder();
            int offset = 0;
            int strlen = value.length();
            while (offset < strlen) {
                int ch = value.codePointAt(offset);
                offset += Character.charCount(ch);

                switch (ch) {
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '\'':
                        sb.append("&apos;");
                        break;
                    case CR:
                    case LF:
                    case TAB:
                    case NEL:
                    case LINE_SEPARATOR:
                        sb.append(String.format("&#x%X;", ch));
                        break;
                    default:
                        if (ch <= 0x1F || (ch >= 0x7F && ch <= 0x9F)) {
                            sb.append(String.format("&#x%X;", ch));
                        } else {
                            sb.appendCodePoint(ch);
                        }
                        break;
                }
            }

            stream.printf("%s='%s'", qname, sb);
        }

        state = IN_TAG;
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if (state == IN_TAG) {
            stream.print("/>");
        }

        if (options.getPrettyPrint()) {
            if (indent.length() >= iunit.length()) {
                indent = indent.substring(iunit.length());
            }

            switch (state) {
                case IN_TAG:
                case CHARS:
                    break;
                case START_TAG:
                case END_TAG:
                    stream.printf("%n");
                    stream.print(indent);
           }
        }

        if (state != IN_TAG) {
            stream.printf("</%s>", localName);
        }

        state = END_TAG;
    }

    @Override
    public void characters (char[] ch, int start, int length)
            throws SAXException
    {
        if (state == IN_TAG) {
            stream.print(">");
        }

        state = CHARS;

        for (int pos = start; pos < start+length; pos++) {
            switch (ch[pos]) {
                case '<':
                    stream.print("&lt;");
                    break;
                case '>':
                    stream.print("&gt;");
                    break;
                case '&':
                    stream.print("&amp;");
                    break;
                case CR:
                case NEL:
                case LINE_SEPARATOR:
                    stream.printf("&#x%X;", (int) ch[pos]);
                    break;
                case TAB:
                case LF:
                    stream.print(ch[pos]);
                    break;
                default:
                    if (ch[pos] <= 0x1F || (ch[pos] >= 0x7F && ch[pos] <= 0x9F)) {
                        stream.printf("&#x%X;", (int) ch[pos]);
                    } else {
                        stream.print(ch[pos]);
                    }
                    break;
            }
        }
    }
}
