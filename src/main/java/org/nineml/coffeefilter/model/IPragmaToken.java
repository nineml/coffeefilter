package org.nineml.coffeefilter.model;

public class IPragmaToken extends IPragma {
    public IPragmaToken(XNode parent, String name, String data) {
        super(parent, name);
        pragmaData = data;
    }
}
