package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'letter'.
 */
public class ILetter extends XNonterminal {
    /**
     * Create an ILetter.
     *
     * @param parent The parent node.
     */
    public ILetter(XNode parent) {
        super(parent, "letter");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ILetter newnode = new ILetter(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
