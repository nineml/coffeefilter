package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataText;
import org.nineml.coffeefilter.trees.DataTree;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.xml.sax.SAXException;

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

    @Test
    public void jsonSerializerAtomicNull() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "null");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("null", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerUnsignedInteger() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "3");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("3", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerPosInteger() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "+3");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("3", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerNegInteger() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "-3");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("-3", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerUnsignedFloat() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "3.14");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("3.14", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerPosFloat() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "+3.14");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("3.14", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerNegFloat() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "-3.14");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("-3.14", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerAtomicTrue() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "true");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("true", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerAtomicFalse() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "false");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("false", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializerAtomicString() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();
            text(builder, "test");
            builder.endDocument();
            DataTree tree = builder.getTree();
            String value = tree.asJSON();
            Assert.assertEquals("\"test\"", value);
        } catch (SAXException ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializer() {
        try {
            DataTreeBuilder builder = new DataTreeBuilder();
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);
            builder.endElement("", "root", "root");

            atomic(builder, "bool", "true");
            atomic(builder, "int", "17");
            atomic(builder, "float", "3.4");
            atomic(builder, "float", "+3.14");
            atomic(builder, "float", "-2.7");
            atomic(builder, "null", "null");
            atomic(builder, "big", "+9007199254740991");
            atomic(builder, "toobig", "+9007199254740995");

            builder.startElement("", "wrapper", "wrapper", AttributeBuilder.EMPTY_ATTRIBUTES);
            atomic(builder, "item", "3");
            builder.startElement("", "s", "s", AttributeBuilder.EMPTY_ATTRIBUTES);
            atomic(builder, "item", "3");
            builder.endElement("", "s", "s");
            atomic(builder, "item", "4");
            atomic(builder, "other-item", "test");
            atomic(builder, "item", "false");
            builder.endElement("", "wrapper", "wrapper");

            builder.endDocument();
            DataTree tree = builder.getTree();

            String json = tree.asJSON();

            Assert.assertEquals("{\"root\":{\"test\":\"spoon\"},\"bool\":true,\"int\":17,\"float\":[3.4,3.14,-2.7],\"null\":null,\"big\":9007199254740991,\"toobig\":\"+9007199254740995\",\"wrapper\":{\"item\":[3,4,false],\"s\":{\"item\":3},\"other-item\":\"test\"}}",
                    json);
        } catch (Exception ex) {
            fail();
        }
    }

    private void atomic(DataTreeBuilder builder, String name, String text) throws SAXException {
        builder.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        builder.characters(text.toCharArray(), 0, text.length());
        builder.endElement("", name, name);
    }

    private void text(DataTreeBuilder builder, String text) throws SAXException {
        builder.characters(text.toCharArray(), 0, text.length());
    }

}
