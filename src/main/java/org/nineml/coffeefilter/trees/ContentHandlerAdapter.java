package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeefilter.util.TokenUtils;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;
import java.util.stream.Collectors;

public class ContentHandlerAdapter implements TreeBuilder {
    private Node node;
    public final String parserVersion;
    private final ParserOptions options;
    private final ContentHandler handler;
    private Stack<Node> nodeStack = null;

    private boolean ambiguous = false;
    private boolean madeAmbiguousChoice = false;

    private String xmlns = "";
    private boolean badVersion = false;
    private boolean markAmbiguous = false;
    private boolean root = true;
    private boolean firstSymbol = true;

    public ContentHandlerAdapter(String version, ParserOptions options, ContentHandler handler) {
        this.parserVersion = version;
        this.options = new ParserOptions(options);
        this.handler = handler;
    }

    public ParserOptions getOptions() {
        return options;
    }

    public ContentHandler getHandler() {
        return handler;
    }

    @Override
    public void startTree() {
        node = new DocumentNode();
        root = true;
        firstSymbol = true;
    }

    @Override
    public void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous) {
        this.ambiguous = absolutelyAmbiguous;
        this.madeAmbiguousChoice = ambiguous;

        node.flatten();
        assert nodeStack != null;
        assert nodeStack.size() == 1;
        DocumentNode doc = (DocumentNode) nodeStack.get(0);
        Node root = doc.children.isEmpty() ? null : doc.children.get(0);
        if (root != null) {
            if (root instanceof TextNode) {
                throw IxmlException.notSingleRooted("(text node)");
            }
            if (root instanceof AttributeNode) {
                throw IxmlException.attributeRoot(root.getName());
            }
        }

        if (doc.children.size() != 1) {
            throw IxmlException.notSingleRooted("(document)");
        }

        try {
            handler.startDocument();
            doc.serialize();
            handler.endDocument();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
        char mark = attributes.getOrDefault(InvisibleXml.MARK_ATTRIBUTE, "^").charAt(0);
        if (mark == '@' && !options.getShowBnfNonterminals()) {
            node = node.addChild(new AttributeNode(symbol, attributes));
        } else {
            node = node.addChild(new ElementNode(symbol, attributes));
        }

        if (firstSymbol && attributes.containsKey(InvisibleXml.XMLNS_ATTRIBUTE)) {
            xmlns = attributes.get(InvisibleXml.XMLNS_ATTRIBUTE);
        }

        firstSymbol = false;
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
        node = node.parent;
    }

    @Override
    public void token(Token token, Map<String, String> attributes, int leftExtent, int rightExtent) {
        node.addChild(new TextNode(token, attributes));
    }

    private abstract class Node {
        public final Map<String,String> attributes;
        public final ArrayList<Node> children;
        public final char mark;
        public Node parent;
        public Node(char mark, Map<String,String> attributes) {
            this.mark = mark;
            this.attributes = new HashMap<>(attributes);
            this.children = new ArrayList<>();
        }
        public Node addChild(Node node) {
            node.parent = this;
            children.add(node);
            return node;
        }
        public abstract void flatten();
        public abstract void serialize() throws SAXException;
        public String getName() {
            return "";
        }

        public String getStringValue() {
            if (mark == '+') {
                return attributes.get(InvisibleXml.INSERTION_ATTRIBUTE);
            }

            StringBuilder sb = new StringBuilder();
            for (Node child : children) {
                sb.append(child.getStringValue());
            }
            return sb.toString();
        }
        public void startDocument(DocumentNode doc) {
            nodeStack = new Stack<>();
            nodeStack.push(doc);
        }

        public void startNode(SymbolNode node) {
            nodeStack.peek().children.add(node);
            nodeStack.push(node);
        }
        public void text(TextNode node) {
            nodeStack.peek().children.add(node);
        }
        public void endNode() {
            nodeStack.pop();
        }
        public void endDocument() {
        }
    }

    private class DocumentNode extends Node {
        public DocumentNode() {
            super('^', Collections.emptyMap());
        }
        @Override
        public void flatten() {
            assert children.size() == 1;
            startDocument(this);
            Node child = children.get(0);
            children.clear();
            child.flatten();
            endDocument();
        }
        @Override
        public void serialize() throws SAXException {
            if (!"".equals(xmlns)) {
                handler.startPrefixMapping("", xmlns);
            }

            if (options.getShowMarks()) {
                handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);
            }

            if (options.getShowBnfNonterminals()) {
                handler.startPrefixMapping(InvisibleXml.nineml_prefix, InvisibleXml.nineml_ns);
            }

            String grammarVersion = parserVersion;
            badVersion = !"1.0".equals(grammarVersion)
                    && !"1.0-nineml".equals(grammarVersion)
                    && !"1.1-nineml".equals(grammarVersion);

            markAmbiguous = ambiguous && !options.isSuppressedState(InvisibleXml.AMBIGUOUS)
                    && (options.getStrictAmbiguity() || madeAmbiguousChoice);

            badVersion = badVersion && !options.isSuppressedState(InvisibleXml.VERSION_MISMATCH);

            if (markAmbiguous || badVersion) {
                handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);
            }

            for (Node child : children) {
                child.serialize();
            }
        }
        @Override
        public String toString() {
            return "(document)";
        }
    }

    private abstract class SymbolNode extends Node {
        public final NonterminalSymbol symbol;
        public SymbolNode(NonterminalSymbol symbol, Map<String,String> attributes) {
            super(attributes.getOrDefault(InvisibleXml.MARK_ATTRIBUTE, "^").charAt(0), attributes);
            this.symbol = symbol;
        }
        @Override
        public String getName() {
            return attributes.getOrDefault(InvisibleXml.NAME_ATTRIBUTE, symbol.getName());
        }
        @Override
        public void flatten() {
            switch (mark) {
                case '^':
                case '@':
                case '+':
                    startNode(this);
                    break;
                case '-':
                    if (options.getShowBnfNonterminals()) {
                        startNode(this);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected mark: " + mark);
            }

            List<Node> copyChildren = new ArrayList<>(children);
            children.clear();
            for (Node child : copyChildren) {
                child.flatten();
            }

            if (mark != '-' || options.getShowBnfNonterminals()) {
                endNode();
            }
        }
        @Override
        public String toString() {
            return String.format("%s%s (%d)", mark, symbol.getName(), children.size());
        }
    }

    private class ElementNode extends SymbolNode {
        public ElementNode(NonterminalSymbol symbol, Map<String,String> attributes) {
            super(symbol, attributes);
        }
        @Override
        public void serialize() throws SAXException {
            if (mark == '+') {
                String value = attributes.get(InvisibleXml.INSERTION_ATTRIBUTE);
                if (value == null) {
                    throw new IllegalStateException("Insertion has no value");
                }
                if (options.getAssertValidXmlCharacters()) {
                    TokenUtils.assertXmlChars(value);
                }
                char[] chars = value.toCharArray();
                handler.characters(chars, 0, chars.length);
                return;
            }

            AttributeBuilder atts = new AttributeBuilder(options);
            HashSet<String> names = new HashSet<>();
            if (options.getShowMarks()) {
                String aname = InvisibleXml.ixml_prefix + ":mark";
                atts.addAttribute(InvisibleXml.ixml_ns, aname, String.valueOf(mark));
                names.add(aname);
            }

            for (Node att : children.stream().filter(e -> e instanceof AttributeNode).collect(Collectors.toList())) {
                String name = att.getName();
                String value = att.getStringValue();

                if (names.contains(name)) {
                    throw IxmlException.repeatedAttribute(att.getName());
                }
                if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                    throw IxmlException.attributeNameForbidden(name);
                }

                boolean discardEmpty = "empty".equals(att.attributes.getOrDefault(InvisibleXml.DISCARD_ATTRIBUTE, "none"));
                if (!discardEmpty || !"".equals(value)) {
                    if (options.getAssertValidXmlNames()) {
                        TokenUtils.assertXmlName(name);
                    }
                    if (options.getAssertValidXmlCharacters()) {
                        TokenUtils.assertXmlChars(value);
                    }

                    atts.addAttribute(name, value);
                    names.add(name);
                }
            }

            if (root) {
                root = false;
                String state = markAmbiguous ? "ambiguous" : "";
                if (badVersion) {
                    state += ("".equals(state) ? "" : " ") + "version-mismatch";
                }
                if (!"".equals(state)) {
                    atts.addAttribute(InvisibleXml.ixml_ns, InvisibleXml.ixml_prefix + ":state", state);
                }
            }

            boolean discardEmpty = "empty".equals(attributes.getOrDefault(InvisibleXml.DISCARD_ATTRIBUTE, "none"));
            if (discardEmpty && atts.getLength() == 0 && children.isEmpty()) {
                return;
            }

            String ns = "";
            String name = getName();
            if (mark == '-') {
                name = "n:symbol";
                ns = InvisibleXml.nineml_ns;
                atts.addAttribute("name", symbol.getName());
                if (!symbol.symbolName.equals(symbol.getName())) {
                    atts.addAttribute("symbol-name", symbol.symbolName);
                }
            }

            if (options.getAssertValidXmlNames()) {
                TokenUtils.assertXmlName(name);
            }

            handler.startElement(ns, name, name, atts);

            for (Node child : children) {
                if (!(child instanceof AttributeNode)) {
                    child.serialize();
                }
            }

            handler.endElement(ns, name, name);
        }
    }

    private class AttributeNode extends SymbolNode {
        public AttributeNode(NonterminalSymbol symbol, Map<String,String> attributes) {
            super(symbol, attributes);
        }
        @Override
        public void serialize() {
            throw new IllegalStateException("Attributes cannot be serialized directly");
        }
    }

    private class TextNode extends Node {
        public final Token token;
        public TextNode(Token token, Map<String,String> attributes) {
            super(attributes.getOrDefault(InvisibleXml.TMARK_ATTRIBUTE, "^").charAt(0), attributes);
            this.token = token;
        }
        @Override
        public String getStringValue() {
            return token.getValue();
        }
        public void flatten() {
            if (mark != '-') {
                text(this);
            }
        }
        @Override
        public void serialize() throws SAXException {
            if (options.getAssertValidXmlCharacters()) {
                TokenUtils.assertXmlChars(token.getValue());
            }
            char[] chars = token.getValue().toCharArray();
            handler.characters(chars, 0, chars.length);
        }
        @Override
        public String toString() {
            return String.format("%s%s", mark, token.getValue());
        }
    }
}
