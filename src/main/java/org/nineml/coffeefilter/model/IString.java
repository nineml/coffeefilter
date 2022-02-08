package org.nineml.coffeefilter.model;

public class IString extends XNonterminal {
    /**
     * Create an IString.
     *
     * @param parent The parent node.
     */
    public IString(XNode parent) {
        super(parent, "string");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IString newnode = new IString(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
