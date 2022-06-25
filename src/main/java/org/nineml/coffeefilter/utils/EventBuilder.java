package org.nineml.coffeefilter.utils;

import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;

/**
 * This class transforms the output of a successful ixml parse into XML.
 */
public class EventBuilder extends TreeBuilder {
    public static final String logcategory = "TreeBuilder";
    private ParserOptions options;
    private final Stack<Child> output;
    private String xmlns = "";
    private double defaultPriority = 0;
    private String grammarVersion = null;
    private ContentHandler handler = null;
    private boolean isRoot = false;
    private int depth;

    public EventBuilder(ParserOptions options) {
        this("1.0", options);
    }

    public EventBuilder(String ixmlVersion, ParserOptions options) {
        if (ixmlVersion == null) {
            throw new NullPointerException("Invisible XML grammar version must not be null.");
        }

        this.options = options;
        // xmlns?
        grammarVersion = ixmlVersion;
        output = new Stack<>();
        depth = 0;
    }

    public ParserOptions getOptions() {
        return options;
    }

    public void setOptions(ParserOptions options) {
        this.options = options;
    }

    public void setHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public void setAmbiguous(boolean ambiguous, boolean infinitelyAmbiguous) {
        this.ambiguous = ambiguous;
        this.infinitelyAmbiguous = infinitelyAmbiguous;
    }

    public ContentHandler getHandler() {
        return handler;
    }

    @Override
    public int chooseFromRemaining(List<RuleChoice> alternatives) {
        ambiguous = true;

        int choice = 0;
        double priority = 0;
        for (int idx = 0; idx < alternatives.size(); idx++) {
            double test = 0;
            // The alternative is null if it matches epsilon
            if (alternatives.get(idx) != null) {
                Symbol nt = alternatives.get(idx).getSymbol();
                Symbol[] rhs = alternatives.get(idx).getRightHandSide();

                if (nt != null && nt.hasAttribute("priority")) {
                    test = Double.parseDouble(alternatives.get(idx).getSymbol().getAttribute("priority").getValue());
                } else {
                    // The rhs is null if this is a non-terminal symbol (as opposed to an intermediate state)
                    if (rhs == null) {
                        test = defaultPriority;
                    } else {
                        for (Symbol symbol : alternatives.get(idx).getRightHandSide()) {
                            if (symbol.hasAttribute("priority")) {
                                test += Double.parseDouble(symbol.getAttribute("priority").getValue());
                            } else {
                                test += defaultPriority;
                            }
                        }
                    }
                }
            }

            if (test > priority) {
                choice = idx;
                priority = test;
            }
        }
        return choice;
    }

    @Override
    public void startTree() {
        super.startTree();
        output.clear();
        depth = 0;
        isRoot = true;
        if (handler == null) {
            options.getLogger().error(logcategory, "No handler provided to tree builder");
        }
    }

