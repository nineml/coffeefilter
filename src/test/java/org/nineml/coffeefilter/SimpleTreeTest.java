package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.trees.SimpleText;
import org.nineml.coffeefilter.trees.SimpleTree;
import org.nineml.coffeefilter.trees.SimpleTreeBuilder;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.nineml.coffeegrinder.util.GrammarCompiler;

import java.io.File;

import static junit.framework.TestCase.fail;

public class SimpleTreeTest {
    @Test
    public void emptyTree() {
        SimpleTreeBuilder builder = new SimpleTreeBuilder();
        builder.startDocument();
        builder.endDocument();
        SimpleTree tree = builder.getTree();
        Assert.assertNotNull(tree);
        Assert.assertTrue(tree.getAttributes().isEmpty());
        Assert.assertTrue(tree.getChildren().isEmpty());
    }

    @Test
    public void treeWithTextNode() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder();
            builder.startDocument();
            builder.characters("abc".toCharArray(), 0, 3);
            builder.endDocument();
            SimpleTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertTrue(tree.getAttributes().isEmpty());
            Assert.assertEquals(1, tree.getChildren().size());
            Assert.assertTrue(tree.getChildren().get(0) instanceof SimpleText);
            Assert.assertEquals("abc", tree.getChildren().get(0).getText());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithNode() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder();
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);
            builder.characters("abc".toCharArray(), 0, 3);
            builder.endElement("", "root", "root");
            builder.endDocument();
            SimpleTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertTrue(tree.getAttributes().isEmpty());
            Assert.assertEquals(1, tree.getChildren().size());

            SimpleTree node = tree.getChildren().get(0);
            Assert.assertEquals(1,node.getChildren().size());

            SimpleTree textNode = node.getChildren().get(0);
            Assert.assertTrue(textNode instanceof SimpleText);
            Assert.assertEquals("abc", textNode.getText());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithWhitespace() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder();
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);
            builder.characters("   ".toCharArray(), 0, 3);
            builder.endElement("", "root", "root");
            builder.endDocument();
            SimpleTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertTrue(tree.getAttributes().isEmpty());
            Assert.assertEquals(1, tree.getChildren().size());

            SimpleTree node = tree.getChildren().get(0);
            Assert.assertEquals(1,node.getChildren().size());

            SimpleTree textNode = node.getChildren().get(0);
            Assert.assertTrue(textNode instanceof SimpleText);
            Assert.assertEquals("   ", textNode.getText());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void treeWithoutWhitespace() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder(true);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder();
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);
            builder.characters("   ".toCharArray(), 0, 3);
            builder.endElement("", "root", "root");
            builder.endDocument();
            SimpleTree tree = builder.getTree();
            Assert.assertNotNull(tree);
            Assert.assertTrue(tree.getAttributes().isEmpty());
            Assert.assertEquals(1, tree.getChildren().size());

            SimpleTree node = tree.getChildren().get(0);
            Assert.assertEquals(0,node.getChildren().size());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testExceptions() {
        try {
            InvisibleXmlParser parser = InvisibleXml.parserFromFile("src/test/resources/exceptions.ixml");

            String input = "set \"a\"\n" +
                    "set \"b\"\n" +
                    "    case \"c\" because d.\n";

            InvisibleXmlDocument doc = parser.parse(input);
            //doc.getEarleyResult().getForest().serialize("graph.xml");

            String xml = doc.getTree();

            Assert.assertNotNull(xml);

            System.err.println(xml);

        } catch (Exception ex) {
            fail();
        }
    }

}
