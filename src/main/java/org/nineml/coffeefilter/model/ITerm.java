package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'term'.
 */
public class ITerm extends XNonterminal {
    /**
     * Create an ITerm.
     *
     * @param parent The parent node.
     */
    public ITerm(XNode parent) {
        super(parent, "term");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ITerm newnode = new ITerm(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
