package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'alts'.
 */
public class IAlts extends XNonterminal {
    /**
     * Create an IAlts.
     *
     * @param parent The parent node.
     */
    public IAlts(XNode parent) {
        super(parent, "alts");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IAlts newnode = new IAlts(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
