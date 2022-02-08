package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'sep'.
 */
public class ISep extends XNonterminal {
    /**
     * Create an ISep.
     *
     * @param parent The parent node.
     */
    public ISep(XNode parent) {
        super(parent, "sep");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ISep newnode = new ISep(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
