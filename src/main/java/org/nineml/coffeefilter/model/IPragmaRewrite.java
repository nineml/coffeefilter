package org.nineml.coffeefilter.model;

public class IPragmaRewrite extends IPragma {
    public IPragmaRewrite(XNode parent, String data) {
        super(parent, "nineml");
        pragmaData = data;
    }
}
