package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'member'.
 */
public class IMember extends XNonterminal {
    /**
     * Create an IMember.
     *
     * @param parent The parent node.
     */
    public IMember(XNode parent) {
        super(parent, "member");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IMember newnode = new IMember(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
