package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'to'.
 */
public class ITo extends XNonterminal {
    /**
     * Create an ITo.
     *
     * @param parent The parent node.
     */
    public ITo(XNode parent) {
        super(parent, "to");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ITo newnode = new ITo(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
