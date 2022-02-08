package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'sstring'.
 */
public class ISString extends XNonterminal {
    /**
     * Create an ISString.
     *
     * @param parent The parent node.
     */
    public ISString(XNode parent) {
        super(parent, "sstring");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ISString newnode = new ISString(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
