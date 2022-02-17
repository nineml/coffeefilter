package org.nineml.coffeefilter.trees;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nineml.coffeefilter.trees.TreeUtils.floatRegex;
import static org.nineml.coffeefilter.trees.TreeUtils.intRegex;
import static org.nineml.coffeefilter.trees.TreeUtils.maxInt;
import static org.nineml.coffeefilter.trees.TreeUtils.minInt;

/**
 * A node in a {@link DataTree} that contains a single atomic value.
 */
public class DataText extends DataTree {
    private final String text;

    protected DataText(DataTree parent, String text) {
        super(parent, null);
        this.text = text;
    }

    /**
     * Get the value.
     * @return the value.
     */
    @Override
    public String getValue() {
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
