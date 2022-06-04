package org.nineml.coffeefilter.utils;

import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.model.InsertionNonterminal;
import org.nineml.coffeegrinder.parser.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * This class transforms the output of a successful ixml parse into XML.
 */
public class CommonBuilder {
    public static final String logcategory = "TreeBuilder";
    public static final String ixml_prefix = "ixml";
    public static final String ixml_ns = "http://invisiblexml.org/NS";
    private final ParserOptions options;
    private final Stack<PartialOutput> stack = new Stack<>();
    private final Stack<Character> context = new Stack<>();
    private final Stack<String> elementStack = new Stack<>();
    private boolean rootFinished = false;
    private PartialOutput result = null;
    private boolean documentElement = false;
    private boolean ambiguous = false;
    private boolean prefix = false;
    private String xmlns = "";
    private String grammarVersion = null;

    public CommonBuilder(ParseTree tree, GearleyResult result, ParserOptions options) {
        this(tree, null, result, options);
    }

    public CommonBuilder(ParseTree tree, String ixmlVersion, GearleyResult result, ParserOptions options) {
        this.options = options;
        if (tree == null) {
            options.getLogger().trace(logcategory, "No tree");
            return;
        }
        context.push('*');

        if (tree.getSymbol().hasAttribute("ns")) {
            xmlns = tree.getSymbol().getAttribute("ns").getValue();
        }

        constructTree(tree, null);
        ambiguous = tree.getForest().isAmbiguous();
        prefix = result.prefixSucceeded();
        grammarVersion = ixmlVersion;
    }

    private void startNonterminal(ParseTree tree, char mark, String name, Map<String,String> parseAttributes) {
        //System.err.println("ST: " + tree);
        char ctx = mark;
        if (context.peek() == '@') {
            ctx = '@';
        } else {
            if (ctx == '^') {
                if (rootFinished) {
                    throw IxmlException.notSingleRooted(name);
                }
                elementStack.push(name);
            }
            PartialOutput top = new PartialOutput(mark, name, parseAttributes);
            stack.push(top);
        }
        context.push(ctx);
    }

    private void endNonterminal(ParseTree tree, String name) {
        //System.err.println("EN: " + tree);
        context.pop();
        if (context.peek() == '@') {
            return;
        }

        PartialOutput top = stack.pop();
        if (!name.equals(top.name)) {
            throw new RuntimeException("Missmatch on output stack");
        }
        if (stack.isEmpty()) {
            result = top;
        } else {
            if (top.mark == '^') {
                elementStack.pop();
                rootFinished = rootFinished || elementStack.isEmpty();
            }

            if (top.mark == '@' && stack.peek().mark != '^') {
                // Special case. We have to put this on an item that will generate an element
                int pos = stack.size() - 1;
                while (stack.get(pos).mark != '^') {
                    pos--;
                    if (pos < 0) {
                        // If this is the root element, that's not allowed
                        throw IxmlException.attributeRoot(name);
                    }
                }
                stack.get(pos).add(top);
            } else {
                stack.peek().add(top);
            }
        }
    }

    private void terminal(ParseTree tree, char mark, int codepoint, boolean acc, String rewrite) {
        //System.err.println("TT2: " + tree);
        if (mark == '-') {
            return;
        }

        if (rewrite != null) {
            if (!stack.isEmpty()) {
                stack.peek().rewrite(rewrite);
            }
        } else {
            PartialOutput item = new PartialOutput(codepoint, acc);
            if (stack.isEmpty()) {
                stack.push(item);
            } else {
                stack.peek().add(item);
            }
        }
    }

