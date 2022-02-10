package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataText;
import org.nineml.coffeefilter.trees.DataTree;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.utils.AttributeBuilder;

import static junit.framework.TestCase.fail;

public class DataTreeTest {
    @Test
    public void emptyTree() {
        DataTreeBuilder builder = new DataTreeBuilder();
        builder.startDocument();
        builder.endDocument();
        DataTree tree = builder.getTree();
        Assert.assertNotNull(tree);
        Assert.assertTrue(tree.getAll().isEmpty());
    }

    @Test
    public void treeWithTextNode() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            builder.characters("abc".toCharArray(), 0, 3);
            builder.endDocument();
            DataTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertEquals(1, tree.getAll().size());
            Assert.assertEquals("abc", tree.getValue());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithNode() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);
            builder.endElement("", "root", "root");
            builder.endDocument();
            DataTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertEquals(1, tree.getAll().size());

            DataTree node = tree.get("root");
            Assert.assertNotNull(node);

            Assert.assertEquals(1,node.getAll().size());
            Assert.assertEquals("spoon", node.get("test").getValue());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithNodes() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("a", "A");
            attrs.addAttribute("b", "B");
            attrs.addAttribute("empty1", "      ");
            builder.startElement("", "root", "root", attrs);
            builder.characters("         ".toCharArray(), 0, 1);
            builder.startElement("", "c", "c", AttributeBuilder.EMPTY_ATTRIBUTES);
            builder.characters("C".toCharArray(), 0, 1);
            builder.endElement("", "c", "c");

            builder.startElement("", "empty2", "empty2", AttributeBuilder.EMPTY_ATTRIBUTES);
            builder.characters("\t     ".toCharArray(), 0, 1);
            builder.endElement("", "empty2", "empty2");

            builder.characters("\t\n".toCharArray(), 0, 1);
            builder.endElement("", "root", "root");
            builder.endDocument();
            DataTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertEquals(1, tree.getAll().size());

            DataTree node = tree.get("root");
            Assert.assertNotNull(node);

            Assert.assertEquals(5,node.getAll().size());

            Assert.assertEquals("C", node.get("c").getValue());
            Assert.assertEquals("B", node.get("b").getValue());
            Assert.assertEquals("A", node.get("a").getValue());
            Assert.assertEquals("", node.get("empty1").getValue());
            Assert.assertEquals("", node.get("empty2").getValue());
            Assert.assertEquals("", node.get("").getValue());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithDuplicateNodes() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder(true);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("a", "A");
            builder.startElement("", "root", "root", attrs);

            builder.startElement("", "a", "a", AttributeBuilder.EMPTY_ATTRIBUTES);
            builder.characters("B".toCharArray(), 0, 1);
            builder.endElement("", "c", "c");

            builder.startElement("", "c", "c", AttributeBuilder.EMPTY_ATTRIBUTES);
            builder.characters("C".toCharArray(), 0, 1);
            builder.endElement("", "c", "c");

            builder.endElement("", "root", "root");
            builder.endDocument();
            DataTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertEquals(1, tree.getAll().size());

            DataTree node = tree.get("root");
            Assert.assertNotNull(node);

            Assert.assertEquals(3,node.getAll().size());

            Assert.assertEquals("C", node.get("c").getValue());
            Assert.assertEquals("A", node.get("a").getValue());

            Assert.assertEquals(2, node.getAll("a").size());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithDuplicateNodesForbidden() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder(false);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("a", "A");
            builder.startElement("", "b", "b", attrs);
            builder.startElement("", "a", "a", AttributeBuilder.EMPTY_ATTRIBUTES);
            builder.endElement("", "a", "a");
            builder.endElement("", "b", "b");
            builder.endDocument();
            fail();
        } catch (IxmlException ex) {
            Assert.assertTrue(ex.getMessage().contains("Duplicate names forbidden"));
        } catch (Exception ex) {
            fail();
        }
    }
}
