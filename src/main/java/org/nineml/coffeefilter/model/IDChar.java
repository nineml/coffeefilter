package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'dchar'.
 */
public class IDChar extends XNonterminal {
    /**
     * Create an IDChar.
     *
     * @param parent The parent node.
     */
    public IDChar(XNode parent) {
        super(parent, "dchar");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IDChar newnode = new IDChar(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
