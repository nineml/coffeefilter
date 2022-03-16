package org.nineml.coffeefilter.model;

public class IPragmaDiscardEmpty extends IPragma {
    public IPragmaDiscardEmpty(XNode parent, String data) {
        super(parent, "nineml");
        pragmaData = data;
    }
}
