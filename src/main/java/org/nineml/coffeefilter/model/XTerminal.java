package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;

import java.util.List;

/**
 * An abstract class representing terminal nodes.
 */
public abstract class XTerminal extends XNode {
    protected Token token = null;

    /**
     * Construct a terminal.
     * @param parent The parent.
     * @param nodeName The node name, that is, the name of the ixml node type (class, literal, ...)
     */
    public XTerminal(XNode parent, String nodeName) {
        super(parent, nodeName);
        optional = false;
    }

    /**
     * Return the token that matches this terminal.
     * <p>The default is to match the underlying character sets (strings, ranges, classes, etc.)
     * as an inclusion.</p>
     * @return the token.
     */
    public Token getToken()  {
        if (token == null) {
            token = TokenCharacterSet.inclusion(getCharacterSets());
        }
        return token;
    }

    /**
     * Return the {@link CharacterSet}(s) that represents this class.
     * @return The character set.
     */
    public abstract List<CharacterSet> getCharacterSets();
}
