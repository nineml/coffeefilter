package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 's'.
 */
public class IS extends XNonterminal {
    /**
     * Create an IS.
     *
     * @param parent The parent node.
     */
    public IS(XNode parent) {
        super(parent, "s");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IS newnode = new IS(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
