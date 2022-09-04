package org.nineml.coffeefilter.model;

public class IPragmaData extends XNonterminal {
    private StringBuilder pragmaData = new StringBuilder();

    public IPragmaData(XNode parent) {
        super(parent, "pragmadata", "pragmadata");
    }

    /**
     * Add characters to the pragma data.
     * @param chars the characters.
     */
    public void addCharacters(String chars) {
        pragmaData.append(chars);
    }

    /** Get the pragma data
     * @return the pragma data
     */
    public String getPragmaData() {
        return pragmaData.toString();
    }

    @Override
    protected XNode copy() {
        IPragmaData prolog = new IPragmaData(parent);
        prolog.pragmaData.append(pragmaData);
        prolog.copyChildren(getChildren());
        return prolog;
    }
}
