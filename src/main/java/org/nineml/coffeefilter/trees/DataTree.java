package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.exceptions.IxmlTreeException;

import java.util.ArrayList;
import java.util.List;

/**
 * DataTree is data-oriented tree-like data structure.
 *
 * <p>Data trees are easily constructed
 * from an Invisible XML document. Data trees do not support mixed content.
 * Each node in the tree is either an atomic value or contains a list of atomic
 * values.</p>
 *
 * <p>Consider the following tree:</p>
 *
 * <pre>
 *                 +--------+
 *                 | config |
 *                 +--------+
 *                  /      \
 *                 /        \__________
 *                /                    \
 *          +---------+              +--------+
 *          | prop1   |              | prop2  |
 *          +---------+              +--------+
 *               |                    /      \
 *          +---------+       +--------+    +--------+
 *          | "value" |       | value1 |    | value2 |
 *          +---------+       +--------+    +--------+
 *                                 |             |
 *                            +--------+    +--------+
 *                            | "4"    |    | "true" |
 *                            +--------+    +--------+
 * </pre>
 *
 * <p>You could access the value "4" with the following chained methods:
 * <code>tree.get("config").get("prop2").get("value1").getValue()</code>.</p>
 * <p>The data tree knows nothing about namespaces, which don't occur in VXML
 * except in the special case of the <code>ixml:state</code> attribute. Any node
 * with a qualfied name simply has a name that contains a colon.</p>
 */
public class DataTree {
    public static final DataTree EMPTY_DATA_TREE = new DataTree();
    private final String name;
    private final ArrayList<DataTree> children;
    private final DataTree parent;

    protected DataTree() {
        this(null, null);
    }

    protected DataTree(DataTree parent, String name) {
        this.parent = parent;
        this.name = name;
        children = new ArrayList<>();
    }

    /**
     * Get this node's parent.
     * @return the parent, or null if this is the root.
     */
    public DataTree getParent() {
        return parent;
    }

    /**
     * Get the name of this node.
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a node named "name".
     * <p>If this node has no child named "name", an {@link #EMPTY_DATA_TREE} is returned.
     * This means you can <code>.get()</code> chain past it, although now value will
     * ever be found.</p>
     * <p>If there are several children with the requested name, one of them will be returned.
     * In practice the order of children may be consistent, but no consistency is guaranteed.</p>
     * @param name the name of the child
     * @return the child
     */
    public DataTree get(String name) {
        for (DataTree child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return EMPTY_DATA_TREE;
    }

    /**
     * Get all the nodes named "name".
     * <p>If this node has no child named "name", an empty list is returned.</p>
     * <p>In practice the order of children may be consistent, but no consistency is guaranteed.</p>
     * @param name the name of the child
     * @return the list of children
     */
    public List<DataTree> getAll(String name) {
        ArrayList<DataTree> matches = new ArrayList<>();
        for (DataTree child : children) {
            if (child.name.equals(name)) {
                matches.add(child);
            }
        }
        return matches;
    }

    /**
     * Get all the children of a node.
     * @return the list of children.
     */
    public List<DataTree> getAll() {
        return children;
    }

    /**
     * Get the value of a node
     * <p>If a node has no children, its value is the empty string. If a node has one or more
     * tree children, its value is null. (If a node has a single {@link DataText} child, its value
     * is the value of that child.</p>
     * @return the value.
     */
    public String getValue() {
        if (children.isEmpty()) {
            return "";
        }
        if (children.get(0) instanceof DataText) {
            return children.get(0).getValue();
        }
        return null;
    }

    protected DataTree addChild(String name) {
        if (name == null) {
            throw new NullPointerException("Child name must not be null");
        }
        DataTree node = new DataTree(this, name);
        children.add(node);
        return node;
    }

    protected void addText(String text) {
        if (children.isEmpty()) {
            DataText node = new DataText(this, text);
            children.add(node);
            return;
        }
        throw new IxmlTreeException("Cannot mix subtree and text nodes in a data tree");
    }

    @Override
    public String toString() {
        if (name == null) {
            return "<root>";
        }
        if (children.size() == 1 && children.get(0).getValue() != null) {
            return name + "=" + children.get(0).getValue();
        }
        return name + " (" + children.size() + ")";
    }

}
