package org.nineml.coffeefilter.model;

public class IPragmaXmlns extends IPragma {
    public IPragmaXmlns(XNode parent, String name,String data) {
        super(parent, name);
        pragmaData = data;
        ptype = PragmaType.XMLNS;
    }
}
