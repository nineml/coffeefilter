package org.nineml.coffeefilter.model;

import java.util.ArrayList;

public class IProlog extends XNonterminal {

    public IProlog(XNode parent) {
        super(parent, "prolog");
    }

    @Override
    protected XNode copy() {
        IProlog prolog = new IProlog(parent);
        return prolog;
    }
}
