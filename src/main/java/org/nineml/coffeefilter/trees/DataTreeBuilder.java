package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.exceptions.IxmlTreeException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Construct a {@link DataTree}.
 * <p>This builder can be passed to {@link InvisibleXmlDocument#getTree()} to build {@link DataTree}.</p>
 */
public class DataTreeBuilder extends AbstractTreeBuilder {
    private final boolean allowDuplicateNames;
    private DataTree tree;
    private StringBuilder text = null;
    private boolean started = false;
    private boolean finished = false;

    /**
     * Create a default data tree builder.
     * <p>The default builder allows duplicate names.</p>
     * @param options The parser options.
     */
    public DataTreeBuilder(ParserOptions options) {
        this(options, true);
    }

    /**
     * Create a data tree builder.
     * <p>If duplicate names are not allowed, the builder will raise an exception if
     * any node has more than one child with a given name.</p>
     * @param options The parser options.
     * @param allowDuplicateNames if false, children with duplicated names are forbidden.
     */
    public DataTreeBuilder(ParserOptions options, boolean allowDuplicateNames) {
        super(options);
        this.allowDuplicateNames = allowDuplicateNames;
        tree = new DataTree();
    }

    /**
     * Get the tree once constructed.
     * <p>This method will return the tree, if it has been contructed.</p>
     * @return the tree, or null if no tree has yet been (completely) constructed.
     */
    public DataTree getTree() {
        if (started && finished) {
            return tree;
        }
        return null;
    }

    /**
     * Reset the builder.
     * <p>The builder isn't thread safe, but you can reuse it. Call reset between uses.</p>
     */
    public void reset() {
        tree = new DataTree();
        started = false;
        finished = false;
    }

    /**
     * Indicates the construction has begun.
     */
    @Override
    public void startDocument() {
        started = true;
    }

    /**
     * Inidcates that construction has finished.
     */
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

    /**
     * Add an element to the DataTree.
     * <p>This method will throw exceptions if the tree structure can't be supported by {@link DataTree},
     * for example if the tree contains mixed content. Attributes are represented as child nodes.</p>
     * @param uri the element namespace URI, ignored.
     * @param localName the element local name, ignored.
     * @param qName The element QName, used as the name of the node.
     * @param attributes The element attributes.
     * @throws SAXException If a SAX error occurs.
     * @throws IxmlTreeException if the tree cannot be represented.
     */
    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        if (text != null) {
            String value = text.toString();
            if (!"".equals(value.trim())) {
                throw IxmlTreeException.noMixedContent();
            }
            text = null;
        }

        if (!allowDuplicateNames && tree.get(localName) != null) {
            throw IxmlTreeException.duplicatesForbidden(localName);
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

    /**
     * Ends an element.
     * @param uri the element namespace URI, ignored.
     * @param localName the element local name, ignored.
     * @param qName The element QName, used as the name of the node.
     * @throws SAXException If a SAX error occurs.
     */
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

    /**
     * Add characters to the value of a {#link DataText} node.
     * @param ch The array of characters.
     * @param start The first.
     * @param length The length.
     * @throws SAXException If a SAX error occurs, which it never does.
     */
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
