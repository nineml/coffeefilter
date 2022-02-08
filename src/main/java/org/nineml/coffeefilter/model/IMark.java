package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'mark'.
 */
public class IMark extends XNonterminal {
    /**
     * Create an IMark.
     *
     * @param parent The parent node.
     */
    public IMark(XNode parent) {
        super(parent, "mark");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IMark newnode = new IMark(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
