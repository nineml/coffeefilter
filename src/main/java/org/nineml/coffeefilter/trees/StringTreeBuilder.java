package org.nineml.coffeefilter.trees;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringTreeBuilder extends DefaultHandler {
    private static final int FIRST = 0;
    private static final int START_TAG = 1;
    private static final int IN_TAG = 2;
    private static final int END_TAG = 3;
    private static final int CHARS = 4;

    private String indent = "";
    private int state = FIRST;
    private ByteArrayOutputStream baos = null;

    private final String iunit = "   ";
    private final PrintStream stream;
    private final boolean prettyPrint;

    public StringTreeBuilder() {
        this(false);
    }

    public StringTreeBuilder(boolean prettyPrint) {
        baos = new ByteArrayOutputStream();
        stream = new PrintStream(baos);
        this.prettyPrint = prettyPrint;
    }

    public StringTreeBuilder(PrintStream stream, boolean prettyPrint) {
        this.stream = stream;
        this.prettyPrint = prettyPrint;
    }

    public String getXml() {
        if (baos == null) {
            return null;
        }
        String xml = baos.toString();
        baos = null;
        return xml;
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

        if (prettyPrint) {
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
        for (int pos = 0; pos < attributes.getLength(); pos++) {
            stream.print(" ");

            String qname = attributes.getQName(pos);
            if (qname.contains(":")) {
                stream.printf("xmlns:%s=\"%s\" ", qname.substring(0, qname.indexOf(":")), attributes.getURI(pos));
            }

            String value = attributes.getValue(pos);
            value = value.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
            value = value.replaceAll("\"", "&quot;");
            stream.printf("%s=\"%s\"", qname, value);
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

        if (prettyPrint) {
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
                default:
                    stream.print(ch[pos]);
                    break;
            }
        }
    }

}
