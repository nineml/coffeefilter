package org.nineml.coffeefilter.model;

import java.util.ArrayList;

public abstract class RuleRewriter {
    protected Ixml root = null;

    public void setRoot(Ixml ixml) {
        if (root != null && root != ixml) {
            throw new IllegalStateException("Cannot change the rewriter root");
        }
        root = ixml;
    }

    public abstract RuleRewrites[] rewriteOrder();
    public abstract ArrayList<XNode> rewriteRepeat0Sep(IRepeat0 node);
    public abstract ArrayList<XNode> rewriteRepeat1Sep(IRepeat1 node);
    public abstract ArrayList<XNode> rewriteRepeat1(IRepeat1 node);
    public abstract ArrayList<XNode> rewriteRepeat0(IRepeat0 node);
    public abstract ArrayList<XNode> rewriteOption(IOption node);

}
