package org.nineml.coffeefilter.model;

public class IPragmaRewrite extends IPragma {
    public IPragmaRewrite(XNode parent, String name, String data) {
        super(parent, name);
        pragmaData = data;
    }
}
