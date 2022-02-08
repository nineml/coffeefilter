package org.nineml.coffeefilter.model;

import org.xml.sax.Attributes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The abstract class that is the supertype of all nodes in the Ixml model.
 */
public abstract class XNode {
    protected final String nodeName;
    protected String name = null;
    protected XNode parent;
    protected ArrayList<XNode> children;
    protected boolean optional = false;

    /**
     * The base class constructor.
     * @param parent The parent.
     * @param nodeName The node name, that is, the name of the ixml node type (alt, alts, ...)
     */
    protected XNode(XNode parent, String nodeName) {
        if (nodeName == null) {
            throw new NullPointerException("Node name cannot be null");
        }
        this.parent = parent;
        this.nodeName = nodeName;
        children = new ArrayList<>();
    }

    /**
     * Convenience constructor that sets the parent to null.
     * @param nodeName The node name, that is, the name of the ixml node type (alt, alts, ...)
     */
    public XNode(String nodeName) {
        this(null, nodeName);
    }

    /**
     * Return the name for this node.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this node is optional.
     * @return True iff the node is optional.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * Creates a new child node from the name and attributes specified.
     *
     * <p>This method is used by the XML parser to construct a model of an ixml grammar
     * from its XML representation.</p>
     *
     * <p>The constructed node is returned, but it is also added to the children of the node
     * on which this method is called.</p>
     *
     * @param name The name of the node.
     * @param attributes Its attributes.
     * @return The node constructed.
     */
    public XNode createChild(String name, Attributes attributes) {
        XNode child = null;
        String tmark = null;
        String mark = null;

        switch (name) {
            case "alt":
                child = new IAlt(this);
                break;
            case "alts":
                child = new IAlts(this);
                break;
            case "capital":
                child = new ICapital(this);
                break;
            case "cchar":
                child = new ICChar(this);
                break;
            case "character":
                child = new ICharacter(this);
                break;
            case "charset":
                child = new ICharset(this);
                break;
            case "class":
                child = new IClass(this, attributes.getValue("code"));
                break;
            case "code":
                child = new ICode(this);
                break;
            case "comment":
                child = new IComment(this);
                break;
            case "cr":
                child = new ICr(this);
                break;
            case "dchar":
                child = new IDChar(this);
                break;
            case "dstring":
                child = new IDString(this);
                break;
            case "encoded":
                tmark = attributes.getValue("tmark");
                if (tmark == null) {
                    child = new IEncoded(this, '^');
                } else {
                    if (tmark.length() != 1) {
                        throw new IllegalArgumentException("tmark attribute must be a single character");
                    }
                    child = new IEncoded(this, tmark.charAt(0));
                }
                break;
            case "exclusion":
                tmark = attributes.getValue("tmark");
                if (tmark == null) {
                    child = new IExclusion(this, '^');
                } else {
                    if (tmark.length() != 1) {
                        throw new IllegalArgumentException("tmark attribute must be a single character");
                    }
                    child = new IExclusion(this, tmark.charAt(0));
                }
                break;
            case "factor":
                child = new IFactor(this);
                break;
            case "from":
                child = new IFrom(this);
                break;
            case "hex":
                child = new IHex(this);
                break;
            case "inclusion":
                tmark = attributes.getValue("tmark");
                if (tmark == null) {
                    child = new IInclusion(this, '^');
                } else {
                    if (tmark.length() != 1) {
                        throw new IllegalArgumentException("tmark attribute must be a single character");
                    }
                    child = new IInclusion(this, tmark.charAt(0));
                }
                break;
            case "letter":
                child = new ILetter(this);
                break;
            case "lf":
                child = new ILf(this);
                break;
            case "literal":
                String str = attributes.getValue("string");
                if (str == null) {
                    str = attributes.getValue("dstring");
                }
                if (str == null) {
                    str = attributes.getValue("sstring");
                }
                tmark = attributes.getValue("tmark");
                if (tmark == null) {
                    child = new ILiteral(this, '^', str, attributes.getValue("hex"));
                } else {
                    if (tmark.length() != 1) {
                        throw new IllegalArgumentException("tmark attribute must be a single character");
                    }
                    child = new ILiteral(this, tmark.charAt(0), str, attributes.getValue("hex"));
                }
                break;
            case "mark":
                child = new IMark(this);
                break;
            case "member":
                child = new IMember(this);
                break;
            case "name":
                child = new IName(this);
                break;
            case "namefollower":
                child = new INamefollower(this);
                break;
            case "namestart":
                child = new INamestart(this);
                break;
            case "nonterminal":
                mark = attributes.getValue("mark");
                if (mark == null) {
                    child = new INonterminal(this, attributes.getValue("name"));
                } else {
                    if (mark.length() != 1) {
                        throw new IllegalArgumentException("mark attribute must be a single character");
                    }
                    child = new INonterminal(this, attributes.getValue("name"), mark.charAt(0));
                }
                break;
            case "option":
                child = new IOption(this);
                break;
            case "quoted":
                tmark = attributes.getValue("tmark");
                if (tmark == null) {
                    child = new IQuoted(this, '^');
                } else {
                    if (tmark.length() != 1) {
                        throw new IllegalArgumentException("tmark attribute must be a single character");
                    }
                    child = new IQuoted(this, tmark.charAt(0));
                }
                break;
            case "range":
                child = new IRange(this, attributes.getValue("from"), attributes.getValue("to"));
                break;
            case "repeat0":
                child = new IRepeat0(this);
                break;
            case "repeat1":
                child = new IRepeat1(this);
                break;
            case "rule":
                mark = attributes.getValue("mark");
                if (mark == null) {
                    child = new IRule(this, attributes.getValue("name"),'^');
                } else {
                    if (mark.length() != 1) {
                        throw new IllegalArgumentException("mark attribute must be a single character");
                    }
                    child = new IRule(this, attributes.getValue("name"), mark.charAt(0));
                }
                break;
            case "s":
                child = new IS(this);
                break;
            case "schar":
                child = new ISChar(this);
                break;
            case "sep":
                child = new ISep(this);
                break;
            case "set":
                child = new ISet(this);
                break;
            case "sstring":
                child = new ISString(this);
                break;
            case "string":
                child = new IString(this);
                break;
            case "tab":
                child = new ITab(this);
                break;
            case "term":
                child = new ITerm(this);
                break;
            case "terminal":
                child = new ITerminal(this);
                break;
            case "tmark":
                child = new ITMark(this);
                break;
            case "to":
                child = new ITo(this);
                break;
            case "whitespace":
                child = new IWhitespace(this);
                break;
            default:
                throw new IllegalArgumentException("Unexpected token name: " + name);
        }
        children.add(child);
        return child;
    }

