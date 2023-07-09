package org.nineml.coffeefilter.model;

public class IPragmaPriority extends IPragma {
    public IPragmaPriority(XNode parent, String name, String priority) {
        super(parent, name);
        pragmaData = priority;
        ptype = PragmaType.PRIORITY;
        inherit = true;

        try {
            int value = Integer.parseInt(priority);
            if (value < 0) {
                parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "Priority is negative: %s", priority);
            }
        } catch (NumberFormatException ex) {
            parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "Priority is not an integer: %s", priority);
        }
    }
}
