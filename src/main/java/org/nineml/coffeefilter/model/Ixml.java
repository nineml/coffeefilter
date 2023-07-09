package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Model an Invisible XML grammar. This class represents the "top" of an ixml grammar.
 * This is what you get back if you parse a grammar.
 */
public class Ixml extends XNonterminal {
    protected int symCount = 0;
    private boolean changed = true;
    private ArrayList<IRule> newRules = null;
    private final ArrayList<Rule> grammarRules = new ArrayList<>();
    private final String startRule = "$$";
    protected final ParserOptions options;
    protected final HashMap<String,String> ixmlns;
    protected final HashMap<String,String> pragmaDecl;
    private SourceGrammar grammar = null;
    protected IRule emptyProduction = null;
    protected String version = "1.0";
    protected final RuleRewriter ruleRewriter;
    protected final ArrayList<XNode> originalRules;
    /**
     * Construct an Ixml.
     */
    protected Ixml(ParserOptions options) {
        super(null, "ixml", "$$_ixml");
        this.options = options;
        ixmlns = new HashMap<>();
        pragmaDecl = new HashMap<>();

        if (options.getRuleRewriter() == null) {
            if ("GLL".equals(options.getParserType())) {
                ruleRewriter = new RuleRewriterSpec();
            } else {
                ruleRewriter = new RuleRewriterAlternate();
            }
        } else {
            ruleRewriter = options.getRuleRewriter();
        }

        ruleRewriter.setRoot(this);
        originalRules = new ArrayList<>();
    }

    /**
     * Construct Ixml from a grammar.
     * @param grammar the grammar.
     */
    public Ixml(ParserOptions options, SourceGrammar grammar) {
        super(null, "ixml", "$$_ixml");
        this.grammar = grammar;
        symCount += grammar.getRules().size();
        grammarRules.addAll(grammar.getRules());
        this.options = options;
        ixmlns = new HashMap<>();
        pragmaDecl = new HashMap<>();

        if (options.getRuleRewriter() == null) {
            if ("GLL".equals(options.getParserType())) {
                ruleRewriter = new RuleRewriterSpec();
            } else {
                ruleRewriter = new RuleRewriterAlternate();
            }
        } else {
            ruleRewriter = options.getRuleRewriter();
        }

        ruleRewriter.setRoot(this);
        originalRules = new ArrayList<>();
    }

