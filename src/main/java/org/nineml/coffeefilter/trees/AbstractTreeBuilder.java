package org.nineml.coffeefilter.trees;

import org.nineml.coffeefilter.ParserOptions;
import org.xml.sax.helpers.DefaultHandler;

public abstract class AbstractTreeBuilder extends DefaultHandler {
    protected ParserOptions options;

    public AbstractTreeBuilder(ParserOptions options) {
        this.options = options;
    }
}
