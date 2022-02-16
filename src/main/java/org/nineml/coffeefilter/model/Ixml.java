package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Model an Invisible XML grammar. This class represents the "top" of an ixml grammar.
 * This is what you get back if you parse a grammar.
 */
public class Ixml extends XNonterminal {
    protected int synCount = 0;
    private boolean changed = true;
    private ArrayList<IRule> newRules = null;
    private final ArrayList<Rule> grammarRules = new ArrayList<>();
    private String startRule = null;
    private Grammar grammar = null;
    protected boolean emptyProduction = false;

    /**
     * Construct an Ixml.
     */
    protected Ixml() {
        super(null, "ixml", "$$_ixml");
    }

    public Ixml(Grammar grammar) {
        super(null, "ixml", "$$_ixml");
        this.grammar = grammar;
        synCount += grammar.getRules().size();
        grammarRules.addAll(grammar.getRules());
        startRule = "$$";
    }

    /**
     * Returns the name of the starting rule.
     * @return the name of the staring rule
     */
    public String startingRule() {
        return startRule;
    }

    /**
     * Returns a new, unique rule name.
     * @return A new, unique name.
     */
    public String nextRuleName() {
        synCount += 1;
        return "$" + synCount;
    }

    /**
     * Return a list of the underlying {@link Rule} objects.
     *
     * @return The rule mapping.
     */
    public List<Rule> getGrammarRules() {
        return grammarRules;
    }

    /**
     * Copy the current node and its descendants.
     *
     * <p>The parent of the copy is often the same as the original, but
     * this is a bit spurious as it'll get changed when the copy is inserted
     * as a child of some other node.</p>
     *
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        Ixml newnode = new Ixml();
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    private List<IRule> ruleChildren() {
        ArrayList<IRule> rules = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IRule) {
                rules.add((IRule) child);
            } else if (child instanceof IS) {
                // ignore
            } else {
                throw new RuntimeException(this + " child: " + child);
            }
        }
        return rules;
    }

    /**
     * Simplify the grammar.
     *
     * <p>Simplification transforms the original ixml grammar into something that can
     * be processed by the backend. This involves replacing repeat0 and repeat1 constructions with
     * simpler constructions, creating duplicate rules to deal with optionality, and a number
     * of other transformations.</p>
     */
    public void simplifyGrammar() {
        flatten();

        ArrayList<XNode> newchildren = new ArrayList<>();
        for (IRule rule : ruleChildren()) {
            if (startRule == null) {
                startRule = rule.getName();
            }
            if (!rule.children.isEmpty() && rule.children.get(0) instanceof IAlt) {
                for (XNode alt : rule.children) {
                    if (!(alt instanceof IAlt)) {
                        throw new RuntimeException("Mixture of alts and other things?");
                    }
                    IRule newRule = new IRule(this, rule.getName(), rule.getMark());
                    for (XNode gchild : alt.getChildren()) {
                        newRule.addCopy(gchild);
                    }
                    newchildren.add(newRule);
                }
            } else {
                newchildren.add(rule);
            }
        }
        children = newchildren;

        replaceAlternatives();
        if (newRules != null) {
            children.addAll(newRules);
            newRules.clear();
        }

        simplify();
        flattenNonterminals();
        constructGrammar();
    }

    protected void simplify() {
        // Make sure we have a top-level rule that will have only one rhs
        // even if the user's seed rule winds up having alternatives
        ArrayList<XNode> newchildren = new ArrayList<>();
        IRule START = new IRule(this, "$$", '-');

        IRule firstRule = null;
        int pos = 0;
        while (!(children.get(pos) instanceof IRule)) {
            pos++;
        }
        firstRule = (IRule) children.get(pos);

        START.children.add(new INonterminal(START, firstRule.getName(), firstRule.getMark()));
        newchildren.add(START);
        newchildren.addAll(children);

        children.clear();
        children.addAll(newchildren);

        changed = true;
        while (changed) {
            changed = false;
            children = simplifyRepeat1();
            if (newRules != null) {
                children.addAll(newRules);
                newRules = null;
            }

            children = simplifyRepeat0();
            if (newRules != null) {
                children.addAll(newRules);
                newRules = null;
            }
        }

        expandStrings();
        trimOptional();
    }

