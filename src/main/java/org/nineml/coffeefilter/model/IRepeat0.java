package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'repeat0'.
 */
public class IRepeat0 extends XNonterminal {
    /**
     * Create an IRepeat0.
     *
     * @param parent The parent node.
     */
    public IRepeat0(XNode parent) {
        super(parent, "repeat0");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IRepeat0 newnode = new IRepeat0(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
