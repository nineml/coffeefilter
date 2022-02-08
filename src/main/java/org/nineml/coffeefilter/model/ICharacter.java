package org.nineml.coffeefilter.model;

/**
 * Model an Invisible XML 'character'.
 */
public class ICharacter extends XNonterminal {
    /**
     * Create an ICharacter.
     *
     * @param parent The parent node.
     */
    public ICharacter(XNode parent) {
        super(parent, "character");
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IAlt newnode = new IAlt(parent);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }
}
