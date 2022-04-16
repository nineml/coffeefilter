package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.Token;
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
            if (child instanceof IMember) {
                setlist.add(((IMember) child).charset);
            } else {
                throw new RuntimeException("Charset child is not a member: " + child);
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