    public void build(ContentHandler handler) {
        try {
            handler.startDocument();
            documentElement = true;
            result.output(handler);

            if (documentElement) {
                throw IxmlException.notSingleRooted("");
            }

            handler.endDocument();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void constructTree(ParseTree tree, Symbol xsymbol) {
        ParseTree child0 = null;
        Symbol child0Symbol = null;
        ParseTree child1 = null;
        Symbol child1Symbol = null;
        String localName = null;

        assert tree != null;
        State state = tree.getState();

        if (!tree.getChildren().isEmpty()) {
            child0 = tree.getChildren().get(0);
            if (tree.getChildren().size() > 1) {
                child1 = tree.getChildren().get(1);
            }
            assert tree.getChildren().size() <= 2;
        }

        int pos;
        Symbol symbol = tree.getSymbol();
        if (symbol == null) {
            assert child0 != null;
            if (child1 != null) {
                pos = getSymbol(child1.getSymbol(), state, state.getPosition());
                if (pos >= 0) {
                    child1Symbol = state.getRhs().get(pos);
                } else {
                    pos = state.getPosition();
                }

                pos = getSymbol(child0.getSymbol(), state, pos); // don't "pass" the second symbol
                if (pos >= 0) {
                    child0Symbol = state.getRhs().get(pos);
                }

                constructTree(child0, child0Symbol);
                constructTree(child1, child1Symbol);
            } else {
                pos = getSymbol(child0.getSymbol(), state, state.getPosition());
                if (pos >= 0) {
                    child0Symbol = state.getRhs().get(pos);
                }

                constructTree(child0, child0Symbol);
            }
            return;
        }

        char mark = getMark(xsymbol == null ? symbol : xsymbol);
        if (symbol instanceof TerminalSymbol) {
            Token token = ((TerminalSymbol) tree.getSymbol()).getToken();
            if (!(token instanceof TokenCharacter)) {
                throw new RuntimeException("Unexpected token in tree (not a TokenCharacter): " + token);
            }

            String acc = getAttribute(symbol, xsymbol, "acc");
            String rewrite = getAttribute(symbol, xsymbol, "rewrite");

            int ch = ((TokenCharacter) token).getCodepoint();
            terminal(tree, mark, ch, acc != null, rewrite);
        } else {
            if (child1 != null) {
                pos = getSymbol(child1.getSymbol(), state, state.getPosition());
                if (pos >= 0) {
                    child1Symbol = state.getRhs().get(pos);
                } else {
                    pos = state.getPosition();
                }

                pos = getSymbol(child0.getSymbol(), state, pos); // don't "pass" the second symbol
                if (pos >= 0) {
                    child0Symbol = state.getRhs().get(pos);
                }
            } else {
                if (child0 != null) {
                    pos = getSymbol(child0.getSymbol(), state, state.getPosition()); // don't "pass" the second symbol
                    if (pos >= 0) {
                        child0Symbol = state.getRhs().get(pos);
                    }
                }
            }

            if (symbol instanceof InsertionNonterminal) {
                String gentext = symbol.getAttributeValue("insertion", null);
                if (gentext != null) {
                    stack.peek().add(new PartialOutput(gentext));
                }
            } else {
                localName = getAttribute(symbol, xsymbol, "name");
                startNonterminal(tree, mark, localName, getAttributes(symbol, xsymbol));

                if (child0 != null) {
                    constructTree(child0, child0Symbol);
                }

                if (child1 != null) {
                    constructTree(child1, child1Symbol);
                }

                endNonterminal(tree, localName);
            }
        }
    }

    private String getAttribute(Symbol symbol, Symbol xsymbol, String name) {
        if (xsymbol != null && xsymbol.hasAttribute(name)) {
            return xsymbol.getAttribute(name).getValue();
        }
        if (symbol != null && symbol.hasAttribute(name)) {
            return symbol.getAttribute(name).getValue();
        }
        return null;
    };

    private Map<String,String> getAttributes(Symbol symbol, Symbol xsymbol) {
        HashMap<String, String> map = new HashMap<>();
        if (xsymbol != null) {
            for (ParserAttribute attr : xsymbol.getAttributes()) {
                map.put(attr.getName(), attr.getValue());
            }
        }
        if (symbol != null) {
            for (ParserAttribute attr : symbol.getAttributes()) {
                if (!map.containsKey(attr.getName())) {
                    map.put(attr.getName(), attr.getValue());
                }
            }
        }
        return map;
    };

    private int getSymbol(Symbol seek, State state, int maxPos) {
        // Because some nonterminals can go to epsilon, we can't always find them
        // by position. If there's only one symbol, then we want the last one before
        // the position. But if there are two, then the *second* symbol has to come
        // after the first!
        int found = -1;
        if (seek instanceof TerminalSymbol) {
            Token token = ((TerminalSymbol) seek).getToken();
            for (int pos = 0; pos < maxPos; pos++) {
                if (state.getRhs().get(pos).matches(token)) {
                    found = pos;
                }
            }
        } else {
            for (int pos = 0; pos < maxPos; pos++) {
                if (state.getRhs().get(pos).equals(seek)) {
                    found = pos;
                }
            }
        }

        return found;
    }

    private char getMark(Symbol symbol) {
        ParserAttribute attr = symbol.getAttribute(symbol instanceof TerminalSymbol ? "tmark" : "mark");
        if (attr == null) {
            return '?';
        }
        return attr.getValue().charAt(0);
    }

    private class PartialOutput {
        private final char mark;
        private final String name;
        private String text;
        private boolean accumulator = false;
        private final ArrayList<PartialOutput> attributes = new ArrayList<>();
        private final ArrayList<PartialOutput> children = new ArrayList<>();
        private final HashMap<String,String> parseAttributes = new HashMap<>();
        private final boolean discardEmpty;

        public PartialOutput(char mark, String name, Map<String,String> parseAttributes) {
            this.mark = mark;
            this.name = name;
            this.parseAttributes.putAll(parseAttributes);
            discardEmpty = parseAttributes.containsKey("discard") && "empty".equals(parseAttributes.get("discard"));
            text = null;
        }

        public PartialOutput(int codepoint, boolean acc) {
            mark ='?';
            name = null;
            // Surely there's something more efficient than this?
            text = new StringBuilder().appendCodePoint(codepoint).toString();
            accumulator = acc;
            discardEmpty = false;
        }

        public PartialOutput(String text) {
            mark ='?';
            name = null;
            this.text = text;
            discardEmpty = false;
        }

        public void add(PartialOutput item) {
            if (item.mark == '@') {
                // Collapse all the text item children into a single string
                StringBuilder sb = new StringBuilder();
                for (PartialOutput child : item.children) {
                    if (child.name == null) {
                        sb.append(child.text);
                    }
                }
                item.children.clear();
                item.children.add(new PartialOutput(sb.toString()));
                attributes.add(item);
                return;
            }

            if (children.isEmpty()) {
                children.add(item);
            } else {
                if (item.name == null) {
                    PartialOutput last = children.get(children.size() - 1);
                    if (last.name == null && last.accumulator == item.accumulator) {
                        last.text += item.text;
                    } else {
                        children.add(item);
                    }
                } else {
                    children.add(item);
                }
            }
        }

        public void rewrite(String rewrite) {
            if (children.isEmpty()) {
                // we must be rewriting a single token
                children.add(new PartialOutput(rewrite));
            } else {
                PartialOutput last = children.get(children.size() - 1);
                if (last.name == null) {
                    last.text = rewrite;
                }
            }
        }

        private String assertValidName(String name) {
            // Let's take the pedantic position that the valid name characters are the ones
            // in the Fifth Edition. That's not what all parsers actually implement, but
            // [expletive deleted].
            if (name == null) {
                throw new NullPointerException("XML names cannot be null");
            }
            if ("".equals(name) || name.charAt(0) == ':') {
                throw IxmlException.invalidXmlName(name);
            }
            boolean first = true;
            boolean colon = false;
            for (int ch : name.codePoints().toArray()) {
                if (ch == ':') {
                    if (colon) {
                        throw IxmlException.invalidXmlName(name);
                    }
                    colon = true;
                } else if ((ch >= 'A' && ch <= 'Z') || ch == '_' || (ch >= 'a' && ch <= 'z')
                        || (ch >= 0xC0&& ch <= 0xD6) || (ch >= 0xD8&& ch <= 0xF6) || (ch >= 0xF8&& ch <= 0x2FF)
                        || (ch >= 0x370&& ch <= 0x37D) || (ch >= 0x37F&& ch <= 0x1FFF)
                        || (ch >= 0x200C&& ch <= 0x200D) || (ch >= 0x2070&& ch <= 0x218F)
                        || (ch >= 0x2C00&& ch <= 0x2FEF) || (ch >= 0x3001&& ch <= 0xD7FF)
                        || (ch >= 0xF900&& ch <= 0xFDCF) || (ch >= 0xFDF0&& ch <= 0xFFFD)
                        || (ch >= 0x10000&& ch <= 0xEFFFF)) {
                    // ok
                } else {
                    if (first) {
                        throw IxmlException.invalidXmlName(name);
                    }
                    if (ch == '-' || ch == '.' || (ch >= '0' && ch <= '9') || ch == 0xB7
                            || (ch >= 0x0300 && ch <= 0x036F) || (ch >= 0x203F && ch <= 0x2040)) {
                        // still ok
                    } else {
                        throw IxmlException.invalidXmlName(name);
                    }
                }
                first = false;
            }

            return name;
        }

        private String assertValidChars(String text) {
            if (text == null) {
                throw new NullPointerException("Text cannot be null");
            }
            for (int ch : text.codePoints().toArray()) {
                if (ch == 0x9 || ch == 0xA || ch == 0xD || (ch >= ' ' && ch <= 0xD7FF)
                    || (ch >= 0xE000 & ch <= 0xFFFD) || (ch >= 0x10000 && ch <= 0x10FFFF)) {
                    // ok
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.appendCodePoint(ch);
                    throw IxmlException.invalidXmlCharacter(sb.toString());
                }
            }

            return text;
        }

        public void output(ContentHandler handler) throws SAXException {
            if (discardEmpty && isEmpty()) {
                return;
            }

            if (mark != '-') {
                if (name == null) {
                    handler.characters(assertValidChars(text).toCharArray(), 0, text.length());
                } else {
                    AttributeBuilder attrs = new AttributeBuilder(options);
                    for (PartialOutput attr : attributes) {
                        // Attributes are constructed with a single child that contains the value
                        String value;
                        if (attr.children.isEmpty()) {
                            value = "";
                        } else {
                            value = attr.children.get(0).text;
                        }

                        if (!attr.discardEmpty || !"".equals(value)) {
                            String name = assertValidName(attr.name);
                            if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
                                throw IxmlException.attributeNameForbidden(name);
                            }
                            attrs.addAttribute(name, assertValidChars(value));
                        }
                    }

                    if (documentElement) {
                        documentElement = false;

                        String state = "";
                        String sep = "";
                        if (ambiguous && !options.isSuppressedState("ambiguous")) {
                            state = "ambiguous";
                            sep = " ";
                        }
                        if (prefix && !options.isSuppressedState("prefix")) {
                            state += sep + "prefix";
                        }
                        if (grammarVersion != null) {
                            if (!"1.0".equals(grammarVersion) && !"1.0-nineml".equals(grammarVersion)) {
                                state += sep + "version-mismatch";
                            }
                        }

                        if (!"".equals(xmlns)) {
                            handler.startPrefixMapping("", xmlns);
                        }

                        if (!"".equals(state)) {
                            handler.startPrefixMapping(ixml_prefix, ixml_ns);
                            attrs.addAttribute(ixml_ns, ixml_prefix + ":state", state);
                        }
                    }

                    handler.startElement(xmlns, name, assertValidName(name), attrs);
                }
            }

            for (PartialOutput child : children) {
                child.output(handler);
            }

            if (mark == '^' && name != null) {
                handler.endElement("", name, name);
            }
        }

        private boolean isEmpty() {
            if (mark == '-' || "empty".equals(parseAttributes.getOrDefault("discard", "false"))) {
                for (PartialOutput child : children) {
                    if (!child.isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            if (name == null) {
                return mark + "\"" + text + "\"";
            } else {
                return mark + name;
            }
        }
    }
}
