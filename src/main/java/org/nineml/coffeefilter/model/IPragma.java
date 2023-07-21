package org.nineml.coffeefilter.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IPragma extends XNonterminal {
    public enum PragmaType { UNDEFINED, DISCARD_EMPTY, METADATA, PRIORITY, REGEX, RENAME, XMLNS, CSV_HEADING };
    public static final Map<PragmaType, String> pragmaTypeNames;
    static {
        HashMap<PragmaType, String> names = new HashMap<>();
        names.put(PragmaType.UNDEFINED, "undefined");
        names.put(PragmaType.DISCARD_EMPTY, "discard empty");
        names.put(PragmaType.METADATA, "metadata");
        names.put(PragmaType.PRIORITY, "priority");
        names.put(PragmaType.REGEX, "regex");
        names.put(PragmaType.RENAME, "rename");
        names.put(PragmaType.XMLNS, "xmlns");
        //names.put(PragmaType.CSV_HEADING, "csv heading");
        pragmaTypeNames = Collections.unmodifiableMap(names);
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
