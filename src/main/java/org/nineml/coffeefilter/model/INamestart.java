package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'namestart'.
 */
public class INamestart extends XNonterminal {
    /**
     * Create an INamestart.
     *
     * @param parent The parent node.
     */
    public INamestart(XNode parent) {
        super(parent, "namestart");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        INamestart newnode = new INamestart(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
