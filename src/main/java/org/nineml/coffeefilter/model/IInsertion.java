package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.util.TokenUtils;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Model an Invisible XML 'literal'.
 */
public class IInsertion extends XNonterminal implements TMarked {
    protected final String string;
    protected final String hex;

    /**
     * Create an IInsertion.
     *
     * <p>Like ILiteral, the IInsertion can represent either a string of characters or a hex character. That's
     * a distinction in the surface syntax of ixml, but doesn't matter to the implementation.
     * Insertions are generated in the output but match nothing in the input.</p>
     *
     * @param parent The parent node.
     * @param string The string value of the literal.
     * @param hex The hex value of the literal.
     * @throws IllegalArgumentException if both 'string' and 'hex' are null or neither are null.
     */
    public IInsertion(XNode parent, String string, String hex) {
        super(parent, "insertion");
        if (string == null && hex == null) {
            throw new IllegalArgumentException("IInsertion needs either string or hex");
        }
        if (string != null && hex != null) {
            throw new IllegalArgumentException("IInsertion needs exactly one of string or hex");
        }
        this.string = string;
        this.hex = hex;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IInsertion newnode = new IInsertion(parent, string, hex);
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
     * Get the string value.
     *
     * <p>If the insertion was constructed with a hex value, this will return null.</p>
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
     * <p>If the insertion was constructed with a string, this will return null.</p>
     *
     * @return The string represented by the hex value.
     */
    public String getHex() {
        return hex;
    }

    @Override
    public char getTMark() {
        return '+';
    }

    public String getInsertion() {
        if (string != null) {
            return string;
        }

        int cp = TokenUtils.convertHex(hex);
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(cp);
        return sb.toString();
    }

    /**
     * Returns true iff the specified object is equal to this one.
     * @param obj the other object.
     * @return True iff they represent the same insertion.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IInsertion) {
            IInsertion other = (IInsertion) obj;
            return Objects.equals(string, other.string) && Objects.equals(hex, other.hex);
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

        if (string != null) {
            final StringBuilder xmls = new StringBuilder();
            string.codePoints().forEach(cp -> {
                if (cp < ' ') {
                    xmls.append(String.format("&#x%x;", cp));
                } else {
                    switch (cp) {
                        case '<':
                            xmls.append("&lt;");
                            break;
                        case '>':
                            xmls.append("&gt;");
                            break;
                        case '&':
                            xmls.append("&amp;");
                            break;
                        case '"':
                            xmls.append("&quot;");
                            break;
                        default:
                            xmls.appendCodePoint(cp);
                    }
                }
            });
            stream.printf(" string=\"%s\"", xmls.toString());
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
            str = "+&0x" + hex + ";";
        } else {
            if (string.contains("\"")) {
                str = "+'" + string.replaceAll("'", "''") + "'";
            } else {
                str = "+\"" + string + "\"";
            }
        }
        return str;
    }
}
