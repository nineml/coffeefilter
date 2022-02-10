package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.exceptions.IxmlException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public DataTree getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public DataTree get(String name) {
        for (DataTree child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return EMPTY_DATA_TREE;
    }

    public List<DataTree> getAll(String name) {
        ArrayList<DataTree> matches = new ArrayList<>();
        for (DataTree child : children) {
            if (child.name.equals(name)) {
                matches.add(child);
            }
        }
        return matches;
    }

    public List<DataTree> getAll() {
        return children;
    }

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
        throw new IxmlException("Cannot mix subtree and text nodes in a data tree");
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
