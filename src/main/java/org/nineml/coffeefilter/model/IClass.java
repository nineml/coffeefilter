package org.nineml.coffeefilter.model;

import org.nineml.coffeegrinder.tokens.CharacterSet;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * Model an Invisible XML 'class'.
 */
public class IClass extends XTerminal {
    protected String code;
    protected CharacterSet charset = null;

    /**
     * Create an IClass.
     *
     * @param parent The parent node.
     * @param code The class code.
     * @throws NullPointerException if code is null.
     */
    public IClass(XNode parent, String code) {
        super(parent, "class");
        if (code == null) {
            throw new NullPointerException("IClass created with null code?");
        }

        this.code = code;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IClass newnode = new IClass(parent, code);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Returns a name for this node.
     *
     * <p>Classes don't have names in ixml, so a name will be manufactured.</p>
     *
     * @return The name.
     */
    @Override
    public String getName() {
        if (name == null) {
            name = getRoot().nextRuleName() + "_class_" + code;
        }
        return name;
    }

    /**
     * Returns the code for this class.
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Return the {@link CharacterSet} that represents this class.
     *
     * <p>This method returns a list because a list is required in the general case.
     * For an <code>IClass</code>, only one character set is ever required.</p>
     *
     * @return The character set.
     */
    @Override
    public List<CharacterSet> getCharacterSets() {
        if (charset == null) {
            charset = CharacterSet.unicodeClass(code);
        }
        return Collections.singletonList(charset);
    }

    /**
     * Returns true iff the two objects are equal.
     * @param obj The other object.
     * @return True if the other object represents the same class.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IClass) {
            IClass oclass = (IClass) obj;
            return code.equals(oclass.code);
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
        stream.print(" code='" + code + "'");
        dumpBody(stream, indent);
    }

    /**
     * Return a string representation of this node.
     * @return A string.
     */
    @Override
    public String toString() {
        String str = "TC[" + code + "]";
        if (optional) {
            str += "?";
        }
        return str;
    }
}
