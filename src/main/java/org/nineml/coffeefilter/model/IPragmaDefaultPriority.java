package org.nineml.coffeefilter.model;

public class IPragmaDefaultPriority extends IPragma {
    public IPragmaDefaultPriority(XNode parent, String name, String priority) {
        super(parent, name);
        pragmaData = priority;
    }
}
