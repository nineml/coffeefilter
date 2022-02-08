package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'cr'.
 */
public class ICr extends XNonterminal {
    /**
     * Create an ICr.
     *
     * @param parent The parent node.
     */
    public ICr(XNode parent) {
        super(parent, "cr");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ICr newnode = new ICr(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
