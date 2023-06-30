package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.exceptions.IxmlTreeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

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
     * This means you can <code>.get()</code> chain past it, although no value will
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
        throw IxmlTreeException.noMixedContent();
    }

    /**
     * Return an XML representation of the tree.
     * @return the serialized xml tree.
     */
    public String asXML() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append("<").append(name);
            if (children.isEmpty()) {
                sb.append("/>");
                return sb.toString();
            }
            sb.append(">");
        }
        for (DataTree child : children) {
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
        if (children.isEmpty()) {
            return "null";
        }

        if (children.get(0) instanceof DataText) {
            return children.get(0).asJSON();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        HashMap<String,Integer> nameCount = new HashMap<>();
        for (DataTree child : children) {
            if (!nameCount.containsKey(child.name)) {
                nameCount.put(child.name, 0);
            }
            nameCount.put(child.name, nameCount.get(child.name) + 1);
        }

        String osep = "";
        for (DataTree child : children) {
            if (!nameCount.containsKey(child.name)) {
                continue; // already dealt with these
            }
            sb.append(osep);
            sb.append("\"").append(child.name).append("\":");

            if (nameCount.get(child.name) == 1) {
                sb.append(child.asJSON());
            } else {
                sb.append("[");
                String asep = "";
                for (DataTree elem : children) {
                    if (child.name.equals(elem.name)) {
                        sb.append(asep);
                        sb.append(elem.asJSON());
                        asep = ",";
                    }
                }
                sb.append("]");
            }
            osep = ",";
            nameCount.remove(child.name);
        }
        sb.append("}");

        return sb.toString();
    }

    public List<CsvColumn> prepareCsv() {
        if (parent != null) {
            return null;
        }

        if (children.size() != 1) {
            return null;
        }

        if (children.get(0) instanceof DataText) {
            return null;
        }

        ArrayList<String> names = new ArrayList<>();
        HashMap<String, CsvColumn> columns = new HashMap<>();
        for (DataTree row : children.get(0).children) {
            if (!row.children.isEmpty()) {
                for (DataTree col : row.children) {
                    if (col instanceof DataText) {
                        return null;
                    }
                    if (col.children.size() > 1) {
                        return null;
                    }
                    if (!col.children.isEmpty()) {
                        if (!(col.children.get(0) instanceof DataText)) {
                            return null;
                        }
                    }

                    if (!columns.containsKey(col.name)) {
                        names.add(col.name);
                        columns.put(col.name, new CsvColumn(col.name));
                    }
                    checkColumn(columns.get(col.name), col.getValue());
                }
            }
        }

        ArrayList<CsvColumn> columnList = new ArrayList<>();
        for (String name : names) {
            columnList.add(columns.get(name));
        }

        return columnList;
    }

    public String asCSV(List<CsvColumn> columns) {
        return asCSV(columns, false);
    }

    public String asCSV(List<CsvColumn> columns, boolean omitHeaders) {
        StringBuilder sb = new StringBuilder();

        if (parent != null) {
            throw IxmlTreeException.noCsv();
        }

        if (children.size() != 1) {
            throw IxmlTreeException.noCsv();
        }

        if (children.get(0) instanceof DataText) {
            throw IxmlTreeException.noCsv();
        }

        if (!omitHeaders) {
            String sep = "";
            for (CsvColumn col : columns) {
                sb.append(sep);
                sb.append("\"").append(TreeUtils.csvEscape(col.header)).append("\"");
                sep = ",";
            }
            sb.append('\n');
        }

        HashMap<String,String> values = new HashMap<>();
        for (DataTree row : children.get(0).children) {
            values.clear();
            if (!row.children.isEmpty()) {
                for (DataTree col : row.children) {
                    if (col instanceof DataText) {
                        throw IxmlTreeException.noCsv();
                    }
                    if (col.children.size() > 1) {
                        throw IxmlTreeException.noCsv();
                    }
                    if (!col.children.isEmpty()) {
                        if (!(col.children.get(0) instanceof DataText)) {
                            throw IxmlTreeException.noCsv();
                        }
                    }

                    values.put(col.name, col.getValue());
                }
            }

            String sep = "";
            for (CsvColumn col : columns) {
                sb.append(sep);
                if (values.containsKey(col.name)) {
                    if ("string".equals(col.datatype)) {
                        sb.append("\"").append(TreeUtils.csvEscape(values.get(col.name))).append("\"");
                    } else {
                        sb.append(TreeUtils.csvEscape(values.get(col.name)));
                    }
                }
                sep = ",";
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    private void checkColumn(CsvColumn column, String value) {
        if ("string".equals(column.datatype)) {
            return;
        }

        if ("true".equals(value) || "false".equals(value)) {
            if (column.datatype == null) {
                column.datatype = "boolean";
            }
            if (!"boolean".equals(column.datatype)) {
                column.datatype = "string";
            }
        }

        if ("null".equals(value)) {
            if (column.datatype == null) {
                column.datatype = "null";
            }
            if (!"null".equals(column.datatype)) {
                column.datatype = "string";
            }
        }

        Matcher match = TreeUtils.intRegex.matcher(value);
        if (match.matches()) {
            if (column.datatype == null) {
                column.datatype = "integer";
            }
            if (!"integer".equals(column.datatype)) {
                column.datatype = "string";
            }
            return;
        }

        match = TreeUtils.floatRegex.matcher(value);
        if (match.matches()) {
            if (column.datatype == null) {
                column.datatype = "float";
            }
            if (!"float".equals(column.datatype)) {
                column.datatype = "string";
            }
        }

        if (column.datatype == null) {
            column.datatype = "string";
        }
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
