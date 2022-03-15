package org.nineml.coffeefilter.model;

public class IPragmaRegex extends IPragma {
    public IPragmaRegex(XNode parent, String data) {
        super(parent, "nineml");
        pragmaData = data;
    }
}
