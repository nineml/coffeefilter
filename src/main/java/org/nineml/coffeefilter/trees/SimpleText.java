package org.nineml.coffeefilter.trees;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nineml.coffeefilter.trees.TreeUtils.floatRegex;
import static org.nineml.coffeefilter.trees.TreeUtils.intRegex;
import static org.nineml.coffeefilter.trees.TreeUtils.maxInt;
import static org.nineml.coffeefilter.trees.TreeUtils.minInt;

/**
 * A node in a {@link SimpleTree} that contains a single text value.
 */
public class SimpleText extends SimpleTree {
    private final String text;

    protected SimpleText(SimpleTree parent, String text) {
        super(parent, null);
        this.text = text;
    }

    /**
     * Get the value.
     * @return the value.
     */
    @Override
    public String getText() {
        return text;
    }

    @Override
    public String asXML() {
        return TreeUtils.xmlEscape(text);
    }

    @Override
    public String asJSON() {
        return TreeUtils.jsonValue(text);
    }
}
