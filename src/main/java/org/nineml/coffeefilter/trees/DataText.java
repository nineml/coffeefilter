package org.nineml.coffeefilter.trees;

public class DataText extends DataTree {
    private final String text;

    protected DataText(DataTree parent, String text) {
        super(parent, null);
        this.text = text;
    }

    @Override
    public String getValue() {
        return text;
    }
}
