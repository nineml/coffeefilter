package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'charset'.
 */
public class ICharset extends XNonterminal {
    /**
     * Create an ICharset.
     *
     * @param parent The parent node.
     */
    public ICharset(XNode parent) {
        super(parent, "charset");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ICharset newnode = new ICharset(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
