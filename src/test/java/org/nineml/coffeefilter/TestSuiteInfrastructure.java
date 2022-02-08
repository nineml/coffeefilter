package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.exceptions.IxmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class TestSuiteInfrastructure {
    public static Processor processor = null;
    private static XdmNode catalog = null;
    private int attempts = 0;
    private int passes = 0;
    private boolean ranOne = false;

    public TestSuiteInfrastructure() {
        if (catalog == null) {
            try {
                processor = new Processor(true);
                DocumentBuilder builder = processor.newDocumentBuilder();
                catalog = builder.build(new File("test-suite/test-catalog.xml"));
            } catch (SaxonApiException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    protected boolean pass(String testSet, String testCase) {
        try {
            TestConfiguration config = new TestConfiguration(catalog, testSet, testCase);
            attempts = 0;
            passes = 0;
            ranOne = false;
            runCatalogDocument(config);
            return passes == attempts && ranOne;
        } catch (SaxonApiException ex) {
            return false;
        }
    }


    private void runCatalogDocument(TestConfiguration config) throws SaxonApiException {
        XdmSequenceIterator<XdmNode> iter = config.testCatalog.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode elem = iter.next();
            if (elem.getNodeKind() == XdmNodeKind.ELEMENT && elem.getNodeName().equals(TestConfiguration.t_test_catalog)) {
                runCatalog(new TestConfiguration(elem, config.setName, config.caseName));
                return;
            }
        }
        System.err.println("Did not find test-catalog in " + config.testCatalog.getBaseURI());
        System.exit(1);
    }

    private void runCatalog(TestConfiguration config) throws SaxonApiException {
        for (XdmNode testSet : config.testSets()) {
            runTestSet(new TestConfiguration(config, testSet));
        }
    }

    private void runTestSet(TestConfiguration config) throws SaxonApiException {
        if (TestConfiguration.t_test_set_ref.equals(config.testSet.getNodeName())) {
            DocumentBuilder builder = processor.newDocumentBuilder();
            URI href = config.testSet.getBaseURI().resolve(config.testSet.getAttributeValue(TestConfiguration._href));
            // FIXME: what if it isn't a file?
            File cat = new File(href.getPath());
            XdmNode catalog = builder.build(cat);
            catalog = config.find(catalog, TestConfiguration.t_test_catalog);
            TestConfiguration newConfig = new TestConfiguration(catalog, config.setName, config.caseName);
            newConfig.parent = config;
            runCatalog(newConfig);
            return;
        }

        XdmNode grammar = config.findOne(config.testSet, TestConfiguration.t_ixml_grammar,
                TestConfiguration.t_ixml_grammar_ref, TestConfiguration.t_vxml_grammar, TestConfiguration.t_vxml_grammar_ref);
        XdmNode grammarTest = config.find(config.testSet, TestConfiguration.t_grammar_test);

        if (grammar != null) {
            config.grammar = grammar;
        }

        if (grammarTest != null) {
            config.grammarTest = grammarTest;
        }

        for (XdmNode testSet : config.testSets()) {
            runTestSet(new TestConfiguration(config, testSet));
        }

        for (XdmNode testCase : config.testCases()) {
            runTest(config, testCase);
        }
    }

    private void runTest(TestConfiguration config, XdmNode testCase) {
        try {
            doRunTest(config, testCase);
        } catch (Exception ex) {
            System.err.println("EXCEPTION " + testCase.getAttributeValue(TestConfiguration._name) + ": " + ex.getMessage());
        }
    }

    private void doRunTest(TestConfiguration config, XdmNode testCase) throws IOException, URISyntaxException, SaxonApiException {
        ranOne = true;
        if (testCase.getAttributeValue(TestConfiguration._name) != null) {
            System.err.printf("RUN %s in %s\n", testCase.getAttributeValue(TestConfiguration._name), config.testSet.getAttributeValue(TestConfiguration._name));
        } else {
            System.err.printf("RUN test in %s\n", config.testSet.getAttributeValue(TestConfiguration._name));
        }

        XdmNode result = config.find(testCase, TestConfiguration.t_result);
        if (result == null) {
            throw new RuntimeException("Test case has no result?: " + testCase.getAttributeValue(TestConfiguration._name));
        }

        List<XdmNode> assertions = config.findAll(result, TestConfiguration.t_assert_xml, TestConfiguration.t_assert_xml_ref, TestConfiguration.t_assert_not_a_grammar, TestConfiguration.t_assert_not_a_sentence);
        if (assertions.isEmpty()) {
            throw new RuntimeException("Test case makes no assertion?: " + testCase.getAttributeValue(TestConfiguration._name));
        }

        if (TestConfiguration.t_assert_not_a_grammar.equals(assertions.get(0).getNodeName())) {
            doNotAGrammar(config, testCase, assertions);
            return;
        }

        if (TestConfiguration.t_assert_not_a_sentence.equals(assertions.get(0).getNodeName())) {
            doNotASentence(config, testCase, assertions);
            return;
        }

        XdmNode testString = config.findOne(testCase, TestConfiguration.t_test_string, TestConfiguration.t_test_string_ref);
        if (testString == null) {
            throw new RuntimeException("Test case has no test string?: " + testCase.getAttributeValue(TestConfiguration._name));
        }

        if (config.grammar == null) {
            throw new RuntimeException("Test case has no in-scope grammar?: " + testCase.getAttributeValue(TestConfiguration._name));
        }

        doValidParse(config, testCase, assertions);
    }

    private void doValidParse(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        XdmNode testString = config.findOne(testCase, TestConfiguration.t_test_string, TestConfiguration.t_test_string_ref);

        assert testString != null;
        attempts++;

        InvisibleXmlParser parser = loadGrammar(config);

        String input = null;
        if (TestConfiguration.t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (TestConfiguration.t_test_string_ref.equals(testString.getNodeName())) {
            input = textFile(testString.getBaseURI().resolve(testString.getAttributeValue(TestConfiguration._href)));
        } else {
            throw new RuntimeException("Unexpected test string: " + testString.getNodeName());
        }

        InvisibleXmlDocument doc = parser.parse(input);

        DocumentBuilder builder = processor.newDocumentBuilder();
        BuildingContentHandler bch = builder.newBuildingContentHandler();
        doc.getTree(bch);
        XdmNode node = bch.getDocumentNode();

        XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.CHILD);
        while (iter.hasNext() && node.getNodeKind() != XdmNodeKind.ELEMENT) {
            node = iter.next();
        }

        doc.getEarleyResult().getForest().serialize("graph2.xml");

        boolean same = false;
        for (XdmNode assertion : assertions) {
            XdmNode expected = null;
            if (TestConfiguration.t_assert_xml.equals(assertion.getNodeName())) {
                expected = assertion;
            } else if (TestConfiguration.t_assert_xml_ref.equals(assertion.getNodeName())) {
                expected = xmlFile(assertion.getBaseURI().resolve(assertion.getAttributeValue(TestConfiguration._href)));
            } else {
                throw new RuntimeException("Unexpected assertion: " + assertion);
            }

            iter = expected.axisIterator(Axis.CHILD);
            while (iter.hasNext() && expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                expected = iter.next();
            }
            if (expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                throw new RuntimeException("Did not find element to compare against in assertion?: " + testCase.getBaseURI());
            }

            same = deepEqual(expected, node);
            if (same) {
                break;
            }
        }

        if (same) {
            System.err.println("PASS");
            passes++;
        } else {
            System.err.println("FAIL");
        }
    }

    private InvisibleXmlParser loadGrammar(TestConfiguration config) throws IOException, URISyntaxException {
        InvisibleXmlParser parser;
        String ixml;
        if (TestConfiguration.t_ixml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            parser = InvisibleXml.parserFromString(ixml);
        } else if (TestConfiguration.t_ixml_grammar_ref.equals(config.grammar.getNodeName())) {
            ixml = textFile(config.grammar.getBaseURI().resolve(config.grammar.getAttributeValue(TestConfiguration._href)));
            parser = InvisibleXml.parserFromString(ixml);
        } else if (TestConfiguration.t_vxml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            parser = InvisibleXml.parserFromVxmlString(ixml);
        } else if (TestConfiguration.t_vxml_grammar_ref.equals(config.grammar.getNodeName())) {
            String href = config.grammar.getAttributeValue(TestConfiguration._href);
            if (href.endsWith("/reference/ixml.xml")) {
                parser = InvisibleXml.invisibleXmlParser();
            } else {
                parser = InvisibleXml.parserFromVxml(config.grammar.getBaseURI().resolve(href).getPath());
            }
        } else {
            throw new RuntimeException("Unexpected grammar: " + config.grammar.getNodeName());
        }
        return parser;
    }

    private String textFile(URI uri) {
        try {
            String filename = uri.getPath();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), "UTF-8");
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int len = reader.read(buffer);
            while (len >= 0) {
                sb.append(buffer, 0, len);
                len = reader.read(buffer);
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private XdmNode xmlFile(URI uri) throws SaxonApiException {
        String filename = uri.getPath();
        File input = new File(filename);
        DocumentBuilder builder = processor.newDocumentBuilder();
        return builder.build(input);
    }

    private boolean deepEqual(XdmValue left, XdmValue right) throws SaxonApiException {
        QName a = new QName("", "a");
        QName b = new QName("", "b");
        XPathCompiler compiler = processor.newXPathCompiler();
        compiler.declareVariable(a);
        compiler.declareVariable(b);
        compiler.declareNamespace("saxon", "http://saxon.sf.net/");
        XPathExecutable exec = compiler.compile("saxon:deep-equal($a,$b, (), '?')");
        XPathSelector selector = exec.load();
        selector.setVariable(a, left);
        selector.setVariable(b, right);
        XdmSequenceIterator<XdmItem> iter = selector.iterator();
        XdmAtomicValue item = (XdmAtomicValue) iter.next();
        return item.getBooleanValue();
    }

    private void doNotASentence(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        XdmNode testString = config.findOne(testCase, TestConfiguration.t_test_string, TestConfiguration.t_test_string_ref);

        assert testString != null;
        attempts++;

        InvisibleXmlParser parser = loadGrammar(config);

        String input = null;
        if (TestConfiguration.t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (TestConfiguration.t_test_string_ref.equals(testString.getNodeName())) {
            input =textFile(testString.getBaseURI().resolve(testString.getAttributeValue(TestConfiguration._href)));
        } else {
            throw new RuntimeException("Unexpected test string: " + testString.getNodeName());
        }

        InvisibleXmlDocument doc = parser.parse(input);

        if (doc.numberOfParses() == 0) {
            System.err.println("PASS");
            passes++;
        } else {
            System.err.println("FAIL");
            System.err.println(doc.getTree());
        }
    }

    private void doNotAGrammar(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        attempts++;

        try {
            InvisibleXmlParser parser = loadGrammar(config);
            if (parser.constructed()) {
                System.err.println("FAIL");
            } else {
                System.err.println("PASS");
                passes++;
            }
        } catch (IxmlException ex) {
            System.err.println("PASS");
            passes++;
        }
    }
}
