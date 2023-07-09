package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.exceptions.IxmlException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Model an Invisible XML 'rule'.
 */
public class IRule extends XNonterminal {
    protected final char mark;
    protected final String rename;

    /**
     * Create an IRule.
     *
     * @param parent The parent node.
     * @param name The rule name.
     * @param mark The mark.
     * @throws IllegalArgumentException if the mark is invalid.
     */
    public IRule(XNode parent, String name, char mark) {
        this(parent, name, null, mark);
    }

    /**
     * Create an IRule.
     *
     * @param parent The parent node.
     * @param name The rule name.
     * @param rename The serialization name for the rule
     * @param mark The mark.
     * @throws IllegalArgumentException if the mark is invalid.
     */
    public IRule(XNode parent, String name, String rename, char mark) {
        super(parent, "rule", name);
        this.rename = rename;

        if (mark != '^' && mark != '@' && mark != '-') {
            throw IxmlException.invalidMark(mark);
        }

        this.mark = mark;
    }

    /** The name to use when serializing this nonterminal
     *
     * @return the serialization name
     */
    public String getRename() {
        String newName = null;
        for (IPragma pragma : getPragmas()) {
            if (pragma instanceof IPragmaRename) {
                if (newName != null) {
                    throw IxmlException.repeatedPragma("rename", newName, pragma.getPragmaData());
                }
                newName = pragma.getPragmaData();
            }
        }

        if (newName != null) {
            return newName;
        }

        if (rename != null) {
            Ixml ixml = getRoot();
            if (!"1.1-nineml".equals(ixml.getIxmlVersion())) {
                throw IxmlException.renameUnavailable(name, rename);
            }
        }

        return rename;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IRule newnode = new IRule(parent, name, rename, mark);
        newnode.optional = optional;
        newnode.copyChildren(getChildren());
        return newnode;
    }

    /**
     * Return the mark.
     * @return The mark.
     */
    public char getMark() {
        return mark;
    }

    protected void splitAlternatives() {
        ArrayList<XNode> alternatives = copyAlternatives();
        for (XNode root : alternatives) {
            getRoot().addRule((IRule) root);
        }
    }

    protected ArrayList<XNode> flattenNonterminals() {
        Ixml root = getRoot();
        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child : children) {
            if (child.children.isEmpty() || (child instanceof IInclusion) | (child instanceof IExclusion)) {
                newchildren.add(child);
            } else {
                String newname = root.nextRuleName(child.getNodeName());
                INonterminal newnt = new INonterminal(parent, newname, '-');
                newnt.derivedFrom = child;
                newchildren.add(newnt);

                newnt.optional = child.optional;

                IRule rule;
                rule = new IRule(root, newname, '-');
                rule.derivedFrom = child;
                rule.optional = newnt.optional;

                for (XNode grandchild : child.children) {
                    rule.addCopy(grandchild);
                }
                root.addRule(rule);
            }
        }

        return newchildren;
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
        stream.print(" mark='" + mark + "'");
        dumpBody(stream, indent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mark);
        sb.append(name);
        if (optional) {
            sb.append("?");
        }
        sb.append(" => ");
        String sep = "";
        for (XNode child : children) {
            sb.append(sep);
            sb.append(child.toString());
            sep = ", ";
        }
        return sb.toString();
    }
}
