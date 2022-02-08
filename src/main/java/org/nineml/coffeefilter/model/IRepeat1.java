package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'repeat1'.
 */
public class IRepeat1 extends XNonterminal {
    /**
     * Create an IRepeat1.
     *
     * @param parent The parent node.
     */
    public IRepeat1(XNode parent) {
        super(parent, "repeat1");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IRepeat1 newnode = new IRepeat1(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
