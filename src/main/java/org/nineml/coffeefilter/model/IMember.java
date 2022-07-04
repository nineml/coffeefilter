package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.util.TokenUtils;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.tokens.CharacterSet;

/**
 * Model an Invisible XML 'member'.
 */
public class IMember extends XNonterminal {
    protected CharacterSet charset = null;

    /**
     * Create an IMember.
     *
     * @param parent The parent node.
     */
    public IMember(XNode parent) {
        super(parent, "member");
    }

    protected void setString(String string) {
        if (charset != null) {
            throw new IllegalArgumentException("Cannot set string after a member has been initialized");
        }
        charset = CharacterSet.literal(string);
    }

    protected void setHex(String hex) {
        if (charset != null) {
            throw new IllegalArgumentException("Cannot set hex after a member has been initialized");
        }
        int cp = TokenUtils.convertHex(hex);
        charset = CharacterSet.range(cp, cp);
    }

    protected void setCode(String code) {
        if (charset != null) {
            throw new IllegalArgumentException("Cannot set hex after a member has been initialized");
        }
        try {
            charset = CharacterSet.unicodeClass(code);
        } catch (GrammarException ex) {
            if ("E002".equals(ex.getCode())) {
                // Map the CoffeeGrinder exception into an IxmlException for consistency
                throw IxmlException.invalidCharacterClass(code);
            }
            throw ex;
        }
    }

    protected void setRange(String from, String to) {
        if (charset != null) {
            throw new IllegalArgumentException("Cannot set range after a member has been initialized");
        }

        if (from == null) {
            throw new NullPointerException("No 'from' attribute on range?");
        }

        // Hex values arrive here as '#' followed by digits
        if (from.length() > 1 && from.startsWith("#")) {
            int cp = TokenUtils.convertHex(from.substring(1));
            StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(cp);
            from = sb.toString();
        }

        if (from.codePointCount(0, from.length()) != 1) {
            throw new IllegalArgumentException("Range 'from' must be a single character");
        }

        int fromcp = from.codePointAt(0);

        if (to == null) {
            throw new NullPointerException("No 'to' attribute on range?");
        }

        // Hex values arrive here as '#' followed by digits
        if (to.length() > 1 && to.startsWith("#")) {
            int cp = TokenUtils.convertHex(to.substring(1));
            StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(cp);
            to = sb.toString();
        }

        if (to.codePointCount(0, to.length()) != 1) {
            throw new IllegalArgumentException("Range 'to' must be a single character");
        }

        int tocp = to.codePointAt(0);

        if (fromcp > tocp) {
            throw IxmlException.invalidRange(from, to);
        }

        charset = CharacterSet.range(fromcp, tocp);
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IMember newnode = new IMember(parent);
        newnode.optional = optional;
        newnode.charset = charset;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    @Override
    public String toString() {
        return charset.toString();
    }
}
