package org.nineml.coffeefilter.trees;

public class SimpleText extends SimpleTree {
    private final String text;

    protected SimpleText(SimpleTree parent, String text) {
        super(parent, null);
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
}
