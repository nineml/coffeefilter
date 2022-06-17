package org.nineml.coffeefilter.model;

public class IPragmaPriority extends IPragma {
    public IPragmaPriority(XNode parent, String priority) {
        super(parent, "nineml");
        pragmaData = priority;
    }
}
