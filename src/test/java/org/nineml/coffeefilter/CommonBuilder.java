package org.nineml.coffeefilter;

import org.nineml.coffeefilter.trees.DataTree;
import org.nineml.coffeefilter.trees.DataTreeBuilder;
import org.nineml.coffeefilter.trees.SimpleTreeBuilder;
import org.nineml.coffeefilter.utils.AttributeBuilder;
import org.xml.sax.SAXException;

public class CommonBuilder {

    public DataTree buildRecordTree() throws SAXException {
        DataTreeBuilder builder = new DataTreeBuilder();
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
        return builder.getTree();
    }

    protected void atomic(DataTreeBuilder builder, String name, String text) throws SAXException {
        builder.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        builder.characters(text.toCharArray(), 0, text.length());
        builder.endElement("", name, name);
    }

    protected void text(DataTreeBuilder builder, String text) throws SAXException {
        builder.characters(text.toCharArray(), 0, text.length());
    }

    protected void atomic(SimpleTreeBuilder builder, String name, String text) throws SAXException {
        builder.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        builder.characters(text.toCharArray(), 0, text.length());
        builder.endElement("", name, name);
    }

    protected void text(SimpleTreeBuilder builder, String text) throws SAXException {
        builder.characters(text.toCharArray(), 0, text.length());
    }
}
