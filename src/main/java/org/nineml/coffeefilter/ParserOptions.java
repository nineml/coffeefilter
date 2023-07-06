package org.nineml.coffeefilter;

import org.nineml.coffeefilter.model.RuleRewriter;
import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Options to the Invisible XML processor.
 * <p>This object is extended by other members of the NineML family to provide additional options.
 * It started out as a collection of public fields, but changed to a more traditional collection of
 * getters and setters when it began to develop options that were not entirely independent.</p>
 */
public class ParserOptions extends org.nineml.coffeegrinder.parser.ParserOptions {
    private boolean ignoreTrailingWhitespace = false;
    private boolean allowUndefinedSymbols = false;
    private boolean allowUnreachableSymbols = true;
    private boolean allowUnproductiveSymbols = true;
    private boolean allowMultipleDefinitions = false;
    private boolean prettyPrint = false;
    private boolean showChart = false;
    private String graphviz = null;
    private final HashSet<String> suppressedIxmlStates;
    private boolean assertValidXmlNames = true;
    private boolean assertValidXmlCharacters = true;
    private boolean pedantic = false;
    private boolean showMarks = false;
    private boolean showBnfNonterminals = false;
    private RuleRewriter ruleRewriter = null;
    private boolean ignoreBOM = true;
    private boolean strictAmbiguity = false;
    private final HashSet<String> disabledPragmas;
    private static final HashSet<String> knownPragmas = new HashSet<>(Arrays.asList("discard-empty", "ns", "priority", "regex", "rename", "token"));

    /**
     * Create the parser options.
     * <p>The initial logger will be a {@link DefaultLogger} initialized with
     * {@link DefaultLogger#readSystemProperties readSystemProperties()}.</p>
     */
    public ParserOptions() {
        super();
        suppressedIxmlStates = new HashSet<>();
        disabledPragmas = new HashSet<>();
    }

    /**
     * Create the parser options with an explicit logger.
     * @param logger the logger.
     */
    public ParserOptions(Logger logger) {
        super(logger);
        suppressedIxmlStates = new HashSet<>();
        disabledPragmas = new HashSet<>();
    }

    /**
     * Create a new set of options from an existing set.
     * @param copy the options to copy
     */
    public ParserOptions(ParserOptions copy) {
        super(copy);
        ignoreTrailingWhitespace = copy.ignoreTrailingWhitespace;
        allowUndefinedSymbols = copy.allowUndefinedSymbols;
        allowUnreachableSymbols = copy.allowUnreachableSymbols;
        allowUnproductiveSymbols = copy.allowUnproductiveSymbols;
        allowMultipleDefinitions = copy.allowMultipleDefinitions;
        prettyPrint = copy.prettyPrint;
        showChart = copy.showChart;
        graphviz = copy.graphviz;
        suppressedIxmlStates = new HashSet<>(copy.suppressedIxmlStates);
        assertValidXmlNames = copy.assertValidXmlNames;
        pedantic = copy.pedantic;
        showMarks = copy.showMarks;
        showBnfNonterminals = copy.showBnfNonterminals;
        assertValidXmlCharacters = copy.assertValidXmlCharacters;
        ruleRewriter = copy.ruleRewriter;
        strictAmbiguity = copy.strictAmbiguity;
        ignoreBOM = copy.ignoreBOM;
        disabledPragmas = new HashSet<>(copy.disabledPragmas);
    }

    /**
     * Ignore trailing whitespace?
     * <p>If a parse fails where it would have succeeded if trailing whitespace was
     * removed from the input, report success.</p>
     * @return true if trailing whitespace is ignored
     */
    public boolean getIgnoreTrailingWhitespace() {
        return ignoreTrailingWhitespace;
    }

    /**
     * Set the {@link #getIgnoreTrailingWhitespace()} property.
     * @param ignore ignore trailing whitespace?
     */
    public void setIgnoreTrailingWhitespace(boolean ignore) {
        ignoreTrailingWhitespace = ignore;
    }

    /**
     * Allow undefined symbols?
     * <p>A grammar with undefined symbols isn't necessarily unusable. But Invisible XML
     * forbids them.</p>
     * @return true if undefined symbols are allowed
     */
    public boolean getAllowUndefinedSymbols() {
        return allowUndefinedSymbols;
    }

    /**
     * Set the {@link #getAllowUndefinedSymbols()} property.
     * <p>Allowing undefined symbols implies {@link #getAllowUnproductiveSymbols()} and
     * {@link #getAllowUndefinedSymbols()}.
     * An undefined symbol is always part of an unproductive rule and avoiding errors for
     * undefined rules often introduces undefined symbols.</p>
     * @param allow allow undefined symbols?
     */
    public void setAllowUndefinedSymbols(boolean allow) {
        allowUndefinedSymbols = allow;
        if (allowUndefinedSymbols) {
            allowUnproductiveSymbols = true;
        }
    }

