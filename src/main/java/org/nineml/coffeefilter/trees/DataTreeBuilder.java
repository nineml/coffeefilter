package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DataTreeBuilder extends DefaultHandler {
    private final boolean allowDuplicateNames;
    private DataTree tree;
    private StringBuilder text = null;
    private boolean started = false;
    private boolean finished = false;

    public DataTreeBuilder() {
        this(true);
    }

    public DataTreeBuilder(boolean allowDuplicateNames) {
        this.allowDuplicateNames = allowDuplicateNames;
        tree = new DataTree();
    }

    public DataTree getTree() {
        if (started && finished) {
            return tree;
        }
        return null;
    }

    public void reset() {
        tree = new DataTree();
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
            if (!"".equals(value.trim())) {
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
            if (!"".equals(value.trim())) {
                throw new IxmlException("Cannot mix subtree and text nodes in a data tree");
            }
            text = null;
        }

        if (!allowDuplicateNames && tree.get(localName) != null) {
            throw new IxmlException("Duplicate names forbidden in data tree");
        }

        tree = tree.addChild(localName);

        for (int pos = 0; pos < attributes.getLength(); pos++) {
            DataTree child = tree.addChild(attributes.getQName(pos));
            String value = attributes.getValue(pos);
            if ("".equals(value.trim())) {
                child.addText("");
            } else {
                child.addText(attributes.getValue(pos));
            }
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if (text != null) {
            String value = text.toString();
            if (!"".equals(value.trim())) {
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
