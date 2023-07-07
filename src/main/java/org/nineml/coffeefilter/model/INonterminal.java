package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.exceptions.IxmlException;

import java.io.PrintStream;

/**
 * Model an Invisible XML 'nonterminal'.
 */
public class INonterminal extends XNonterminal {
    protected char mark = '?';
    protected String rename = null;

    /**
     * Create an INonterminal.
     *
     * @param parent The parent node.
     * @param name The nonterminal name.
     * @param mark The mark.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if mark is invalid.
     */
    public INonterminal(XNode parent, String name, char mark) {
        this(parent, name, null, mark);
    }

    /**
     * Create an INonterminal.
     *
     * @param parent The parent node.
     * @param name The nonterminal name.
     * @param mark The mark.
     * @throws NullPointerException if the name is null.
     * @throws IllegalArgumentException if mark is invalid.
     */
    public INonterminal(XNode parent, String name, String rename, char mark) {
        super(parent, "nonterminal", name);

        if (name == null) {
            throw new NullPointerException("Unnamed nonterminal?");
        }

        if (mark != '^' && mark != '@' && mark != '-') {
            throw IxmlException.invalidMark(mark);
        }

        this.rename = rename;
        this.mark = mark;
    }

    /**
     * Create an INonterminal.
     *
     * @param parent The parent node.
     * @param name The nonterminal name.
     * @throws NullPointerException if the name is null.
     */
    public INonterminal(XNode parent, String name) {
        this(parent, name, null);
    }

    /**
     * Create an INonterminal.
     *
     * @param parent The parent node.
     * @param name The nonterminal name.
     * @throws NullPointerException if the name is null.
     */
    public INonterminal(XNode parent, String name, String rename) {
        super(parent, "nonterminal", name);

        if (name == null) {
            throw new NullPointerException("Unnamed nonterminal?");
        }

        this.rename = rename;
        mark = '?';
    }

    /**
     * Create an INonterminal.
     *
     * <p>This is a convenience constructor that creates an INonterminal with the specified name.
     * The parent is <code>null</code> and the mark is "-".</p>
     *
     * @param name The nonterminal name.
     */
    public INonterminal(String name) {
        this(null, name, '-');
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        INonterminal newnode = new INonterminal(parent, name, rename);
        newnode.pragmas.addAll(pragmas);
        newnode.mark = mark;
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Return the mark.
     * @return The mark.
     */
    public char getMark() {
        if (mark == '?') {
            IRule rule = getRoot().getRule(name);
            if (rule == null) {
                mark = '^'; // it's irrelevant since any reference to the symbol will throw an exception
            } else {
                mark = rule.getMark();
            }
        }
        return mark;
    }

    public String getRename() {
        String newName = null;
        for (IPragma pragma : getPragmas()) {
            if (pragma instanceof IPragmaRename) {
                if (newName != null) {
                    throw IxmlException.repeatedPragma("rename", newName, pragma.getPragmaData());
                }
                newName = pragma.getPragmaData();
            }
        }

        if (newName != null) {
            return newName;
        }

        if (rename != null) {
            Ixml ixml = getRoot();
            if (!"1.1-nineml".equals(ixml.getIxmlVersion())) {
                throw IxmlException.renameUnavailable(name, rename);
            }
            return rename;
        }

        // N.B. rule can be null if the symbol is undefined in the grammar
        IRule rule = getRoot().getRule(name);
        if (rule != null && rule.getRename() != null) {
            return rule.getRename();
        }
        return name;
    }

    /**
     * Format a crude XML dump of this node on the specified stream.
     * @param stream The stream to which the model should be written.
     * @param indent The current indent.
     */
    @Override
    protected void dump(PrintStream stream, String indent) {
        stream.print(indent);
        stream.print("<" + nodeName);
        if (mark != '?') {
            stream.print(" mark='" + mark + "'");
        }
        dumpBody(stream, indent);
    }
}
