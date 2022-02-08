package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'whitespace'.
 */
public class IWhitespace extends XNonterminal {
    /**
     * Create an IWhitespace.
     *
     * @param parent The parent node.
     */
    public IWhitespace(XNode parent) {
        super(parent, "whitespace");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IWhitespace newnode = new IWhitespace(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
