package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'alt'.
 */
public class IAlt extends XNonterminal {
    /**
     * Create an IAlt.
     *
     * @param parent The parent node.
     */
    public IAlt(XNode parent) {
        super(parent, "alt");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IAlt newnode = new IAlt(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
