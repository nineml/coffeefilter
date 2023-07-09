package org.nineml.coffeefilter.model;

public class IPragmaRegex extends IPragma {
    public IPragmaRegex(XNode parent, String name, String data) {
        super(parent, name);
        pragmaData = data;
        ptype = PragmaType.REGEX;
    }
}
