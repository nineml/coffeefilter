package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.ParserOptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Construct a {@link SimpleTree}.
 * <p>This builder can be passed to {@link InvisibleXmlDocument#getTree()} to build a {@link SimpleTree}.</p>
 */
public class SimpleTreeBuilder extends AbstractTreeBuilder {
    private final boolean ignoreWhitespaceNodes;
    private SimpleTree tree;
    private StringBuilder text = null;
    private boolean started = false;
    private boolean finished = false;

    /**
     * Create a default simple tree builder.
     * <p>The default builder does not ignore whitespace nodes.</p>
     * @param options The parser options.
     */
    public SimpleTreeBuilder(ParserOptions options) {
        this(options, false);
    }

    /**
     * Create a simple tree builder.
     * <p>If <code>ignoreWhitespaceNodes</code> is true, any text node that consists entirely
     * of whitespace will be elided when the tree is constructed.</p>
     * @param ignoreWhitespaceNodes if true, text nodes containing only whitespace are ignored.
     * @param options The parser options.
     */
    public SimpleTreeBuilder(ParserOptions options, boolean ignoreWhitespaceNodes) {
        super(options);
        this.ignoreWhitespaceNodes = ignoreWhitespaceNodes;
        tree = new SimpleTree();
    }

    /**
     * Get the tree once constructed.
     * <p>This method will return the tree, if it has been contructed.</p>
     * @return the tree, or null if no tree has yet been (completely) constructed.
     */
    public SimpleTree getTree() {
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
        tree = new SimpleTree();
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
     * Indicates the construction has finished.
     */
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

    /**
     * Add an element to the SimpleTree.
     * @param uri the element namespace URI, ignored.
     * @param localName the element local name, ignored.
     * @param qName The element QName, used as the name of the node.
     * @param attributes The element attributes.
     * @throws SAXException If a SAX error occurs (it doesn't).
     */
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
            String qname = attributes.getQName(pos);
            tree.addAttribute(qname, attributes.getValue(pos));
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
            if (!ignoreWhitespaceNodes || !"".equals(value.trim())) {
                tree.addText(value);
            }
            text = null;
        }

        tree = tree.getParent();
    }

    /**
     * Add characters to the value of a {#link SimpleText} node.
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
