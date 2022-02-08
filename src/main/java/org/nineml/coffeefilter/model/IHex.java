package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'hex'.
 */
public class IHex extends XNonterminal {
    /**
     * Create an IHex.
     *
     * @param parent The parent node.
     */
    public IHex(XNode parent) {
        super(parent, "hex");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IHex newnode = new IHex(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