    /**
     * Allow unreachable symbols?
     * <p>A grammar with unreachable symbols isn't forbidden by Invisible XML, but might
     * still be an error.</p>
     * @return true if unreachable symbols are allowed.
     */
    public boolean getAllowUnreachableSymbols() {
        return allowUnreachableSymbols;
    }

    /**
     * Set the {@link #getAllowUnreachableSymbols()} property.
     * @param allow allow unreachable symbols?
     */
    public void setAllowUnreachableSymbols(boolean allow) {
        allowUnreachableSymbols = allow;
    }

    /**
     * Allow unproductive symbols?
     * <p>An unproductive symbol is one which can never produce output in a valid parse.</p>
     * @return true if unproductive rules are allowed
     */
    public boolean getAllowUnproductiveSymbols() {
        return allowUnproductiveSymbols;
    }

    /**
     * Set the {@link #getAllowUnproductiveSymbols()} property.
     * @param allow allow unproductive rules?
     */
    public void setAllowUnproductiveSymbols(boolean allow) {
        allowUnproductiveSymbols = allow;
    }

    /**
     * Allow multiple definitions?
     * <p>A grammar with multiply defined symbols isn't a problem for the underlying Earley
     * parser, but it is forbidden by Invisible XML.</p>
     * @return true if multiple definitions are allowed.
     */
    public boolean getAllowMultipleDefinitions() {
        return allowMultipleDefinitions;
    }

    /**
     * Set the {@link #getAllowMultipleDefinitions()} property.
     * @param allow allow multiple definitions?
     */
    public void setAllowMultipleDefinitions(boolean allow) {
        allowMultipleDefinitions = allow;
    }

    /**
     * Attempt to pretty print the result?
     * <p>If the result is pretty printed, extra newlines and spaces for indentation
     * will be added to the result.</p>
     * @return true if the result should be pretty printed.
     */
    public boolean getPrettyPrint() {
        return prettyPrint;
    };

    /**
     * Set the {@link #getPrettyPrint()} property.
     * @param prettPrint pretty print?
     */
    public void setPrettyPrint(boolean prettPrint) {
        this.prettyPrint = prettPrint;
    }

    /**
     * Show the Earley chart in error results?
     * @return true if the early chart should appear in error results.
     */
    public boolean getShowChart() {
        return showChart;
    };

    /**
     * Set the {@link #getShowChart()} property.
     * @param show show the chart?
     */
    public void setShowChart(boolean show) {
        showChart = show;
    }

    /**
     * Where's the GraphViz 'dot' command?
     * @return the path of the dot command.
     */
    public String getGraphviz() {
        return graphviz;
    };

    /**
     * Set the {@link #getGraphviz()} property.
     * <p>Setting the path to <code>null</code> disables SVG output.</p>
     * @param dot the path to the dot command.
     */
    public void setGraphviz(String dot) {
        graphviz = dot;
    }

    /**
     * Is the specified state suppressed?
     * <p>The Invisible XML processor adds state values to the root element to report
     * conditions such as ambiguity or a prefix parse. These states can be suppressed.</p>
     * @param state the state
     * @return true if the state is suppressed.
     */
    public boolean isSuppressedState(String state) {
        return suppressedIxmlStates.contains(state);
    }

    /**
     * Suppress a state.
     * <p>The Invisible XML processor adds state values to the root element to report
     * conditions such as ambiguity or a prefix parse. These states can be suppressed.</p>
     * @param state the state to suppress.
     */
    public void suppressState(String state) {
        if (!"ambiguous".equals(state) && !"prefix".equals(state)) {
            getLogger().warn("CoffeeFilter", "Unknown state: %s", state);
        }
        suppressedIxmlStates.add(state);
    }

    /**
     * Expose a state.
     * <p>The Invisible XML processor adds state values to the root element to report
     * conditions such as ambiguity or a prefix parse. These states can be suppressed.</p>
     * @param state the state to (no longer) suppress
     */
    public void exposeState(String state) {
        if (!"ambiguous".equals(state) && !"prefix".equals(state)) {
            getLogger().warn("CoffeeFilter", "Unknown state: %s", state);
        }
        suppressedIxmlStates.remove(state);
    }

    /**
     * Raise an exception if invalid XML names are used in nonterminals that are serialized.
     * @return true if invalid XML names should raise an exception
     */
    public boolean getAssertValidXmlNames() {
        return assertValidXmlNames;
    }

