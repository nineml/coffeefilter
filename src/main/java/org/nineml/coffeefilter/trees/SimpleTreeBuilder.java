package org.nineml.coffeefilter.trees;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SimpleTreeBuilder extends DefaultHandler {
    private final boolean ignoreWhitespaceNodes;
    private SimpleTree tree;
    private StringBuilder text = null;
    private boolean started = false;
    private boolean finished = false;

    public SimpleTreeBuilder() {
        this(false);
    }

    public SimpleTreeBuilder(boolean ignoreWhitespaceNodes) {
        this.ignoreWhitespaceNodes = ignoreWhitespaceNodes;
        tree = new SimpleTree();
    }

    public SimpleTree getTree() {
        if (started && finished) {
            return tree;
        }
        return null;
    }

    public void reset() {
        tree = new SimpleTree();
        started = false;
        finished = false;
    }

    @Override
    public void startDocument() {
        started = true;
    }

    @Override
    public void endDocument() {
        if (text != null) {
            String value = text.toString();
            if (!ignoreWhitespaceNodes || !"".equals(value.trim())) {
                tree.addText(value);
            }
            text = null;
        }

        finished = true;
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        if (text != null) {
            String value = text.toString();
            if (!ignoreWhitespaceNodes || !"".equals(value.trim())) {
                tree.addText(value);
            }
            text = null;
        }

        tree = tree.addChild(localName);
        for (int pos = 0; pos < attributes.getLength(); pos++) {
            tree.addAttribute(attributes.getQName(pos), attributes.getValue(pos));
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if (text != null) {
            String value = text.toString();
            if (!ignoreWhitespaceNodes || !"".equals(value.trim())) {
                tree.addText(value);
            }
            text = null;
        }

        tree = tree.getParent();
    }

    @Override
    public void characters (char[] ch, int start, int length)
            throws SAXException
    {
        if (text == null) {
            text = new StringBuilder();
        }
        for (int pos = start; pos < start+length; pos++) {
            text.appendCodePoint(ch[pos]);
        }
    }
}
