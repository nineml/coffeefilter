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

    private String indent = "";
    private int state = FIRST;
    private ByteArrayOutputStream baos = null;
    private boolean documentElement = true;
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
        documentElement = true;
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
                nsuri = nsuri.replaceAll("\"", "&quot;");
                if ("".equals(prefix)) {
                    stream.printf(" xmlns=\"%s\"", nsuri);
                } else {
                    stream.printf(" xmlns:%s=\"%s\"", prefix, nsuri);
                }
            }
            prefixMapping = null;
        }

        for (int pos = 0; pos < attributes.getLength(); pos++) {
            stream.print(" ");

            String qname = attributes.getQName(pos);
            String value = attributes.getValue(pos);
            value = value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace("\"", "&quot;");

            if (value.contains("\r")) {
                StringBuffer sb = new StringBuffer();
                Pattern crplus = Pattern.compile("\r.?", Pattern.DOTALL);
                Matcher match = crplus.matcher(value);
                while (match.find()) {
                    String group = match.group();
                    if ("\r\n".equals(group)) {
                        match.appendReplacement(sb, group);
                    } else {
                        if (group.length() > 1) {
                            match.appendReplacement(sb, "&#d;" + group.substring(1));
                        } else {
                            match.appendReplacement(sb, "&#d;");
                        }
                    }
                }
                match.appendTail(sb);
                value = sb.toString();
            }

            stream.printf("%s=\"%s\"", qname, value);
        }

        documentElement = false;
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
                case '&':
                    stream.print("&amp;");
                    break;
                case '\r':
                    if (pos+1 == start+length || (pos+1 < start+length && ch[pos+1] != '\n')) {
                        stream.print("&#xd;");
                    } else {
                        stream.print(ch[pos]);
                    }
                    break;
                default:
                    stream.print(ch[pos]);
                    break;
            }
        }
    }
}
