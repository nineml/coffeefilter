package org.nineml.coffeefilter.trees;

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
}
