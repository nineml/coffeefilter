package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'code'.
 */
public class ICode extends XNonterminal {
    /**
     * Create an ICode.
     *
     * @param parent The parent node.
     */
    public ICode(XNode parent) {
        super(parent, "code");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ICode newnode = new ICode(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
