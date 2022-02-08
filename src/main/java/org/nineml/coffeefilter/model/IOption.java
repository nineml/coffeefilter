package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'option'.
 */
public class IOption extends XNonterminal {
    /**
     * Create an IOption.
     *
     * @param parent The parent node.
     */
    public IOption(XNode parent) {
        super(parent, "option");
        optional = true;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IOption newnode = new IOption(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
