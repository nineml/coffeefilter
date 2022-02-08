package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'cchar'. A 'cchar' is a character in a comment.
 */
public class ICChar extends XNonterminal {
    /**
     * Create an ICChar.
     *
     * @param parent The parent node.
     */
    public ICChar(XNode parent) {
        super(parent, "cchar");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ICChar newnode = new ICChar(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
