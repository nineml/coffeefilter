package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'schar'.
 */
public class ISChar extends XNonterminal {
    /**
     * Create an ISChar.
     *
     * @param parent The parent node.
     */
    public ISChar(XNode parent) {
        super(parent, "schar");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ISChar newnode = new ISChar(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