    /**
     * Add a copy of the specified node to this node's children.
     *
     * <p>The constructed node is returned, but it is also added to the children of the node
     * on which this method is called.</p>
     *
     * @param child The child to copy.
     * @return The new node.
     */
    protected XNode addCopy(XNode child) {
        XNode copy = child.copy();
        copy.optional = child.optional;
        copy.parent = this;
        children.add(copy);
        return copy;
    }

    /**
     * Return a "shallow" copy of this node.
     *
     * <p>The returned node will have all of the properties of the current node except that
     * it will have no children.</p>
     *
     * @return The copied node.
     */
    protected XNode shallowCopy() {
        XNode copy = copy();
        copy.children.clear(); // FIXME: shallowCopy() method?
        copy.optional = optional;
        copy.parent = null;
        return copy;
    }

    /**
     * Copy the current node and its descendants.
     *
     * <p>The parent of the copy is often the same as the original, but
     * this is a bit spurious as it'll get changed when the copy is inserted
     * as a child of some other node.</p>
     *
     * @return A copy of the node.
     */
    protected abstract XNode copy();

    /**
     * Copy the specified list of children to this node.
     *
     * <p>Each node in the copychildren list will be copied and added to this node as a child.</p>
     *
     * @param copychildren The children.
     */
    protected void copyChildren(List<XNode> copychildren) {
        children.clear();
        for (XNode child : copychildren) {
            addCopy(child.copy());
        }
    }

    /**
     * Return the parent node.
     * @return The parent node or null if the current node is the root node.
     */
    public XNode getParent() {
        return parent;
    }

    /**
     * Return the node's name.
     * @return The node name.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Return the root node.
     * @return The root node.
     */
    public Ixml getRoot() {
        XNode cur = this;
        while (cur.getParent() != null) {
            cur = cur.getParent();
        }
        return (Ixml) cur;
    }

