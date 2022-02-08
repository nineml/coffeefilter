package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'factor'.
 */
public class IFactor extends XNonterminal {
    /**
     * Create an IFactor.
     *
     * @param parent The parent node.
     */
    public IFactor(XNode parent) {
        super(parent, "factor");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IFactor newnode = new IFactor(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
