package org.nineml.coffeefilter.model;

import java.util.HashMap;

public class IPragma extends XNonterminal {
    protected enum PragmaType { UNDEFINED, DISCARD_EMPTY, METADATA, PRIORITY, REGEX, RENAME, TOKEN, XMLNS, CSV_HEADING };
    protected static final HashMap<PragmaType, String> pragmaTypeNames = new HashMap<>();
    static {
        pragmaTypeNames.put(PragmaType.UNDEFINED, "undefined");
        pragmaTypeNames.put(PragmaType.DISCARD_EMPTY, "discard empty");
        pragmaTypeNames.put(PragmaType.METADATA, "metadata");
        pragmaTypeNames.put(PragmaType.PRIORITY, "priority");
        pragmaTypeNames.put(PragmaType.REGEX, "regex");
        pragmaTypeNames.put(PragmaType.RENAME, "rename");
        pragmaTypeNames.put(PragmaType.XMLNS, "xmlns");
        //pragmaTypeNames.put(PragmaType.CSV_HEADING, "csv heading");
    }

    protected String pragmaData = null;
    protected PragmaType ptype = PragmaType.UNDEFINED;
    protected boolean inherit = false;

    public IPragma(XNode parent, String name) {
        super(parent, "pragma", name);
    }

    protected void setPragmaData(String data) {
        pragmaData = data;
    }

    public String getPragmaData() {
        return pragmaData;
    }

    @Override
    protected XNode copy() {
        IPragma prolog = new IPragma(parent, name);
        prolog.pragmaData = pragmaData;
        prolog.copyChildren(getChildren());
        return prolog;
    }

    @Override
    public String toString() {
        if (pragmaData == null || "".equals(pragmaData)) {
            return "{[" + name + "]}";
        }
        return "{[" + name + " " + pragmaTypeNames.get(ptype) + " " + pragmaData + "]}";
    }
}