    /**
     * Return the children.
     * @return This node's children.
     */
    public List<XNode> getChildren() {
        return children;
    }

    /**
     * Returns true if this node has a child with the specified name.
     * @param name The node name.
     * @return True if a (direct) child with that name exists.
     */
    public boolean hasChild(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        for (XNode child : children) {
            if (name.equals(child.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strip comments from this node and its descendants.
     *
     * <p>This method updates the children of this node and potentially its descendants.</p>
     */
    public void stripComments() {
        boolean found = false;
        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IComment) {
                found = true;
            } else {
                child.stripComments();
                newchildren.add(child);
            }
        }
        if (found) {
            children = newchildren;
        }
    }

    public void flatten() {
        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child : children) {
            if (!(child instanceof IComment)) {
                child.flatten();
                newchildren.add(child);
            }
        }
        children.clear();
        children.addAll(newchildren);
        newchildren.clear();

        boolean changed = false;
        for (XNode child : children) {
            if (child instanceof IAlts) {
                if (child.children.isEmpty()) {
                    // Drop an empty set of alternatives
                } else {
                    if (child.children.size() == 1 || !(child.children.get(0) instanceof IAlt)) {
                        newchildren.addAll(child.children);
                        changed = true;
                    } else {
                        newchildren.add(child);
                    }
                }
            } else {
                newchildren.add(child);
            }
        }

        if (newchildren.size() == 1 && newchildren.get(0) instanceof IAlt) {
            newchildren = newchildren.get(0).children;
            changed = true;
        }

        if (changed) {
            children.clear();
            for (XNode child : newchildren) {
                addCopy(child);
            }
        }
    }

    protected void replaceAlternatives() {
        for (XNode child : children) {
            child.replaceAlternatives();
        }

        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child : children) {
            if (child instanceof IAlts) {
                Ixml root = getRoot();

                if (child.children.isEmpty()) {
                    String altname = "$empty";
                    INonterminal altnt = new INonterminal(this, altname, '-');
                    newchildren.add(altnt);
                    if (!root.emptyProduction) {
                        IRule altrule = new IRule(root, altname, '-');
                        root.addRule(altrule);
                        root.emptyProduction = true;
                    }
                } else {
                    String altname = "|" + root.nextRuleName();
                    INonterminal altnt = new INonterminal(this, altname, '-');
                    newchildren.add(altnt);

                    for (XNode alt : child.children) {
                        IRule altrule = new IRule(root, altname, '-');
                        for (XNode gchild : alt.children) {
                            altrule.addCopy(gchild);
                        }
                        root.addRule(altrule);
                    }
                }
            } else {
                newchildren.add(child);
            }
        }
        children = newchildren;
    }

