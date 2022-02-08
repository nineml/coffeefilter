package org.nineml.coffeefilter.model;

/**
 * An abstract class representing nonterminal nodes.
 */
public abstract class XNonterminal extends XNode {
    /**
     * The base constructor for nonterminals.
     *
     * <p>The model uses Nonterminal as the base class for nonterminals in the grammar, not
     * just user defined nonterminals.</p>
     *
     * @param parent The parent.
     * @param nodeName The node name, that is, the name of the ixml node type (alt, alts, ...)
     * @param name The name of the nonterminal.
     */
    protected XNonterminal(XNode parent, String nodeName, String name) {
        super(parent, nodeName);
        this.name = name;
    }

    /**
     * The base constructor for nonterminals.
     *
     * <p>This flavor is used for nonterminals that don't have a name.</p>
     *
     * @param parent The parent.
     * @param nodeName The node name, that is, the name of the ixml node type (alt, alts, ...)
     */
    protected XNonterminal(XNode parent, String nodeName) {
        this(parent, nodeName, null);
    }

    /**
     * Return a string representation of this node.
     * @return A string.
     */
    @Override
    public String toString() {
        String str = name == null ? nodeName : name;
        if (optional) {
            str += "?";
        }
        return str;
    }
}