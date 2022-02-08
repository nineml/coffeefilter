package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'quoted'.
 */
public class IQuoted extends XNonterminal implements TMarked {
    protected final char tmark;

    /**
     * Create an IQuoted.
     *
     * @param parent The parent node.
     * @param tmark The tmark.
     * @throws IllegalArgumentException if tmark is invalid.
     */
    public IQuoted(XNode parent, char tmark) {
        super(parent, "quoted");
        if (tmark != '^' && tmark != '-') {
            throw new IllegalArgumentException("tmark must be '^' or '-'.");
        }
        this.tmark = tmark;
    }

    /**
     * Returns the tmark.
     * @return The tmark.
     */
    public char getTMark() {
        return tmark;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IQuoted newnode = new IQuoted(parent, tmark);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