    /**
     * Rewrite repeat0 constructs.
     *
     * <p>This method replaces all instances of repeat0 in this node and its descendants with
     * a rewriting that does not use repeat0. The children are both mutated on the object and returned
     * for convience.</p>
     *
     * @return The (possibly updated) children.
     */
    protected ArrayList<XNode> simplifyRepeat0() {
        if (hasChild("repeat0")) {
            ArrayList<XNode> newchildren = new ArrayList<>();
            for (XNode child : children) {
                child.children = child.simplifyRepeat0();
                if (child instanceof IRepeat0) {
                    Ixml root = getRoot();

                    XNode sep = child.children.get(child.children.size() - 1);
                    if (sep instanceof ISep) { // f*sep
                        ArrayList<XNode> factor = new ArrayList<>();
                        for (XNode f : child.children) {
                            if (f != sep) {
                                factor.add(f);
                            }
                        }

                        // We need four new nonterminals
                        String f_star_sep_name = root.nextRuleName();
                        INonterminal f_star_sep = new INonterminal(child, f_star_sep_name, '-');

                        String f_plus_sep_name = root.nextRuleName();
                        INonterminal f_plus_sep = new INonterminal(child, f_plus_sep_name, '-');

                        String f_star_name = root.nextRuleName();
                        INonterminal f_star = new INonterminal(child, f_star_name, '-');

                        String f_star_prime_name = root.nextRuleName();
                        INonterminal f_star_prime = new INonterminal(child, f_star_prime_name, '-');

                        // f*sep = f-star-sep
                        newchildren.add(f_star_sep);

                        // f-star-sep = f-plus-sep?
                        IRule f_star_sep_rule = new IRule(root, f_star_sep_name, '-');
                        XNode copy = f_star_sep_rule.addCopy(f_plus_sep);
                        copy.optional = true;

                        // f-plus-sep = f, f-star
                        IRule f_plus_sep_rule = new IRule(root, f_plus_sep_name, '-');
                        for (XNode f : factor) {
                            f_plus_sep_rule.addCopy(f);
                        }
                        f_plus_sep_rule.addCopy(f_star);

                        // f-star => f-star-prime?
                        IRule f_star_rule = new IRule(root, f_star_name, '-');
                        copy = f_star_rule.addCopy(f_star_prime);
                        copy.optional = true;

                        // f-star-prime => sep, f, f-star
                        IRule f_star_prime_rule = new IRule(root, f_star_prime_name, '-');
                        f_star_prime_rule.addCopy(sep);
                        for (XNode f : factor) {
                            f_star_prime_rule.addCopy(f);
                        }
                        f_star_prime_rule.addCopy(f_star);

                        root.addRule(f_star_sep_rule);
                        root.addRule(f_plus_sep_rule);
                        root.addRule(f_star_rule);
                        root.addRule(f_star_prime_rule);

                        /*
                        ArrayList<Node> factor = new ArrayList<>();
                        for (Node f : child.children) {
                            if (f != sep) {
                                factor.add(f);
                            }
                        }

                        String f_star_sep_name = root.nextRuleName();
                        INonterminal f_star_sep = new INonterminal(child, f_star_sep_name, '-');

                        String f_plus_sep_name = root.nextRuleName();
                        INonterminal f_plus_sep = new INonterminal(child, f_plus_sep_name, '-');
                        f_plus_sep.optional = true;

                        String sep_plus_f_star_name = root.nextRuleName();
                        INonterminal sep_plus_f_star = new INonterminal(child, sep_plus_f_star_name, '-');

                        String sep_plus_f_name = root.nextRuleName();
                        INonterminal sep_plus_f = new INonterminal(child, sep_plus_f_name, '-');
                        sep_plus_f.optional = true;

                        // f*sep = f-star-sep
                        newchildren.add(f_star_sep);

                        // f-star-sep = f-plus-sep?
                        IRule f_star_sep_rule = new IRule(root, f_star_sep_name, '-');
                        f_star_sep_rule.addCopy(f_plus_sep);

                        // f-plus-sep = f, sep-plus-f-star
                        IRule f_plus_sep_rule = new IRule(root, f_plus_sep_name, '-');
                        for (Node f : factor) {
                            f_plus_sep_rule.addCopy(f);
                        }
                        f_plus_sep_rule.addCopy(sep_plus_f_star);

                        // sep-plus-f-star = sep-plus-f?
                        IRule sep_plus_f_star_rule = new IRule(root, sep_plus_f_star_name, '-');
                        sep_plus_f_star_rule.addCopy(sep_plus_f);

                        // sep-plus-f = sep, f, sep-plus-f?
                        IRule sep_plus_f_rule = new IRule(root, sep_plus_f_name, '-');
                        sep_plus_f_rule.addCopy(sep);
                        for (Node f : factor) {
                            sep_plus_f_rule.addCopy(f);
                        }
                        sep_plus_f_rule.addCopy(sep_plus_f);

                        root.addRule(f_star_sep_rule);
                        root.addRule(f_plus_sep_rule);
                        root.addRule(sep_plus_f_star_rule);
                        root.addRule(sep_plus_f_rule);

                         */
                    } else { // f*
                        String f_star_name = root.nextRuleName();
                        INonterminal f_star = new INonterminal(child, f_star_name, '-');
                        f_star.optional = true; // XXX

                        String f_plus_name = root.nextRuleName();
                        INonterminal f_plus = new INonterminal(child, f_plus_name, '-');
                        f_plus.optional = true;

                        // f* => f-star
                        newchildren.add(f_star);

                        // -f-star => f-plus?
                        IRule f_star_rule = new IRule(root, f_star_name, '-');
                        f_star_rule.addCopy(f_plus);

                        // -f-plus => f, f-plus?
                        IRule f_plus_rule = new IRule(root, f_plus_name, '-');
                        for (XNode f : child.children) {
                            f_plus_rule.addCopy(f);
                        }
                        f_plus_rule.addCopy(f_plus);

                        root.addRule(f_star_rule);
                        root.addRule(f_plus_rule);
                    }
                } else {
                    newchildren.add(child);
                }
            }
            return newchildren;
        } else {
            for (XNode child : children) {
                child.children = child.simplifyRepeat0();
            }
        }

        return children;
    }

