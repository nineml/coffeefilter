package org.nineml.coffeefilter.trees;

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
}
