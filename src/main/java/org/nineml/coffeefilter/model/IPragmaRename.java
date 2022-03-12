package org.nineml.coffeefilter.model;

public class IPragmaRename extends IPragma {
    public IPragmaRename(XNode parent, String data) {
        super(parent, "nineml");
        pragmaData = data;
    }
}
