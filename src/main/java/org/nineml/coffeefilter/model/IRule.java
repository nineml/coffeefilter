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

    /**
     * Create an IRule.
     *
     * @param parent The parent node.
     * @param name The rule name.
     * @param mark The mark.
     * @throws IllegalArgumentException if the mark is invalid.
     */
    public IRule(XNode parent, String name, char mark) {
        super(parent, "rule", name);

        if (mark != '^' && mark != '@' && mark != '-') {
            throw IxmlException.invalidMark(mark);
        }

        this.mark = mark;
    }

    /**
     * Copy the current node and its descendants.
     * @return A copy of the node.
     */
    @Override
    public XNode copy() {
        IRule newnode = new IRule(parent, name, mark);
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
                String newname = root.nextRuleName() + "_" + child.getNodeName();
                INonterminal newnt = new INonterminal(parent, newname, '-');
                newchildren.add(newnt);

                newnt.optional = child.optional;

                IRule rule;
                rule = new IRule(root, newname, '-');
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
