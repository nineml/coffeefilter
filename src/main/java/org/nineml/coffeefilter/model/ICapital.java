package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'capital'.
 */
public class ICapital extends XNonterminal {
    /**
     * Create an ICapital.
     *
     * @param parent The parent node.
     */
    public ICapital(XNode parent) {
        super(parent, "capital");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ICapital newnode = new ICapital(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
