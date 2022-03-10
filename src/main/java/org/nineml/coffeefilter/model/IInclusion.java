package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.tokens.CharacterSet;

/**
 * Model an Invisible XML 'inclusion'.
 */
public class IInclusion extends Charset {
    /**
     * Create an IInclusion.
     *
     * @param parent The parent node.
     * @param tmark The tmark.
     */
    public IInclusion(XNode parent, char tmark) {
        super(parent, "inclusion", tmark);
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IInclusion newnode = new IInclusion(parent, tmark);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Get a string representation for this object.
     * @return A string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String sep = "";
        for (XNode child : children) {
            if (child instanceof XTerminal) {
                for (CharacterSet charset : ((XTerminal) child).getCharacterSets()) {
                    sb.append(sep);
                    sb.append(charset);
                    sep = "; ";
                }
            }
        }
        sb.append("]");
        if (optional) {
            sb.append("?");
        }
        return sb.toString();
    }
}
