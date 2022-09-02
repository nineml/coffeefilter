package org.nineml.coffeefilter.model;

public class IProlog extends XNonterminal {

    public IProlog(XNode parent) {
        super(parent, "prolog");
    }

    @Override
    protected XNode copy() {
        IProlog prolog = new IProlog(parent);
        prolog.copyChildren(getChildren());
        return prolog;
    }
}
