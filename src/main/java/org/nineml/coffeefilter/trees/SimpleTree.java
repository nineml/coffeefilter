package org.nineml.coffeefilter.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTree {
    private final String name;
    private final HashMap<String,String> attributes;
    private final ArrayList<SimpleTree> children;
    private final SimpleTree parent;

    protected SimpleTree() {
        this(null, null);
    }

    protected SimpleTree(SimpleTree parent, String name) {
        this.parent = parent;
        this.name = name;
        attributes = new HashMap<>();
        children = new ArrayList<>();
    }

    public SimpleTree getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public String getText() {
        return null;
    }

    public List<SimpleTree> getChildren() {
        return children;
    }

    protected void addAttribute(String name, String value) {
        attributes.put(name, value);
    }

    protected SimpleTree addChild(String name) {
        if (name == null) {
            throw new NullPointerException("Child name must not be null");
        }
        SimpleTree node = new SimpleTree(this, name);
        children.add(node);
        return node;
    }

    protected void addText(String text) {
        SimpleText node = new SimpleText(this, text);
        children.add(node);
    }
}
