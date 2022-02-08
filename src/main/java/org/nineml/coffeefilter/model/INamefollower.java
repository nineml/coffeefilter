package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'namefollower'.
 */
public class INamefollower extends XNonterminal {
    /**
     * Create an INamefollower.
     *
     * @param parent The parent node.
     */
    public INamefollower(XNode parent) {
        super(parent, "namefollower");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        INamefollower newnode = new INamefollower(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
