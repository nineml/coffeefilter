package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'lf'.
 */
public class ILf extends XNonterminal {
    /**
     * Create an Ilf.
     *
     * @param parent The parent node.
     */
    public ILf(XNode parent) {
        super(parent, "lf");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ILf newnode = new ILf(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
