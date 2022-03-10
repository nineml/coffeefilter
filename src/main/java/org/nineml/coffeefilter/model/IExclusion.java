package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;

/**
 * Model an Invisible XML 'exclusion'.
 */
public class IExclusion extends Charset {
    /**
     * Create an IExclusion.
     *
     * @param parent The parent node.
     * @param tmark The tmark.
     */
    public IExclusion(XNode parent, char tmark) {
        super(parent, "exclusion", tmark);
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IExclusion newnode = new IExclusion(parent, tmark);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Return the token that matches this terminal.
     * <p>This time the token is an exclusion of the underlying character set.</p>
     * @return the token.
     */
    public Token getToken() {
        if (token == null) {
            token = TokenCharacterSet.exclusion(getCharacterSets());
        }
        return token;
    }

    /**
     * Get a string representation for this object.
     * @return A string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("~[");
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
