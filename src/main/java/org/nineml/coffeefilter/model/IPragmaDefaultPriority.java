package org.nineml.coffeefilter.model;

public class IPragmaDefaultPriority extends IPragma {
    public IPragmaDefaultPriority(XNode parent, String priority) {
        super(parent, "nineml");
        pragmaData = priority;
    }
}
