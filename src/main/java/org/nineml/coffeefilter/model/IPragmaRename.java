package org.nineml.coffeefilter.model;

public class IPragmaRename extends IPragma {
    public IPragmaRename(XNode parent, String name, String data) {
        super(parent, name);
        pragmaData = data;
        ptype = PragmaType.RENAME;
        inherit = true;
    }
}
