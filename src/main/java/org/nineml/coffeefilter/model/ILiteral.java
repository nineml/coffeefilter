package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.util.TokenUtils;
import org.nineml.coffeegrinder.tokens.CharacterSet;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Model an Invisible XML 'literal'.
 */
public class ILiteral extends XTerminal implements TMarked {
    protected CharacterSet charset = null;
    protected final char tmark;
    protected final String string;
    protected final String hex;

    /**
     * Create an ILiteral.
     *
     * <p>The ILiteral can represent either a string of characters or a hex character. That's
     * a distinction in the surface syntax of ixml, but doesn't matter to the implementation.</p>
     *
     * @param parent The parent node.
     * @param tmark The tmark.
     * @param string The string value of the literal.
     * @param hex The hex value of the literal.
     * @throws IllegalArgumentException if both 'string' and 'hex' are null or neither are null.
     * @throws IllegalArgumentException if tmark is invalid.
     */
    public ILiteral(XNode parent, char tmark, String string, String hex) {
        super(parent, "literal");
        if (string == null && hex == null) {
            throw new IllegalArgumentException("ILiteral needs either string or hex");
        }
        if (string != null && hex != null) {
            throw new IllegalArgumentException("ILiteral needs exactly one of string or hex");
        }
        if (tmark != '-' && tmark != '^') {
            throw IxmlException.invalidTMark(tmark);
        }

        this.tmark = tmark;
        this.string = string;
        this.hex = hex;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        ILiteral newnode = new ILiteral(parent, tmark, string, hex);
        newnode.pragmas.addAll(pragmas);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Return the name for this node.
     *
     * <p>The name is constructed from its literal value.</p>
     *
     * @return The name.
     */
    @Override
    public String getName() {
        if (name == null) {
            if (string != null) {
                name = string;
            } else {
                int cp = TokenUtils.convertHex(hex);
                StringBuilder sb = new StringBuilder();
                sb.appendCodePoint(cp);
                name = sb.toString();
            }
        }
        return name;
    }

    /**
     * Get the tmark.
     * @return The tmark.
     */
    public char getTMark() {
        return tmark;
    }

    /**
     * Get the string value.
     *
     * <p>If the literal was constructed with a hex value, this will return null.</p>
     *
     * @return The string.
     */
    public String getString() {
        return string;
    }

    /**
     * Get the hex value.
     *
     * <p>The hex value returned will be the code point that it represents, not the original hex string.</p>
     *
     * <p>If the literal was constructed with a string, this will return null.</p>
     *
     * @return The string represented by the hex value.
     */
    public String getHex() {
        return hex;
    }

    public String getTokenString() {
        if (string != null) {
            return string;
        }

        int cp = TokenUtils.convertHex(hex);
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(cp);
        return sb.substring(0, 1);
    }

    /**
     * Return the {@link CharacterSet} that represents this literal.
     *
     * <p>This method returns a list because a list is required in the general case.
     * For an <code>ILiteral</code>, only one character set is ever required.</p>
     *
     * @return The character set.
     */
    @Override
    public List<CharacterSet> getCharacterSets() {
        if (charset == null) {
            if (string != null) {
                charset = CharacterSet.literal(string);
            } else {
                int cp = TokenUtils.convertHex(hex);
                charset = CharacterSet.range(cp, cp);
            }
        }
        return Collections.singletonList(charset);
    }

    /**
     * Returns true iff the specified object is equal to this one.
     * @param obj the other object.
     * @return True iff they represent the same literal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ILiteral) {
            ILiteral literal = (ILiteral) obj;
            boolean same = (tmark == literal.tmark);
            same = same && (string == null && literal.string == null) || (string != null && literal.string != null);
            same = same && (hex == null && literal.hex == null) || (hex != null && literal.hex != null);
            if (!same) {
                return false;
            }

            same = tmark == literal.tmark;

            if (string != null) {
                same = same && string.equals(literal.string);
            }

            if (hex != null) {
                same = same && hex.equals(literal.hex);
            }

            return same;
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
        stream.print("<" + nodeName);

        stream.print(" tmark='" + tmark + "'");

        if (string != null) {
            final StringBuilder xmls = new StringBuilder();
            string.codePoints().forEach(cp -> {
                if (cp < ' ') {
                    xmls.append(String.format("&#x%x;", cp));
                } else {
                    xmls.appendCodePoint(cp);
                }
            });
            String s = xmls.toString();
            if (s.contains("'")) {
                stream.print(" dstring=\"" + xmlAttr(s, "\"") + "\"");

            } else {
                stream.print(" sstring='" + xmlAttr(s, "'") + "'");
            }
        }
        if (hex != null) {
            stream.print(" hex='" + hex + "'");
        }
        dumpBody(stream, indent);
    }

    /**
     * Get a string representation for this object.
     * @return A string.
     */
    @Override
    public String toString() {
        String str;
        if (hex != null) {
            str = "&0x" + hex + ";";
        } else {
            if (string.contains("\"")) {
                str = "'" + string.replaceAll("'", "''") + "'";
            } else {
                str = "\"" + string + "\"";
            }
        }
        if (optional) {
            str += "?";
        }
        return str;
    }
}