    @Override
    public void endTree() {
        super.endTree();

        if (handler == null) {
            return;
        }

        if (output.size() >= 1 && output.get(0) instanceof Attribute) {
            throw IxmlException.attributeRoot(((Attribute) output.get(0)).name);
        }

        if (output.size() >= 1 && output.get(0) instanceof Text) {
            throw IxmlException.notSingleRooted("???");
        }

        if (output.size() > 1) {
            throw IxmlException.notSingleRooted(((Element) output.get(0)).name);
        }

        if (output.isEmpty()) {
            throw IxmlException.notSingleRooted("???");
        }

        try {
            handler.startDocument();
            isRoot = true;
            output.peek().serialize(handler);
            handler.endDocument();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (handler == null) {
            return;
        }

        depth++;

        if (isRoot) {
            isRoot = false;
            String ns = attributes.getOrDefault("ns", null);
            if (ns != null) {
                xmlns = ns;
            }
            String defprio = attributes.getOrDefault("default-priority", null);
            if (defprio != null) {
                defaultPriority = Double.parseDouble(defprio);
            }
        }

        String name = attributes.getOrDefault("name", symbol.toString());
        String mark = attributes.getOrDefault("mark", "^");
        String origName = null;
        String origMark = null;

        if (options.getShowMarks() || options.getShowBnfNonterminals()) {
            String pruneStr = attributes.getOrDefault(ParserAttribute.PRUNING_NAME, ParserAttribute.NOT_ALLOWED_TO_PRUNE);
            boolean prune = ParserAttribute.ALLOWED_TO_PRUNE.equals(pruneStr);
            boolean showHidden = options.getShowBnfNonterminals() && prune;
            boolean showMarks = options.getShowMarks() && (!prune || showHidden);

            if (showMarks) {
                origMark = mark;
                mark = "^";
            }

            if (showHidden) {
                origName = name;
                name = "n:symbol";
            }
        }

        if ("-".equals(mark)) {
            return;
        }

        Element element = new Element(mark, name, depth);
        element.discardEmpty = "empty".equals(attributes.getOrDefault("discard", "none"));
        output.push(element);

        // The else/if is on purpose here, we need mark to be ^ to show hidden nonterminals,
        // but there's no point in outputting the mark as it's always '-'.
        if (origName != null) {
            output.push(new Attribute(InvisibleXml.nineml_prefix + ":name", origName));
        } else if (origMark != null) {
            output.push(new Attribute(InvisibleXml.ixml_prefix + ":mark", origMark));
        }
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (handler == null) {
            return;
        }

        String name = attributes.getOrDefault("name", symbol.toString());
        String mark = attributes.getOrDefault("mark", "^");

        if (options.getShowMarks() || options.getShowBnfNonterminals()) {
            String pruneStr = attributes.getOrDefault(ParserAttribute.PRUNING_NAME, ParserAttribute.NOT_ALLOWED_TO_PRUNE);
            boolean prune = ParserAttribute.ALLOWED_TO_PRUNE.equals(pruneStr);
            boolean showHidden = options.getShowBnfNonterminals() && prune;
            boolean showMarks = options.getShowMarks() && (!prune || showHidden);

            if (showMarks) {
                mark = "^";
            }

            if (showHidden) {
                name = "n:symbol";
            }
        }

        if ("-".equals(mark)) {
            depth--;
            return;
        }

        // Move my element and my children onto a new stack so that popping them will be in the right order
        Stack<Child> local = new Stack<>();
        Child child = output.pop();
        while (!(child instanceof Element) || !name.equals(((Element) child).name) || ((Element) child).depth != depth) {
            local.push(child);
            child = output.pop();
        }
        Element root = (Element) child;

        switch (root.mark) {
            case "@":
                // Collect up all the values.
                StringBuilder sb = new StringBuilder();
                while (!local.isEmpty()) {
                    sb.append(local.pop().stringValue());
                }
                String value = sb.toString();
                if (!"".equals(value) || !"empty".equals(attributes.getOrDefault("discard", "none"))) {
                    child = new Attribute(root.name, sb.toString());
                    output.push(child);
                }
                break;
            case "^":
                while (!local.isEmpty()) {
                    child = local.pop();
                    if (child instanceof Attribute) {
                        if (root.attributes == null) {
                            root.attributes = new ArrayList<>();
                        }
                        root.attributes.add((Attribute) child);
                    } else {
                        if (root.children == null) {
                            root.children = new ArrayList<>();
                        }
                        root.children.add(child);
                    }
                }
                output.push(root);
                break;
            case "+":
                final Text text;
                if (output.isEmpty() || !(output.peek() instanceof Text)) {
                    text = new Text();
                    output.push(text);
                } else {
                    text = (Text) output.peek();
                }
                text.value.append(attributes.getOrDefault("insertion", ""));
                break;
            default:
                throw new IllegalArgumentException("Unexpected mark: " + root.mark);
        }

        depth--;
    }

    @Override
    public void token(Token token, Map<String,String> attributes) {
        if (handler == null) {
            return;
        }

        final String tmark = attributes.getOrDefault("tmark", "^");
        if ("-".equals(tmark)) {
            return;
        }

        if ("true".equals(attributes.getOrDefault("acc", "false"))) {
            return;
        }

        final Text text;
        if (output.isEmpty() || !(output.peek() instanceof Text)) {
            text = new Text();
            output.push(text);
        } else {
            text = (Text) output.peek();
        }

        String rewrite = attributes.getOrDefault("rewrite", null);
        if (rewrite != null) {
            text.value.append(rewrite);
        } else {
            text.value.appendCodePoint(((TokenCharacter) token).getCodepoint());
        }
    }

    private abstract static class Child {
        public abstract String stringValue();
        public abstract void serialize(ContentHandler handler);
    }
    private class Element extends Child {
        public final String mark;
        public final String name;
        public final int depth;
        public ArrayList<Attribute> attributes;
        public ArrayList<Child> children;
        public boolean discardEmpty = false;
        public Element(String mark, String name, int depth) {
            this.mark = mark;
            this.name = name;
            this.depth = depth;
            attributes = null;
            children = null;
        }

        @Override
        public void serialize(ContentHandler handler) {
            try {
                AttributeBuilder attrs = new AttributeBuilder(options);

                if (isRoot) {
                    isRoot = false;

                    if (!"".equals(xmlns)) {
                        handler.startPrefixMapping("", xmlns);
                    }

                    if (options.getShowMarks()) {
                        handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);
                    }

                    if (options.getShowBnfNonterminals()) {
                        handler.startPrefixMapping(InvisibleXml.nineml_prefix, InvisibleXml.nineml_ns);
                    }

                    boolean badVersion = !"1.0".equals(grammarVersion)
                            && !"1.0-9ml".equals(grammarVersion)
                            && !"1.0-nineml".equals(grammarVersion);

                    ambiguous = ambiguous && !options.isSuppressedState("ambiguous");
                    badVersion = badVersion && !options.isSuppressedState("version-mismatch");

                    if (ambiguous || badVersion) {
                        handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);
                    }

                    String state = ambiguous ? "ambiguous" : "";
                    if (badVersion) {
                        state += ("".equals(state) ? "" : " ") + "version-mismatch";
                    }

                    if (!"".equals(state)) {
                        attrs.addAttribute(InvisibleXml.ixml_ns, InvisibleXml.ixml_prefix + ":state", state);
                    }
                }

                if (attributes != null) {
                    HashSet<String> names = new HashSet<>();
                    for (Attribute attr : attributes) {
                        if (names.contains(attr.name)) {
                            throw IxmlException.repeatedAttribute(name);
                        }
                        if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                            throw IxmlException.attributeNameForbidden(name);
                        }
                        if (options.getAssertValidXmlNames()) {
                            TokenUtils.assertXmlName(attr.name);
                        }
                        if (options.getAssertValidXmlCharacters()) {
                            TokenUtils.assertXmlChars(attr.value);
                        }
                        attrs.addAttribute(attr.name, attr.value);
                        names.add(attr.name);
                    }
                }

                if (discardEmpty && attrs.getLength() == 0 && children == null) {
                    return;
                }

                if (options.getAssertValidXmlNames()) {
                    TokenUtils.assertXmlName(name);
                }
                handler.startElement("", name, name, attrs);
                if (children != null) {
                    for (Child child : children) {
                        child.serialize(handler);
                    }
                }
                handler.endElement("", name, name);
            } catch (SAXException ex) {
                throw new RuntimeException(ex);
            }

        }

        @Override
        public String stringValue() {
            if (children == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (Child child: children) {
                sb.append(child.stringValue());
            }
            return sb.toString();
        }
    }
    private static class Attribute extends Child {
        public final String namespace;
        public final String name;
        public final String value;
        public Attribute(String namespace, String name, String value) {
            this.namespace = namespace;
            this.name = name;
            this.value = value;
        }

        public Attribute(String name, String value) {
            this.namespace = null;
            this.name = name;
            this.value = value;
        }

        @Override
        public void serialize(ContentHandler handler) {
            throw new RuntimeException("Attempt to serialize an attribute");
        }

        @Override
        public String stringValue() {
            return value;
        }
    }
    private class Text extends Child {
        public final StringBuilder value;
        public Text() {
            value = new StringBuilder();
        }

        @Override
        public void serialize(ContentHandler handler) {
            try {
                if (options.getAssertValidXmlCharacters()) {
                    TokenUtils.assertXmlChars(value.toString());
                }
                char[] data = value.toString().toCharArray();
                handler.characters(data, 0, data.length);
            } catch (SAXException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String stringValue() {
            return value.toString();
        }
    }
}
