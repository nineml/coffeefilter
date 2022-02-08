package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'set'.
 */
public class ISet extends XNonterminal {
    /**
     * Create an ISet.
     *
     * @param parent The parent node.
     */
    public ISet(XNode parent) {
        super(parent, "set");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ISet newnode = new ISet(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
