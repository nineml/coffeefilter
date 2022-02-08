package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'comment'.
 */
public class IComment extends XNonterminal {
    /**
     * Create an IComment.
     *
     * @param parent The parent node.
     */
    public IComment(XNode parent) {
        super(parent, "comment");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IComment newnode = new IComment(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
