package org.nineml.coffeefilter.trees;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SimpleTree is a simple, generic representation of an InvisibleXmlDocument tree.
 * <p>Simple trees have elements, attributes, and text value content. The children
 * of a node containing mixed content are a sequence of {#link SimpleTree} and
 * {#link SimpleText} values.</p>
 * <p>The simple tree knows nothing about namespaces, which don't occur in VXML
 * except in the special case of the <code>ixml:state</code> attribute. Any node
 * with a qualfied name simply has a name that contains a colon.</p>
 */
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

    /**
     * Get this node's parent.
     * @return the parent, or null if this is the root.
     */
    public SimpleTree getParent() {
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
     * Get the node's attributes.
     * @return the node's attributes.
     */
    public Map<String,String> getAttributes() {
        return attributes;
    }

    /**
     * Get the value of a particular attribute.
     * @param name the attribute name
     * @return it's value, or null if no such attribute occurs on the element
     */
    public String getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Get the text value of a node.
     * @return the text value, or null if the node is not a text node.
     */
    public String getText() {
        return null;
    }

    /**
     * Get the node's children.
     * @return the children.
     */
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

    /**
     * Return an XML representation of the tree.
     * @return the serialized xml tree.
     */
    public String asXML() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append("<").append(name);

            for (String attname : attributes.keySet()) {
                sb.append(" ").append(attname).append("=\"");
                sb.append(TreeUtils.xmlEscapeAttribute(attributes.get(attname)));
                sb.append("\"");
            }

            if (children.isEmpty()) {
                sb.append("/>");
                return sb.toString();
            }
            sb.append(">");
        }
        for (SimpleTree child : children) {
            sb.append(child.asXML());
        }
        if (name != null) {
            sb.append("</").append(name).append(">");
        }
        return sb.toString();
    }

    /**
     * Return a JSON representation of the tree.
     * <p>All values in the tree are strings. When they are converted to JSON, the string values "true",
     * and "false" are promoted to boolean values; "null" is promoted to null. String values that match
     * integers between -2<sup>53</sup>+1 and 2<sup>53</sup>-1 are promoted to numbers. String values
     * that match floating point numbers are promoted to numbers.</p>
     * @return a serialized JSON representation.
     */
    public String asJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (name != null) {
            sb.append("\"name\":\"").append(name).append("\"");
            if (!attributes.isEmpty() || !children.isEmpty()) {
                sb.append(",");
            }
        }

        if (!attributes.isEmpty()) {
            sb.append("\"attributes\":{");
            String sep = "";
            for (String aname : attributes.keySet()) {
                sb.append(sep);
                sb.append("\"").append(TreeUtils.jsonEscape(aname)).append("\":");
                sb.append(TreeUtils.jsonValue(attributes.get(aname)));
                sep = ",";
            }
            sb.append("}");
            if (!children.isEmpty()) {
                sb.append(",");
            }
        }

        if (!children.isEmpty()) {
            if (children.size() == 1) {
                sb.append("\"content\":");
                sb.append(children.get(0).asJSON());
            } else {
                sb.append("\"content\":[");
                String osep = "";
                for (SimpleTree child : children) {
                    sb.append(osep);
                    sb.append(child.asJSON());
                    osep = ",";
                }
                sb.append("]");
            }
        }

        sb.append("}");

        return sb.toString();
    }
}
