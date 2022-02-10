package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.trees.DataTree;

import java.util.ArrayList;
import java.util.List;

public class TestConfiguration {
    public static final String xmlns_t = "https://github.com/cmsmcq/ixml-tests";
    public static final QName t_test_catalog = new QName("t", xmlns_t, "test-catalog");
    public static final QName t_test_set = new QName("t", xmlns_t, "test-set");
    public static final QName t_test_set_ref = new QName("t", xmlns_t, "test-set-ref");
    public static final QName t_test_case = new QName("t", xmlns_t, "test-case");
    public static final QName t_grammar_test = new QName("t", xmlns_t, "grammar-test");
    public static final QName t_ixml_grammar = new QName("t", xmlns_t, "ixml-grammar");
    public static final QName t_ixml_grammar_ref = new QName("t", xmlns_t, "ixml-grammar-ref");
    public static final QName t_vxml_grammar = new QName("t", xmlns_t, "vxml-grammar");
    public static final QName t_vxml_grammar_ref = new QName("t", xmlns_t, "vxml-grammar-ref");
    public static final QName t_test_string = new QName("t", xmlns_t, "test-string");
    public static final QName t_test_string_ref = new QName("t", xmlns_t, "test-string-ref");
    public static final QName t_result = new QName("t", xmlns_t, "result");
    public static final QName t_assert_xml_ref = new QName("t", xmlns_t, "assert-xml-ref");
    public static final QName t_assert_xml = new QName("t", xmlns_t, "assert-xml");
    public static final QName t_assert_not_a_grammar = new QName("t", xmlns_t, "assert-not-a-grammar");
    public static final QName t_assert_not_a_sentence = new QName("t", xmlns_t, "assert-not-a-sentence");
    public static final QName _name = new QName("", "name");
    public static final QName _href = new QName("", "href");

    public final XdmNode testCatalog;
    public final DataTree exceptions;
    public final String setName;
    public final String caseName;
    public XdmNode testSet = null;
    public XdmNode grammar = null;
    public XdmNode grammarTest = null;
    public TestConfiguration parent = null;

    public TestConfiguration(XdmNode catalog, DataTree exceptions, String setName, String caseName) {
        testCatalog = catalog;
        this.exceptions = exceptions;
        this.setName = setName;
        this.caseName = caseName;
    }

    public TestConfiguration(TestConfiguration copy, XdmNode newSet) {
        this.testCatalog = copy.testCatalog;
        this.exceptions = copy.exceptions;
        this.setName = copy.setName;
        this.caseName = copy.caseName;
        this.grammar = copy.grammar;
        this.grammarTest = copy.grammarTest;
        this.testSet = newSet;
        this.parent = copy;
    }

    public List<XdmNode> testSets() {
        XdmNode parent = testSet;
        if (parent == null) {
            parent = testCatalog;
        }

        ArrayList<XdmNode> nodes = new ArrayList<>();
        XdmSequenceIterator<XdmNode> iter = parent.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode child = iter.next();
            if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
                if (child.getNodeName().equals(t_test_set)
                        || child.getNodeName().equals(t_test_set_ref)) {
                    nodes.add(child);
                }
            }
        }
        return nodes;
    }

    public List<XdmNode> testCases() {
        ArrayList<XdmNode> nodes = new ArrayList<>();
        List<DataTree> sets;
        if (exceptions == null) {
            sets = new ArrayList<>();
        } else {
            sets = exceptions.get("exceptions").getAll("set");
        }
        DataTree dataSet = null;

        String thisSet = testSet.getAttributeValue(_name);
        boolean process = setName == null || setName.equals(thisSet);
        if (setName == null) {
            // What about exceptions?
            for (DataTree exset : sets) {
                if (thisSet.equals(exset.get("id").getValue())) {
                    dataSet = exset;
                    if (exset.getAll("case").isEmpty()) {
                        process = false;
                    }
                }
            }
        }

        if (process) {
            XdmSequenceIterator<XdmNode> iter = testSet.axisIterator(Axis.CHILD);
            while (iter.hasNext()) {
                XdmNode child = iter.next();
                if (child.getNodeKind() == XdmNodeKind.ELEMENT
                        && (child.getNodeName().equals(t_test_case) || child.getNodeName().equals(t_grammar_test))) {
                    String thisCase = child.getAttributeValue(_name);
                    if (caseName == null || caseName.equals(thisCase)) {
                        process = true;
                        if (caseName == null && dataSet != null) {
                            for (DataTree excase : dataSet.getAll("case")) {
                                if (thisCase.equals(excase.get("id").getValue())) {
                                    process = false;
                                }
                            }
                        }
                        if (process) {
                            nodes.add(child);
                        }
                    }
                }
            }
        }
        return nodes;
    }

    public XdmNode find(XdmNode parent, QName name) {
        XdmSequenceIterator<XdmNode> iter = parent.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode child = iter.next();
            if (child.getNodeKind() == XdmNodeKind.ELEMENT && child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public XdmNode findOne(XdmNode parent, QName... names) {
        for (QName name : names) {
            XdmNode found = find(parent, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public List<XdmNode> findAll(XdmNode parent, QName... names) {
        ArrayList<XdmNode> foundList = new ArrayList<>();
        XdmSequenceIterator<XdmNode> iter = parent.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode child = iter.next();
            if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
                boolean match = false;
                for (QName name : names) {
                    match = match || name.equals(child.getNodeName());
                }
                if (match) {
                    foundList.add(child);
                }
            }
        }
        return foundList;
    }
}