    private void expandStrings() {
        newRules = null;

        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IRule) {
                newchildren.add(child);
            } else {
                newchildren.add(child);
            }
        }

        children = newchildren;
    }

    protected void flattenNonterminals() {
        changed = true;
        while (changed) {
            changed = false;
            for (XNode child : children) {
                if (child instanceof IRule) {
                    child.children = ((IRule) child).flattenNonterminals();
                }
            }
            if (newRules != null) {
                children.addAll(newRules);
                newRules = null;
            }
        }
    }

    /**
     * Return an instance of this ixml grammar ready for processing by EarleyParser.
     *
     * @return The underlying grammar.
     */
    public Grammar getGrammar() {
        if (grammar == null) {
            constructGrammar();
        }

        return grammar;
    }

    private void constructGrammar() {
        ParserOptions options = new ParserOptions();
        options.treesWithStates = true;
        options.prefixParsing = true;
        //options.listener.setMessageLevel(ParseListener.DEBUG);
        grammar = new Grammar(options);

        ArrayList<ParserAttribute> attributes = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IRule) {
                IRule rule = (IRule) child;

                attributes.clear();
                attributes.add(new ParserAttribute("mark", ""+rule.getMark()));
                attributes.add(new ParserAttribute("name", rule.getName()));
                if (rule.getName().startsWith("$")) {
                    attributes.add(ParserAttribute.PRUNING_ALLOWED);
                }
                NonterminalSymbol ruleSymbol = grammar.getNonterminal(rule.getName(), attributes);

                ArrayList<Symbol> rhs = new ArrayList<>();
                for (XNode cat: rule.children) {
                    attributes.clear();

                    if (cat.isOptional()) {
                        attributes.add(Symbol.OPTIONAL);
                    }

                    if (cat instanceof XNonterminal) {
                        XNonterminal nt = (XNonterminal) cat;
                        if (cat instanceof INonterminal) {
                            attributes.add(new ParserAttribute("mark", ""+((INonterminal) cat).getMark()));
                            attributes.add(new ParserAttribute("name", cat.getName()));
                            if (cat.getName().startsWith("$")) {
                                attributes.add(ParserAttribute.PRUNING_ALLOWED);
                            }
                            if (cat.isOptional()) {
                                attributes.add(Symbol.OPTIONAL);
                            }
                        }


                        NonterminalSymbol nts = grammar.getNonterminal(nt.getName(), attributes);
                        rhs.add(nts);
                    } else if (cat instanceof ILiteral) {
                        ILiteral lit = (ILiteral) cat;

                        attributes.add(new ParserAttribute("tmark", ""+lit.getTMark()));

                        if (lit.getString() == null) {
                            int cp = Integer.parseInt(lit.getHex(), 16);
                            StringBuilder sb = new StringBuilder();
                            sb.appendCodePoint(cp);
                            rhs.add(new TerminalSymbol(TokenCharacter.get(sb.toString().charAt(0)), attributes));
                        } else {
                            String str = lit.getString();
                            for (int pos = 0; pos < str.length(); pos++) {
                                rhs.add(new TerminalSymbol(TokenCharacter.get(str.charAt(pos)), attributes));
                            }
                        }
                    } else if (cat instanceof XTerminal) {
                        XTerminal term = (XTerminal) cat;
                        rhs.add(term.getTerminal());
                    } else {
                        throw new RuntimeException("Unexpected category: " + cat.getName());
                    }
                }

                Rule grule = new Rule(ruleSymbol, rhs);

                grammarRules.add(grule);
                grammar.addRule(grule);
            }
        }
    }

    private HashSet<String> optionalSymbols(List<XNode> children) {
        HashSet<String> optional = new HashSet<>();
        ArrayList<IRule> rules = new ArrayList<>();
        boolean changed = true;

        for (XNode child : children) {
            if (child instanceof IRule) {
                rules.add((IRule) child);
            }
        }

        while (changed) {
            changed = false;
            for (IRule rule : rules) {
                if ((rule.isOptional() || rule.getChildren().isEmpty()) && !optional.contains(rule.getName())) {
                    changed = true;
                    optional.add(rule.getName());
                }

                for (XNode child : rule.getChildren()) {
                    boolean allOptional = true;
                    if (child instanceof XNonterminal) {
                        XNonterminal sym = (XNonterminal) child;
                        if (sym.isOptional()) {
                            if (!optional.contains(sym.getName())) {
                                changed = true;
                                optional.add(sym.getName());
                            }
                        } else {
                            allOptional = false;
                        }
                    } else {
                        allOptional = false;
                    }

                    if (allOptional && !optional.contains(rule.getName())) {
                        changed = true;
                        optional.add(rule.getName());
                    }
                }
            }
        }

        return optional;
    }

    /**
     * Return the rule for the given name.
     * @param name The rule name.
     * @return Return the rule.
     * @throws IllegalArgumentException if no rule with the specified name exists.
     */
    public IRule getRule(String name) {
        for (XNode child : children) {
            if (child instanceof IRule) {
                IRule rule = (IRule) child;
                if (name.equals(rule.getName())) {
                    return rule;
                }
            }
        }
        throw new IllegalArgumentException("No rule named " + name);
    }

    /**
     * Return a relatively crude XML serialization of the model.
     * @return An XML serialization as a string.
     */
    public String serialize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(baos, false, "UTF-8");
            dump(stream, "");
            stream.close();
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // this can't happen!
            throw new IxmlException("'Impossible' unsupported encoding for UTF-8: " + ex.getMessage(), ex);
        }
    }

    /**
     * Format a crude XML dump of this node on the specified stream.
     * @param stream The stream to which the model should be written.
     * @param indent The current indent.
     */
    @Override
    protected void dump(PrintStream stream, String indent) {
        stream.print(indent);
        stream.println("<" + nodeName + ">");

        HashSet<String> dumped = new HashSet<>();
        for (XNode rule : children) {
            if (rule instanceof IRule) {
                if (dumped.contains(rule.getName())) {
                    // nevermind
                } else {
                    dumped.add(rule.getName());
                    rule.dump(stream, indent + "  ");
                    for (XNode rest : children) {
                        if (rule != rest && (rest instanceof IRule)) {
                            if (rest.getName().equals(rule.getName())) {
                                rest.dump(stream, indent + "  ");
                            }
                        }
                    }
                }
            } else {
                rule.dump(stream, indent + "  ");
            }
        }

        stream.println("</" + nodeName + ">");
    }

    protected void addRule(IRule rule) {
        changed = true;
        if (newRules == null) {
            newRules = new ArrayList<>();
        }
        rule.parent = this;
        newRules.add(rule);
    }
}