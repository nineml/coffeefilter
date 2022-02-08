package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'from'.
 */
public class IFrom extends XNonterminal {
    /**
     * Create an IFrom.
     *
     * @param parent The parent node.
     */
    public IFrom(XNode parent) {
        super(parent, "from");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IFrom newnode = new IFrom(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
