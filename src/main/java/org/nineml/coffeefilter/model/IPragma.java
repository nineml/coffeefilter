package org.nineml.coffeefilter.model;

public class IPragma extends XNonterminal {
    protected String pragmaData = null;

    public IPragma(XNode parent, String name) {
        super(parent, "pragma", name);
    }

    protected void setPragmaData(String data) {
        pragmaData = data;
    }

    protected String getPragmaData() {
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
        return "{[" + name + " " + pragmaData + "]}";
    }
}
