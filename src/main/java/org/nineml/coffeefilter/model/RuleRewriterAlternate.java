package org.nineml.coffeefilter.model;

import java.util.ArrayList;

public class RuleRewriterAlternate extends RuleRewriterSpec {
    @Override
    public RuleRewrites[] rewriteOrder() {
        return new RuleRewrites[] {
                RuleRewrites.REPEAT0SEP, RuleRewrites.REPEAT1SEP, RuleRewrites.REPEAT0, RuleRewrites.REPEAT1, RuleRewrites.OPTION
        };
    }

    @Override
    public ArrayList<XNode> rewriteRepeat0Sep(IRepeat0 node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.parent.getNewRuleName(node, "star-sep");
        INonterminal f_star_sep = new INonterminal(node.parent, name, '-');
        f_star_sep.derivedFrom = node;
        newchildren.add(f_star_sep);

        IRule f_star_sep_rule_empty = new IRule(root, name, '-');
        f_star_sep_rule_empty.derivedFrom = node;
        root.addRule(f_star_sep_rule_empty);

        IRule f_star_sep_rule_plus = new IRule(root, name, '-');
        f_star_sep_rule_plus.derivedFrom = node;
        IRepeat1 f_plus_sep = new IRepeat1(f_star_sep_rule_plus);
        for (XNode grandchild : node.children) {
            f_plus_sep.addCopy(grandchild);
        }
        f_star_sep_rule_plus.children.add(f_plus_sep);
        root.addRule(f_star_sep_rule_plus);

        return newchildren;
    }

    // rewriteRepeat1Sep is the same as the spec rules

    @Override
    public ArrayList<XNode> rewriteRepeat1(IRepeat1 node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.getNewRuleName(node, "plus");
        INonterminal f_plus = new INonterminal(node.parent, name, '-');
        f_plus.derivedFrom = node;
        newchildren.add(f_plus);

        IRule f_plus_rule = new IRule(root, name, '-');
        f_plus_rule.derivedFrom = node;
        for (int pos = 0; pos < node.children.size(); pos++) {
            f_plus_rule.addCopy(node.children.get(pos));
        }
        root.addRule(f_plus_rule);

        f_plus_rule = new IRule(root, name, '-');
        f_plus_rule.derivedFrom = node;
        for (int pos = 0; pos < node.children.size(); pos++) {
            f_plus_rule.addCopy(node.children.get(pos));
        }
        f_plus_rule.children.add(f_plus);

        root.addRule(f_plus_rule);

        return newchildren;
    }

    @Override
    public ArrayList<XNode> rewriteRepeat0(IRepeat0 node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.getNewRuleName(node, "star");
        INonterminal f_star = new INonterminal(node.parent, name, '-');
        f_star.derivedFrom = node;
        newchildren.add(f_star);

        IRule f_star_rule = new IRule(root, name, '-');
        f_star_rule.derivedFrom = node;
        root.addRule(f_star_rule);

        f_star_rule = new IRule(root, name, '-');
        f_star_rule.derivedFrom = node;
        IRepeat1 repeat = new IRepeat1(f_star_rule);
        f_star_rule.children.add(repeat);
        for (int pos = 0; pos < node.children.size(); pos++) {
            repeat.addCopy(node.children.get(pos));
        }

        root.addRule(f_star_rule);

        return newchildren;
    }

    // rewriteOption is the same as the spec rules
}
