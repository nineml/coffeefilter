package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'dstring'.
 */
public class IDString extends XNonterminal {
    /**
     * Create an IDString.
     *
     * @param parent The parent node.
     */
    public IDString(XNode parent) {
        super(parent, "dstring");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IDString newnode = new IDString(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
