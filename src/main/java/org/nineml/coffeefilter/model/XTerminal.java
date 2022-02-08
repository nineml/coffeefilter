package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;

import java.util.List;

/**
 * An abstract class representing terminal nodes.
 */
public abstract class XTerminal extends XNode {
    protected TerminalSymbol symbol = null;

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
     * Return the terminal symbol that matches this terminal.
     * @return The symbol.
     */
    public TerminalSymbol getTerminal() {
        if (symbol == null) {
            symbol = new TerminalSymbol(TokenCharacterSet.inclusion(getCharacterSets()));
        }
        return symbol;
    }

    /**
     * Return the {@link CharacterSet}(s) that represents this class.
     * @return The character set.
     */
    public abstract List<CharacterSet> getCharacterSets();
}
