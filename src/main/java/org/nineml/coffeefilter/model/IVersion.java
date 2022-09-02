package org.nineml.coffeefilter.model;

public class IVersion extends XNonterminal {
    protected String versionString = null;

    public IVersion(XNode parent, String version) {
        super(parent, "version", "version");
        versionString = version;
    }

    protected void setVersion(String data) {
        versionString = data;
    }

    protected String getVersion() {
        return versionString;
    }

    @Override
    protected XNode copy() {
        IVersion version = new IVersion(parent, name);
        version.versionString = versionString;
        version.copyChildren(getChildren());
        return version;
    }

    @Override
    public String toString() {
        return "ixml version '" + versionString + "'.";
    }
}
