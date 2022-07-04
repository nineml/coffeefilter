package org.nineml.coffeefilter;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeefilter.trees.SimpleText;
import org.nineml.coffeefilter.trees.SimpleTree;
import org.nineml.coffeefilter.trees.SimpleTreeBuilder;
import org.nineml.coffeefilter.util.AttributeBuilder;

import java.io.File;

import static junit.framework.TestCase.fail;

public class SimpleTreeTest extends CommonBuilder {
    private static ParserOptions options = new ParserOptions();
    private static InvisibleXml invisibleXml = new InvisibleXml(options);

    @Test
    public void emptyTree() {
        SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
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
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
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
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder(options);
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
            Assert.assertEquals(1, node.getChildren().size());

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
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder(options);
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
            Assert.assertEquals(1, node.getChildren().size());

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
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options,true);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder(options);
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
            Assert.assertEquals(0, node.getChildren().size());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testExceptions() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/exceptions.ixml"));

            String input = "set \"a\"\n" +
                    "set \"b\"\n" +
                    "    case \"c\" because d.\n";

            InvisibleXmlDocument doc = parser.parse(input);
            //doc.getEarleyResult().getForest().serialize("graph.xml");

            String xml = doc.getTree();

            Assert.assertNotNull(xml);

        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void xmlSerializer() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder(options);
            attrs.addAttribute("test", "spoon");
            attrs.addAttribute("test2", "fork");
            builder.startElement("", "root", "root", attrs);

            atomic(builder, "node1", "<test>");
            atomic(builder, "node2", "&other;");
            atomic(builder, "node2", "]]>");

            builder.endElement("", "root", "root");

            builder.endDocument();
            SimpleTree tree = builder.getTree();

            String xml = tree.asXML();

            String ok1 = "<root test2=\"fork\" test=\"spoon\"><node1>&lt;test&gt;</node1><node2>&amp;other;</node2><node2>]]&gt;</node2></root>";
            String ok2 = "<root test=\"spoon\" test2=\"fork\"><node1>&lt;test&gt;</node1><node2>&amp;other;</node2><node2>]]&gt;</node2></root>";

            Assert.assertTrue(ok1.equals(xml) || ok2.equals(xml));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void jsonSerializer() {
        try {
            SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
            builder.startDocument();

            AttributeBuilder attrs = new AttributeBuilder(options);
            attrs.addAttribute("test", "spoon");
            builder.startElement("", "root", "root", attrs);

            attrs = new AttributeBuilder(options);
            attrs.addAttribute("a", "1");
            attrs.addAttribute("b", "2");
            builder.startElement("", "attronly", "attronly", attrs);
            builder.endElement("", "attronly", "attronly");

            atomic(builder, "node1", "\"hello\"");
            atomic(builder, "node2", "c:\\path");
            atomic(builder, "node3", "/usr/local");

            builder.endElement("", "root", "root");

            builder.endDocument();
            SimpleTree tree = builder.getTree();

            String json = tree.asJSON();

            Assert.assertEquals("{\"content\":{\"name\":\"root\",\"attributes\":{\"test\":\"spoon\"},\"content\":[{\"name\":\"attronly\",\"attributes\":{\"a\":1,\"b\":2}},{\"name\":\"node1\",\"content\":\"\\\"hello\\\"\"},{\"name\":\"node2\",\"content\":\"c:\\\\path\"},{\"name\":\"node3\",\"content\":\"/usr/local\"}]}}",
                    json);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void jsonAttributes() {
        try {
            ParserOptions options = new ParserOptions();
            options.setAssertValidXmlNames(false);
            SimpleTree tree = buildAttributesSimpleTree(options);
            String str = tree.asJSON();
            Assert.assertEquals("{\"content\":{\"name\":\"root\",\"attributes\":{\"count\":2,\"version\":1.0},\"content\":[{\"name\":\"record\",\"attributes\":{\"num\":1},\"content\":[{\"name\":\"name\",\"content\":\"John Doe\"},{\"name\":\"age\",\"content\":25}]},{\"name\":\"record\",\"content\":[{\"name\":\"name\",\"content\":\"Mary Smith\"},{\"name\":\"age\",\"content\":22}]},{\"name\":\"no-content\",\"attributes\":{\"test\":\"string\"}},{\"name\":\"no-content-or-attr\"}]}}",
                    str);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void jsonRecords() {
        try {
            ParserOptions options = new ParserOptions();
            options.setAssertValidXmlNames(false);
            SimpleTree tree = buildRecordSimpleTree(options);

            String str = tree.asJSON();
            Assert.assertEquals("{\"content\":{\"name\":\"root\",\"content\":[{\"name\":\"record\",\"content\":[{\"name\":\"name\",\"content\":\"John Doe\"},{\"name\":\"age\",\"content\":25},{\"name\":\"height\",\"content\":1.7},{\"name\":\"bool\",\"content\":true}]},{\"name\":\"record\",\"content\":[{\"name\":\"name\",\"content\":\"Mary Smith\"},{\"name\":\"age\",\"content\":22},{\"name\":\"bool\",\"content\":false}]},{\"name\":\"record\",\"content\":[{\"name\":\"name\",\"content\":\"Jane Doe\"},{\"name\":\"height\",\"content\":1.4},{\"name\":\"age\",\"content\":33},{\"name\":\"bool\",\"content\":true}]}]}}",
                    str);
        } catch (Exception ex) {
            fail();
        }
    }

}
