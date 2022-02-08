package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class that represents a character set, either an inclusion or an exclusion.
 */
public abstract class Charset extends XTerminal implements TMarked {
    protected char tmark;

    /**
     * Charset models the underlying features of an inclusion or exclusion.
     *
     * @param parent The parent node in the grammar.
     * @param nodeName The name of the node.
     * @param tmark The node's tmark. The default is "^".
     */
    public Charset(XNode parent, String nodeName, char tmark) {
        super(parent, nodeName);
        this.tmark = tmark;
    }

    /**
     * Get the {@link CharacterSet}s in this inclusion or exclusion.
     * @return the list of character sets.
     */
    public List<CharacterSet> getCharacterSets() {
        ArrayList<CharacterSet> setlist = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof XTerminal) {
                setlist.addAll(((XTerminal) child).getCharacterSets());
            }
        }
        return setlist;
    }

    /**
     * Get this character set's tmark.
     * @return The tmark.
     */
    public char getTMark() {
        return tmark;
    }

    /**
     * Return the terminal symbol that matches this terminal.
     * @return The symbol.
     */
    @Override
    public TerminalSymbol getTerminal() {
        if (symbol == null) {
            ArrayList<ParserAttribute> attributes = new ArrayList<>();
            attributes.add(new ParserAttribute("tmark", ""+getTMark()));
            symbol = new TerminalSymbol(TokenCharacterSet.inclusion(getCharacterSets()), attributes);
        }
        return symbol;
    }

    /**
     * Format a crude XML dump of this node on the specified stream.
     * @param stream The stream to which the model should be written.
     * @param indent The current indent.
     */
    @Override
    protected void dump(PrintStream stream, String indent) {
        stream.print(indent);
        stream.print("<" + nodeName);
        stream.print(" tmark='" + tmark + "'");
        dumpBody(stream, indent);
    }

}
