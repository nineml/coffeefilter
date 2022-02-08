package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'tmark'.
 */
public class ITMark extends XNonterminal {
    /**
     * Create an ITMark.
     *
     * @param parent The parent node.
     */
    public ITMark(XNode parent) {
        super(parent, "tmark");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ITMark newnode = new ITMark(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