    /**
     * Set the {@link #getAssertValidXmlNames()} property.
     * @param valid assert valid names?
     */
    public void setAssertValidXmlNames(boolean valid) {
        assertValidXmlNames = valid;
    }

    /**
     * Raise an exception if invalid XML characters are used in text or attribute values that are serialized.
     * @return true if invalid XML characters should raise an exception
     */
    public boolean getAssertValidXmlCharacters() {
        return assertValidXmlCharacters;
    }

    /**
     * Set the {@link #getAssertValidXmlCharacters()} property.
     * @param valid assert valid characters?
     */
    public void setAssertValidXmlCharacters(boolean valid) {
        assertValidXmlCharacters = valid;
    }

    /**
     * Enforce strict compliance to the Invisible XML specification.
     * <p>In pedantic mode, the processor won't allow grammar extensions, like pragmas,
     * that are not yet officially incorporated into the specification.</p>
     * @return true if pedantic mode is enabled
     */
    public boolean getPedantic() {
        return pedantic;
    };

    /**
     * Set the {@link #getPedantic()} property.
     * <p>In pedantic mode, the processor won't allow grammar extensions, like pragmas,
     * that are not yet officially incorporated into the specification. Enabling pedantic
     * mode also resets the allowed grammar hygiene rules to their defaults.</p>
     * @param pedantic be pedantic?
     */
    public void setPedantic(boolean pedantic) {
        this.pedantic = pedantic;
        if (pedantic) {
            allowUndefinedSymbols = false;
            allowUnreachableSymbols = true;
            allowUnproductiveSymbols = true;
            allowMultipleDefinitions = false;
        }
    }

    public boolean getShowMarks() {
        return showMarks;
    }
    public void setShowMarks(boolean show) {
        showMarks = show;
    }
    public boolean getShowBnfNonterminals() {
        return showBnfNonterminals;
    }
    public void setShowBnfNonterminals(boolean show) {
        showBnfNonterminals = show;
    }

    public void setRuleRewriter(RuleRewriter rewriter) {
        ruleRewriter = rewriter;
    }
    public RuleRewriter getRuleRewriter() {
        return ruleRewriter;
    }

    /**
     * If a UTF-8 input stream begins with a byte order mark (BOM), ignore it.
     * @return the ignore BOM setting.
     */
    public boolean getIgnoreBOM() {
        return ignoreBOM;
    }

    /**
     * Set the {@link #getIgnoreBOM()} property.
     * @param ignore Ignore the BOM?
     */
    public void setIgnoreBOM(boolean ignore) {
        ignoreBOM = ignore;
    }

    /**
     * If a grammar contains priority pragmas that uniquely determine the outcome of every
     * potentially ambiguous choice, report that the grammar is ambiguous anyway.
     * @return the strict ambiguity setting
     */
    public boolean getStrictAmbiguity() {
        return strictAmbiguity;
    }

    /**
     * Set the {@link #getStrictAmbiguity()} property.
     * @param strict Strictly report ambiguity?
     */
    public void setStrictAmbiguity(boolean strict) {
        strictAmbiguity = strict;
    }

    /**
     * Is the specified pragma disabled?
     * <p>The user can selectively disable pragmas in a grammar. This method determines
     * whether the specified pragma is disabled.</p>
     * @param pragma the pragma name
     * @return true if the pragma is disabled.
     */
    public boolean pragmaDisabled(String pragma) {
        return disabledPragmas.contains(pragma);
    }

    /**
     * Disable the specified pragma.
     * <p>The user can selectively ignore pragmas in a grammar. This method specifies that
     * a particular pragma should be disabled. The token "#all" disables all pragmas.</p>
     * @param pragma the pragma to disable.
     */
    public void disablePragma(String pragma) {
        if ("#all".equals(pragma)) {
            disabledPragmas.addAll(knownPragmas);
        } else {
            if (!knownPragmas.contains(pragma)) {
                getLogger().warn("CoffeeFilter", "Attempt to disable unknown pragma: %s", pragma);
            }
            disabledPragmas.add(pragma);
        }
    }

    /**
     * Disable the specified pragma.
     * <p>The user can selectively ignore pragmas in a grammar. This method specifies that
     * a particular pragma should be enabled. The token "#all" enables all pragmas.</p>
     * @param pragma the state to suppress.
     */
    public void enablePragma(String pragma) {
        if ("#all".equals(pragma)) {
            disabledPragmas.clear();
        } else {
            if (!knownPragmas.contains(pragma)) {
                getLogger().warn("CoffeeFilter", "Attempt to enable unknown pragma: %s", pragma);
            }
            disabledPragmas.remove(pragma);
        }
    }
}
