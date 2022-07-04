package org.nineml.coffeefilter;

import org.nineml.coffeefilter.trees.DataTree;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.trees.SimpleTree;
import org.nineml.coffeefilter.trees.SimpleTreeBuilder;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CommonBuilder {

    public DataTree buildRecordDataTree(ParserOptions options) throws SAXException {
        DataTreeBuilder builder = new DataTreeBuilder(options);
        buildRecordTree(options, builder);
        return builder.getTree();
    }

    public SimpleTree buildRecordSimpleTree(ParserOptions options) throws SAXException {
        SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
        buildRecordTree(options, builder);
        return builder.getTree();
    }

    public void buildRecordTree(ParserOptions options, DefaultHandler builder) throws SAXException {
        builder.startDocument();

        builder.startElement("", "root", "root", AttributeBuilder.EMPTY_ATTRIBUTES);

        builder.startElement("", "record", "record", AttributeBuilder.EMPTY_ATTRIBUTES);
        atomic(builder, "name", "John Doe");
        atomic(builder, "age", "25");
        atomic(builder, "height", "1.7");
        atomic(builder, "bool", "true");
        builder.endElement("", "record", "record");

        builder.startElement("", "record", "record", AttributeBuilder.EMPTY_ATTRIBUTES);
        atomic(builder, "name", "Mary Smith");
        atomic(builder, "age", "22");
        atomic(builder, "bool", "false");
        builder.endElement("", "record", "record");

        builder.startElement("", "record", "record", AttributeBuilder.EMPTY_ATTRIBUTES);
        atomic(builder, "name", "Jane Doe");
        atomic(builder, "height", "1.4");
        atomic(builder, "age", "33");
        atomic(builder, "bool", "true");
        builder.endElement("", "record", "record");

        builder.endElement("", "root", "root");

        builder.endDocument();
    }

    public SimpleTree buildAttributesSimpleTree(ParserOptions options) throws SAXException {
        SimpleTreeBuilder builder = new SimpleTreeBuilder(options);
        buildAttributesTree(options, builder);
        return builder.getTree();
    }

    public void buildAttributesTree(ParserOptions options, DefaultHandler builder) throws SAXException {
        builder.startDocument();

        AttributeBuilder attrs = new AttributeBuilder(options);
        attrs.addAttribute("version", "1.0");
        attrs.addAttribute("count", "2");

        builder.startElement("", "root", "root", attrs);

        attrs = new AttributeBuilder(options);
        attrs.addAttribute("num", "1");

        builder.startElement("", "record", "record", attrs);
        atomic(builder, "name", "John Doe");
        atomic(builder, "age", "25");
        builder.endElement("", "record", "record");

        builder.startElement("", "record", "record", AttributeBuilder.EMPTY_ATTRIBUTES);
        atomic(builder, "name", "Mary Smith");
        atomic(builder, "age", "22");
        builder.endElement("", "record", "record");

        attrs = new AttributeBuilder(options);
        attrs.addAttribute("test", "string");

        builder.startElement("", "no-content", "no-content", attrs);
        builder.endElement("", "no-content", "no-content");

        builder.startElement("", "no-content-or-attr", "no-content-or-attr", AttributeBuilder.EMPTY_ATTRIBUTES);
        builder.endElement("", "no-content-or-attr", "no-content-or-attr");

        builder.endElement("", "root", "root");

        builder.endDocument();
    }

    protected void atomic(DefaultHandler builder, String name, String text) throws SAXException {
        builder.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        builder.characters(text.toCharArray(), 0, text.length());
        builder.endElement("", name, name);
    }

    protected void text(DefaultHandler builder, String text) throws SAXException {
        builder.characters(text.toCharArray(), 0, text.length());
    }
}