    public String getIxmlVersion() {
        return version;
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
    public String nextRuleName(String name) {
        symCount += 1;
        return "$" + symCount + "_" + name;
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
     * Get the parser options for this parser.
     * @return the parser options.
     */
    public ParserOptions getOptions() {
        return options;
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
        Ixml newnode = new Ixml(options);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    private List<IRule> ruleChildren() {
        ArrayList<IRule> rules = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IRule) {
                rules.add((IRule) child);
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
    public void simplifyGrammar(ParserOptions options) {
        // Make sure there's only one rule for each nonterminal before we begin.
        HashSet<String> definedSymbols = new HashSet<>();
        for (XNode node : children) {
            if (node instanceof IRule) {
                IRule rule = (IRule) node;
                if (definedSymbols.contains(rule.getName()) && !options.getAllowMultipleDefinitions()) {
                    throw IxmlException.multipleDefinitionsOfSymbol(rule.getName());
                }
                definedSymbols.add(rule.getName());
            }
        }

        setupOriginalRules();

        flatten();

        ArrayList<XNode> newchildren = new ArrayList<>();
        for (IRule rule : ruleChildren()) {
            if (!rule.children.isEmpty() && rule.children.get(0) instanceof IAlt) {
                for (XNode alt : rule.children) {
                    if (!(alt instanceof IAlt)) {
                        throw new RuntimeException("Mixture of alts and other things?");
                    }
                    IRule newRule = new IRule(this, rule.getName(), rule.getMark());
                    newRule.pragmas.addAll(rule.pragmas);
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
        constructGrammar(options);
    }

    private void setupOriginalRules() {
        for (XNode child : children) {
            XNode copied = child.copy();
            originalRules.add(copied);
            setupDerivation(child, copied);
        }
    }

    private void setupDerivation(XNode original, XNode copy) {
        original.derivedFrom = copy;
        for (int pos = 0; pos < original.children.size(); pos++) {
            setupDerivation(original.children.get(pos), copy.children.get(pos));
        }
    }

    protected void simplify() {
        // Make sure we have a top-level rule that will have only one rhs
        // even if the user's seed rule winds up having alternatives
        ArrayList<XNode> newchildren = new ArrayList<>();
        IRule startSymbol = new IRule(this, startRule, '-');

        IRule firstRule = null;
        int pos = 0;
        while (!(children.get(pos) instanceof IRule)) {
            pos++;
        }
        firstRule = (IRule) children.get(pos);

        startSymbol.children.add(new INonterminal(startSymbol, firstRule.getName(), firstRule.getMark()));
        newchildren.add(startSymbol);
        newchildren.addAll(children);

        children.clear();
        children.addAll(newchildren);
        newchildren = null;

        // N.B. The order of these steps matters
        for (RuleRewrites rewrite : ruleRewriter.rewriteOrder()) {
            switch (rewrite) {
                case REPEAT0SEP:
                    children = simplifyRepeat0Sep();
                    break;
                case REPEAT1SEP:
                    children = simplifyRepeat1Sep();
                    break;
                case REPEAT0:
                    children = simplifyRepeat0();
                    break;
                case REPEAT1:
                    children = simplifyRepeat1();
                    break;
                case OPTION:
                    children = simplifyOption();
                    break;
                default:
                    throw new IllegalStateException("Unexpected rewrite rule: " + rewrite);
            }
            if (newRules != null) {
                children.addAll(newRules);
                newRules = null;
            }
        }
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
     * <p>The contructed grammar is cached. Calling this method with different parser
     * options will have no effect.</p>
     * @return The underlying grammar.
     */
    public SourceGrammar getGrammar(ParserOptions options) {
        if (grammar == null) {
            constructGrammar(options);
        }

        return grammar;
    }

    private void constructGrammar(ParserOptions options) {
        grammar = new SourceGrammar(options);

        ArrayList<ParserAttribute> attributes = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IRule) {
                IRule rule = (IRule) child;

                attributes.clear();
                attributes.add(new ParserAttribute(InvisibleXml.MARK_ATTRIBUTE, String.valueOf(rule.getMark())));
                attributes.add(new ParserAttribute(InvisibleXml.NAME_ATTRIBUTE,
                        rule.getRename() == null ? rule.getName() : rule.getRename()));

                if (startRule.equals(rule.getName())) {
                    for (IPragma pragma : pragmas) {
                        if (pragma instanceof IPragmaXmlns) {
                            attributes.add(new ParserAttribute(InvisibleXml.XMLNS_ATTRIBUTE, pragma.getPragmaData()));
                        } else {
                            options.getLogger().debug(logcategory, "Unknown pragma, or does not apply in the prologue: %s", pragma);
                        }
                    }
                }

                String regex = null;
                for (IPragma pragma : rule.pragmas) {
                    if (pragma instanceof IPragmaRegex) {
                        regex = pragma.getPragmaData();
                        //attributes.add(new ParserAttribute(ParserAttribute.REGEX_NAME, pragma.getPragmaData()));
                    } else if (pragma instanceof IPragmaPriority) {
                        attributes.add(new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, pragma.getPragmaData()));
                    } else {
                        options.getLogger().debug(logcategory, "Unknown pragma, or does not apply to rule: %s", pragma);
                    }
                }

                NonterminalSymbol ruleSymbol = grammar.getNonterminal(rule.getName(), attributes);

                List<XNode> children = new ArrayList<>(rule.children);
                ArrayList<Symbol> rhs = new ArrayList<>();

                if (regex != null) {
                    children.clear();
                    TokenRegex regexToken = TokenRegex.get(regex);
                    rhs.add(new TerminalSymbol(regexToken));
                }

                for (XNode cat: children) {
                    attributes.clear();

                    if (cat instanceof IInsertion) {
                        IInsertion nt = (IInsertion) cat;
                        attributes.add(new ParserAttribute(InvisibleXml.MARK_ATTRIBUTE, "+"));
                        attributes.add(new ParserAttribute(InvisibleXml.INSERTION_ATTRIBUTE, nt.getInsertion()));
                        for (IPragma pragma : cat.pragmas) {
                            options.getLogger().debug(logcategory, "Unknown pragma, or does not apply to a insertion: %s", pragma);
                        }

                        String iname = nt.getInsertion().replaceAll("\\s+", "_");
                        iname = "+" + iname.replaceAll("[^A-Za-z_-]", "");

                        NonterminalSymbol nts = new InsertionNonterminal(grammar, nextRuleName(iname), attributes);
                        rhs.add(nts);
                        grammar.addRule(nts);
                    } else if (cat instanceof XNonterminal) {
                        XNonterminal nt = (XNonterminal) cat;
                        if (cat instanceof INonterminal) {
                            INonterminal icat = (INonterminal) cat;
                            attributes.add(new ParserAttribute(InvisibleXml.MARK_ATTRIBUTE, String.valueOf(icat.getMark())));
                            String name = icat.getRename();

                            List<IPragma> relevant = relevantPragmas(cat);
                            for (IPragma pragma : relevant) {
                                switch (pragma.ptype) {
                                    case PRIORITY:
                                        attributes.add(new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, pragma.getPragmaData()));
                                        break;
                                    case RENAME:
                                        name = pragma.getPragmaData();
                                        break;
                                    case DISCARD_EMPTY:
                                        attributes.add(new ParserAttribute(InvisibleXml.DISCARD_ATTRIBUTE, pragma.getPragmaData()));
                                        break;
                                    default:
                                        options.getLogger().debug(logcategory, "Unknown pragma, or does not apply to a nonterminal: %s", pragma);
                                        break;
                                }
                            }

                            attributes.add(new ParserAttribute(InvisibleXml.NAME_ATTRIBUTE, name));
                        }

                        NonterminalSymbol nts = grammar.getNonterminal(nt.getName(), attributes);
                        rhs.add(nts);
                    } else if (cat instanceof ILiteral) {
                        ILiteral lit = (ILiteral) cat;
                        final String grammarTerminal = lit.getTokenString();

                        attributes.add(new ParserAttribute(InvisibleXml.TMARK_ATTRIBUTE, String.valueOf(lit.getTMark())));

                        List<IPragma> relevant = relevantPragmas(cat);
                        for (IPragma pragma : relevant) {
                            switch (pragma.ptype) {
                                case REGEX:
                                    attributes.add(new ParserAttribute(ParserAttribute.REGEX_NAME, pragma.getPragmaData()));
                                    break;
                                case PRIORITY:
                                    attributes.add(new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, pragma.getPragmaData()));
                                    break;
                                default:
                                    options.getLogger().debug(logcategory, "Unknown pragma, or does not apply to a literal: %s", pragma);
                                    break;
                            }
                        }

                        if ("".equals(grammarTerminal)) {
                            rhs.add(new TerminalSymbol(TokenString.get(""), attributes));
                        } else {
                            ArrayList<Integer> codepoints = new ArrayList<>();
                            for (int offset = 0; offset < grammarTerminal.length(); ) {
                                int codepoint = grammarTerminal.codePointAt(offset);
                                codepoints.add(codepoint);
                                offset += Character.charCount(codepoint);
                            }

                            for (Integer codepoint : codepoints) {
                                rhs.add(new TerminalSymbol(TokenCharacter.get(codepoint), attributes));
                            }
                        }
                    } else if (cat instanceof XTerminal) {
                        XTerminal term = (XTerminal) cat;

                        if (term instanceof TMarked) {
                            char mark = ((TMarked) term).getTMark();
                            attributes.add(new ParserAttribute(InvisibleXml.TMARK_ATTRIBUTE, String.valueOf(mark)));
                        }

                        List<IPragma> relevant = relevantPragmas(cat);
                        for (IPragma pragma : relevant) {
                            switch (pragma.ptype) {
                                case REGEX:
                                    attributes.add(new ParserAttribute(ParserAttribute.REGEX_NAME, pragma.getPragmaData()));
                                    break;
                                case PRIORITY:
                                    attributes.add(new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, pragma.getPragmaData()));
                                    break;
                                default:
                                    options.getLogger().debug(logcategory, "Unknown pragma, or does not apply to a terminal: %s", pragma);
                                    break;
                            }
                        }

                        rhs.add(new TerminalSymbol(term.getToken(), attributes));
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

    private List<IPragma> relevantPragmas(XNode cat) {
        HashSet<IPragma.PragmaType> seen = new HashSet<>();
        ArrayList<IPragma> pragmas = new ArrayList<>();
        for (IPragma pragma : cat.pragmas) {
            pragmas.add(pragma);
            seen.add(pragma.ptype);
        }
        if (cat instanceof XNonterminal) {
            XNode find = getRule(cat.getName());
            if (find != null) {
                for (IPragma pragma : find.pragmas) {
                    if (pragma.inherit && !seen.contains(pragma.ptype)) {
                        pragmas.add(pragma);
                    }
                }
            }
        }
        return pragmas;
    }

    /**
     * Return the rule for the given name.
     * @param name The rule name.
     * @return Return the rule, or null if no such rule exists.
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
        return null;
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
            throw IxmlException.internalError("unsupported encoding: UTF-8: " + ex.getMessage(), ex);
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
