package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.ParserOptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IxmlContentHandler extends DefaultHandler {
    private boolean finished = false;
    private boolean simplified = false;
    private Ixml ixml = null;
    private XNode current = null;

    /**
     * Get the Ixml for this handler.
     * <p>The underlying grammar is cached. Calling this method with different parser
     * options will have no effect.</p>
     * @param options the parser options
     * @return the Ixml grammar
     */
    public Ixml getIxml(ParserOptions options) {
        if (!finished) {
            return null;
        }

        if (ixml == null) {
            throw new NullPointerException("No ixml grammar has been loaded");
        }

        if (!simplified) {
            ixml.simplifyGrammar(options);
            simplified = true;
        }

        return ixml;
    }

    @Override
    public void startDocument () throws SAXException {
        finished = false;
        simplified = false;
    }

    @Override
    public void endDocument () throws SAXException {
        finished = true;
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        /*
        System.err.printf("%s : %d\n", localName, attributes.getLength());
        for (int pos = 0; pos < attributes.getLength(); pos++) {
            System.err.printf("  @%s: %s\n", attributes.getLocalName(pos), attributes.getValue(pos));
        }
         */
        if (current == null) {
            ixml = new Ixml();
            current = ixml;
        } else {
            current = current.createChild(localName, attributes);
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if (current == null) {
            System.err.println("Current is null for " + localName + "?");
        } else {
            current = current.getParent();
        }
    }
}
