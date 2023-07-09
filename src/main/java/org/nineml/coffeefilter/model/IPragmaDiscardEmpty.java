package org.nineml.coffeefilter.model;

public class IPragmaDiscardEmpty extends IPragma {
    public IPragmaDiscardEmpty(XNode parent, String name, String data) {
        super(parent, name);
        pragmaData = data;
        ptype = PragmaType.DISCARD_EMPTY;
    }
}
