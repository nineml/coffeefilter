package org.nineml.coffeefilter.model;

import java.io.PrintStream;

/**
 * Model an Invisible XML 'nonterminal'.
 */
public class INonterminal extends XNonterminal {
    protected char mark = '?';

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
        super(parent, "nonterminal", name);

        if (name == null) {
            throw new NullPointerException("Unnamed nonterminal?");
        }

        if (mark != '^' && mark != '@' && mark != '-') {
            throw new IllegalArgumentException("mark must be '@', '^', or '-'");
        }

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
        super(parent, "nonterminal", name);

        if (name == null) {
            throw new NullPointerException("Unnamed nonterminal?");
        }

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
        INonterminal newnode = new INonterminal(parent, name);
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
            mark = rule.getMark();
        }
        return mark;
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