    /**
     * Rewrite repeat1 constructs.
     *
     * <p>This method replaces all instances of repeat1 in this node and its descendants with
     * a rewriting that does not use repeat1. The children are both mutated on the object and returned
     * for convience.</p>
     *
     * @return The (possibly updated) children.
     */
    protected ArrayList<XNode> simplifyRepeat1() {
        if (hasChild("repeat1")) {
            ArrayList<XNode> newchildren = new ArrayList<>();
            for (XNode child : children) {
                child.children = child.simplifyRepeat1();
                if (child instanceof IRepeat1) {
                    Ixml root = getRoot();

                    XNode sep = child.children.get(child.children.size() - 1);
                    if (sep instanceof ISep) { // f+sep
                        ArrayList<XNode> factor = new ArrayList<>();
                        for (XNode f : child.children) {
                            if (f != sep) {
                                factor.add(f);
                            }
                        }

                        String f_plus_sep_name = root.nextRuleName();
                        INonterminal f_plus_sep = new INonterminal(f_plus_sep_name);

                        String sep_plus_f_star_name = root.nextRuleName();
                        INonterminal sep_plus_f_star = new INonterminal(sep_plus_f_star_name);
                        sep_plus_f_star.optional = true;

                        String sep_plus_f_name = root.nextRuleName();
                        INonterminal sep_plus_f = new INonterminal(sep_plus_f_name);
                        sep_plus_f.optional = true;

                        // f+sep => f-plus-sep
                        newchildren.add(f_plus_sep);

                        // f-plus-sep = f, sep-plus-f-star
                        IRule f_plus_sep_rule = new IRule(root, f_plus_sep_name, '-');
                        for (XNode f : factor) {
                            f_plus_sep_rule.addCopy(f);
                        }
                        f_plus_sep_rule.addCopy(sep_plus_f_star);

                        // sep-plus-f-star = sep-plus-f?
                        IRule sep_plus_f_star_rule = new IRule(root, sep_plus_f_star_name, '-');
                        sep_plus_f_star_rule.addCopy(sep_plus_f);

                        // sep-plus-f = sep, f, sep-plus-f?
                        IRule sep_plus_f_rule = new IRule(root, sep_plus_f_name, '-');
                        sep_plus_f_rule.addCopy(sep);
                        for (XNode f : factor) {
                            sep_plus_f_rule.addCopy(f);
                        }
                        sep_plus_f_rule.addCopy(sep_plus_f);

                        root.addRule(f_plus_sep_rule);
                        root.addRule(sep_plus_f_star_rule);
                        root.addRule(sep_plus_f_rule);
                    } else { // f+
                        String f_plus_name = root.nextRuleName();
                        INonterminal f_plus = new INonterminal(child, f_plus_name, '-');

                        String f_star_name = root.nextRuleName();
                        INonterminal f_star = new INonterminal(child, f_star_name, '-');

                        String f_star_prime_name = root.nextRuleName();
                        INonterminal f_star_prime = new INonterminal(child, f_star_prime_name, '-');

                        // f+ => f-plus
                        newchildren.add(f_plus);

                        // f-plus => f, f-star
                        IRule f_plus_rule = new IRule(root, f_plus_name, '-');
                        for (XNode f : child.children) {
                            f_plus_rule.addCopy(f);
                        }
                        f_plus_rule.addCopy(f_star);

                        // f-star => f-star-prime?
                        IRule f_star_rule = new IRule(root, f_star_name, '-');
                        XNode copy = f_star_rule.addCopy(f_star_prime);
                        copy.optional = true;

                        // f-star-prime => f, f-star
                        IRule f_star_prime_rule = new IRule(root, f_star_prime_name, '-');
                        for (XNode f : child.children) {
                            f_star_prime_rule.addCopy(f);
                        }
                        f_star_prime_rule.addCopy(f_star);

                        root.addRule(f_plus_rule);
                        root.addRule(f_star_rule);
                        root.addRule(f_star_prime_rule);
                    }
                } else {
                    newchildren.add(child);
                }
            }
            return newchildren;
        } else {
            for (XNode child : children) {
                child.children = child.simplifyRepeat1();
            }
        }

        return children;
    }

