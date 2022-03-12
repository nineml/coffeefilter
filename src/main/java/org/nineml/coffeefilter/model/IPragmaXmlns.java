package org.nineml.coffeefilter.model;

public class IPragmaXmlns extends IPragma {
    public IPragmaXmlns(XNode parent, String data) {
        super(parent, "nineml");
        pragmaData = data;
    }
}
