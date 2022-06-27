package org.nineml.coffeefilter.model;

public class IPragmaPriority extends IPragma {
    public IPragmaPriority(XNode parent, String name, String priority) {
        super(parent, name);
        pragmaData = priority;
    }
}
