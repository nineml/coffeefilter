package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'encoded'.
 */
public class IEncoded extends XNonterminal implements TMarked {
    protected final char tmark;

    /**
     * Create an IEncoded.
     *
     * @param parent The parent node.
     * @param tmark The tmark.
     * @throws IllegalArgumentException if tmark is invalid.
     */
    public IEncoded(XNode parent, char tmark) {
        super(parent, "encoded");
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
        IEncoded newnode = new IEncoded(parent, tmark);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
