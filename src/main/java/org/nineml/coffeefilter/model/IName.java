package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'name'.
 */
public class IName extends XNonterminal {
    /**
     * Create an IName.
     *
     * @param parent The parent node.
     */
    public IName(XNode parent) {
        super(parent, "name");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IName newnode = new IName(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
