package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'tab'.
 */
public class ITab extends XNonterminal {
    /**
     * Create an ITab.
     *
     * @param parent The parent node.
     */
    public ITab(XNode parent) {
        super(parent, "tab");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ITab newnode = new ITab(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