    protected void trimAlts() {
        for (XNode child : children) {
            child.trimAlts();
        }
        ArrayList<XNode> newchildren = new ArrayList<>();
        boolean changed = false;
        for (XNode child : children) {
            if (child instanceof IAlts) {
                if (child.children.size() == 1 || !(child.children.get(0) instanceof IAlt)) {
                    newchildren.addAll(child.children);
                    changed = true;
                } else {
                    newchildren.add(child);
                }
            } else {
                newchildren.add(child);
            }
        }

        if (newchildren.size() == 1 && newchildren.get(0) instanceof IAlt) {
            newchildren = newchildren.get(0).children;
            changed = true;
        }

        if (changed) {
            children.clear();
            for (XNode child : newchildren) {
                addCopy(child);
            }
        }
    }

    protected void trimOptional() {
        boolean changed = false;
        ArrayList<XNode> newchildren = new ArrayList<>();
        for (XNode child: children) {
            child.trimOptional();
            if (child instanceof IOption && child.children.size() == 1) {
                changed = true;
                XNode opt = child.children.get(0);
                opt.optional = true;
                newchildren.add(opt);
            } else if (child instanceof ISep) {
                changed = true;
                newchildren.addAll(child.children);
            } else {
                newchildren.add(child);
            }
        }

        if (changed) {
            children.clear();
            for (XNode child : newchildren) {
                addCopy(child);
            }
        }
    }

    protected ArrayList<XNode> copyAlternatives() {
        ArrayList<XNode> trees = new ArrayList<>();
        if (children.isEmpty()) {
            trees.add(shallowCopy());
        } else {
            if (children.get(0) instanceof IAlt) {
                for (XNode child : children) {
                    ArrayList<ArrayList<XNode>> forest = new ArrayList<>();
                    forest.add(child.copyAlternatives());
                    trees.addAll(makeTrees(new ArrayList<>(), forest, 0));
                }
            } else {
                ArrayList<ArrayList<XNode>> forest = new ArrayList<>();
                for (XNode child : children) {
                    forest.add(child.copyAlternatives());
                }
                trees.addAll(makeTrees(new ArrayList<>(), forest, 0));
            }
        }
        return trees;
    }

    private ArrayList<XNode> makeTrees(ArrayList<XNode> copyChildren, ArrayList<ArrayList<XNode>> forest, int pos) {
        ArrayList<XNode> trees = new ArrayList<>();
        if (pos == forest.size()) {
            XNode newsource = shallowCopy();
            for (XNode child : copyChildren) {
                newsource.addCopy(child);
            }
            trees.add(newsource);
        } else {
            for (XNode node : forest.get(pos)) {
                copyChildren.add(node);
                trees.addAll(makeTrees(copyChildren, forest, pos+1));
                copyChildren.remove(copyChildren.size()-1);
            }
        }
        return trees;
    }

    /**
     * Format a crude XML dump of the model on the specified stream.
     * @param stream The stream to which the model should be written.
     * @param indent The current indent.
     */
    protected void dump(PrintStream stream, String indent) {
        stream.print(indent);
        stream.print("<" + nodeName);
        dumpBody(stream, indent);
    }

    protected void dumpBody(PrintStream stream, String indent) {
        if (name != null) {
            final StringBuilder xml = new StringBuilder();
            getName().codePoints().forEach(cp -> {
                if (cp < ' ') {
                    xml.append(String.format("&#x%x;", cp));
                } else {
                    xml.appendCodePoint(cp);
                }
            });
            stream.print(" name='" + xmlAttr(xml.toString(), "'") + "'");
        }
        stream.print(" optional='" + optional + "'");
        if (getChildren().isEmpty()) {
            stream.println("/>");
        } else {
            stream.println(">");
            for (XNode child : getChildren()) {
                child.dump(stream, indent + "  ");
            }
            stream.print(indent);
            stream.println("</" + nodeName + ">");
        }
    }

    protected String xmlAttr(String value, String quote) {
        String esc = value.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
        if ("'".equals(quote)) {
            return esc.replaceAll("'", "&apos;");
        } else {
            return esc.replaceAll("\"", "&quot;");
        }
    }
}
