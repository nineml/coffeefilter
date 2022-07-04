package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.util.TokenUtils;
import org.nineml.coffeegrinder.tokens.CharacterSet;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Model an Invisible XML 'range'.
 */
public class IRange extends XTerminal {
    protected CharacterSet charset = null;
    protected int from;
    protected int to;

    /**
     * Create an IRange.
     *
     * @param parent The parent node.
     * @param from The first codepoint.
     * @param to The last codepoint.
     * @throws NullPointerException if 'from' or 'to' are null.
     * @throws IllegalArgumentException if either 'from' or 'to' are not a single character.
     * @throws IllegalArgumentException if 'from' is greater than or equal to 'to'.
     */
    public IRange(XNode parent, String from, String to) {
        super(parent, "range");

        if (from == null) {
            throw new NullPointerException("No 'from' attribute on range?");
        }
        // FIXME: hex values arrive here as '#' followed by digits.
        // That's probably a bug in how the XML is constructed.
        if (from.length() > 1 && from.startsWith("#")) {
            int cp = TokenUtils.convertHex(from.substring(1));
            StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(cp);
            from = sb.toString();
        }
        if (from.codePointCount(0, from.length()) != 1) {
            throw new IllegalArgumentException("Range 'from' must be a single character");
        }
        this.from = from.codePointAt(0);

        if (to == null) {
            throw new NullPointerException("No 'to' attribute on range?");
        }
        // FIXME: hex values arrive here as '#' followed by digits.
        // That's probably a bug in how the XML is constructed.
        if (to.length() > 1 && to.startsWith("#")) {
            int cp = TokenUtils.convertHex(to.substring(1));
            StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(cp);
            to = sb.toString();
        }
        if (to.codePointCount(0, to.length()) != 1) {
            throw new IllegalArgumentException("Range 'to' must be a single character");
        }
        this.to = to.codePointAt(0);

        if (this.from > this.to) {
            throw IxmlException.invalidRange(from, to);
        }
    }

    /**
     * Create an IRange from integer codepoints.
     *
     * @param parent The parent node.
     * @param from The first codepoint.
     * @param to The last codepoint.
     * @throws IllegalArgumentException if either 'from' or 'to' are not a single character.
     * @throws IllegalArgumentException if 'from' is greater than or equal to 'to'.
     */
    protected IRange(XNode parent, int from, int to) {
        super(parent, "range");
        this.from = from;
        this.to = to;
        if (this.from > this.to) {
            throw IxmlException.invalidRange(""+from, ""+to);
        }
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IRange newnode = new IRange(parent, from, to);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Returns a name for this node.
     *
     * <p>Ranges don't have names in ixml, so a name will be manufactured.</p>
     *
     * @return The name.
     */
    @Override
    public String getName() {
        if (name == null) {
            name = getRoot().nextRuleName("_range_" + from + "_" + to);
        }
        return name;
    }

    /**
     * Return the from codepoint.
     * @return The from codepoint.
     */
    public int getFrom() {
        return from;
    }

    /**
     * Return the to codepoint.
     * @return The to codepoint.
     */
    public int getTo() {
        return to;
    }

    /**
     * Return the {@link CharacterSet} that represents this range.
     *
     * <p>This method returns a list because a list is required in the general case.
     * For an <code>IRange</code>, only one character set is ever required.</p>
     *
     * @return The character set.
     */
    @Override
    public List<CharacterSet> getCharacterSets() {
        if (charset == null) {
            charset = CharacterSet.range(from, to);
        }
        return Collections.singletonList(charset);
    }

    /**
     * Returns true iff the objects are equal.
     * @param obj The other object.
     * @return True if the other object represents the same range.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IRange) {
            IRange range = (IRange) obj;
            return (from == range.from) && (to == range.to);
        }
        return false;
    }

    /**
     * Format a crude XML dump of this node on the specified stream.
     * @param stream The stream to which the model should be written.
     * @param indent The current indent.
     */
    @Override
    protected void dump(PrintStream stream, String indent) {
        stream.print(indent);

        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(from);
        String fromch = sb.toString();
        if ("\"".equals(fromch)) {
            fromch = "&quot;";
        }

        sb = new StringBuilder();
        sb.appendCodePoint(to);
        String toch = sb.toString();
        if ("\"".equals(toch)) {
            toch = "&quot;";
        }

        stream.print("<" + nodeName + " from='" + fromch + "' to='" + toch + "'");
        dumpBody(stream, indent);
    }

    /**
     * Get a string representation for this object.
     * @return A string.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TR[");
        if (from < ' ') {
            sb.append(String.format("&#%02x;", from));
        } else {
            sb.appendCodePoint(from);
        }
        sb.append("-");
        if (to < ' ') {
            sb.append(String.format("&#%02x;", to));
        } else {
            sb.appendCodePoint(to);
        }
        sb.append("]");

        if (optional) {
            sb.append("?");
        }

        return sb.toString();
    }
}
