package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'terminal'.
 */
public class ITerminal extends XNonterminal {
    /**
     * Create an ITerminal.
     *
     * @param parent The parent node.
     */
    public ITerminal(XNode parent) {
        super(parent, "terminal");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ITerminal newnode = new ITerminal(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
