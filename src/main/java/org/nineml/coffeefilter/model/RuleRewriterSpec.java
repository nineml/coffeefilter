package org.nineml.coffeefilter.model;

import java.util.ArrayList;

public class RuleRewriterSpec extends RuleRewriter {
    @Override
    public RuleRewrites[] rewriteOrder() {
        return new RuleRewrites[] {
                RuleRewrites.REPEAT0SEP, RuleRewrites.REPEAT1SEP, RuleRewrites.REPEAT1, RuleRewrites.REPEAT0, RuleRewrites.OPTION
        };
    }

    @Override
    public ArrayList<XNode> rewriteRepeat0Sep(IRepeat0 node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.parent.getNewRuleName(node, "star-sep-option");
        INonterminal f_star_sep_option = new INonterminal(node.parent, name, '-');
        f_star_sep_option.derivedFrom = node;

        newchildren.add(f_star_sep_option);

        IRule f_star_sep_option_rule = new IRule(root, name, '-');
        f_star_sep_option_rule.derivedFrom = node;

        root.addRule(f_star_sep_option_rule);

        f_star_sep_option_rule = new IRule(root, name, '-');
        f_star_sep_option_rule.derivedFrom = node;

        IRepeat1 f_plus_sep = new IRepeat1(f_star_sep_option_rule);
        for (XNode grandchild : node.children) {
            f_plus_sep.addCopy(grandchild);
        }
        f_star_sep_option_rule.children.add(f_plus_sep);
        root.addRule(f_star_sep_option_rule);

        return newchildren;
    }

    @Override
    public ArrayList<XNode> rewriteRepeat1Sep(IRepeat1 node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.getNewRuleName(node, "plus-sep");
        INonterminal f_plus_sep = new INonterminal(node.parent, name, '-');
        f_plus_sep.derivedFrom = node;

        newchildren.add(f_plus_sep);

        IRule f_plus_sep_rule = new IRule(root, name, '-');
        f_plus_sep_rule.derivedFrom = node;
        for (int pos = 0; pos+1 < node.children.size(); pos++) {
            f_plus_sep_rule.addCopy(node.children.get(pos));
        }
        IRepeat0 repeat = new IRepeat0(f_plus_sep_rule);
        f_plus_sep_rule.children.add(repeat);

        ISep sep = (ISep) node.lastChild();
        assert sep != null;
        for (XNode grandchild : sep.children) {
            repeat.addCopy(grandchild);
        }
        for (int pos = 0; pos+1 < node.children.size(); pos++) {
            repeat.addCopy(node.children.get(pos));
        }

        root.addRule(f_plus_sep_rule);

        return newchildren;
    }

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
        IRepeat0 repeat = new IRepeat0(f_plus_rule);
        f_plus_rule.children.add(repeat);
        for (int pos = 0; pos < node.children.size(); pos++) {
            repeat.addCopy(node.children.get(pos));
        }

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
        IOption option = new IOption(node.parent);
        f_star_rule.children.add(option);
        for (int pos = 0; pos < node.children.size(); pos++) {
            option.addCopy(node.children.get(pos));
        }
        option.addCopy(f_star);

        root.addRule(f_star_rule);

        return newchildren;
    }

    @Override
    public ArrayList<XNode> rewriteOption(IOption node) {
        ArrayList<XNode> newchildren = new ArrayList<>();

        String name = node.getNewRuleName(node, "option");
        INonterminal f_option = new INonterminal(node.parent, name, '-');
        f_option.derivedFrom = node;
        newchildren.add(f_option);

        IRule f_option_rule = new IRule(root, name, '-');
        f_option_rule.derivedFrom = node;
        root.addRule(f_option_rule);

        f_option_rule = new IRule(root, name, '-');
        f_option_rule.derivedFrom = node;
        for (int pos = 0; pos < node.children.size(); pos++) {
            f_option_rule.addCopy(node.children.get(pos));
        }

        root.addRule(f_option_rule);

        return newchildren;
    }

}
