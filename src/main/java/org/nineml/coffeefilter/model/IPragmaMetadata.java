package org.nineml.coffeefilter.model;

public class IPragmaMetadata extends IPragma {
    protected final String uri;
    public IPragmaMetadata(XNode parent, String uri, String data) {
        super(parent, "metadata");
        this.uri = uri;
        pragmaData = data;
        ptype = PragmaType.METADATA;
    }

    public String getPragmaURI() {
        return uri;
    }

    @Override
    public String toString() {
        if (pragmaData == null || "".equals(pragmaData)) {
            return "{[" + name + " " + uri + "]}";
        }
        return "{[" + name + " " + uri + " " + pragmaData + "]}";
    }
}
