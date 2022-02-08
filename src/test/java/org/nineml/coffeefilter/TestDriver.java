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

public class TestDriver {
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

    public static Processor processor = null;
    public static boolean ranOne = false;
    public static int attempts = 0;
    public static int passes = 0;

    public static void main(String[] args) throws IOException, SaxonApiException {
        TestDriver driver = new TestDriver();
        driver.run(args);
    }

    private void run(String[] args) throws IOException, SaxonApiException {
        String catalog = null;
        String set_name = null;
        String case_name = null;

        // Usage: TestDriver [-s:set] [-t:test] catalog.xml

        int pos = 0;
        while (pos < args.length) {
            String arg = args[pos];
            if (arg.startsWith("-t:")) {
                case_name = arg.substring(3);
            } else if (arg.startsWith("-s:")) {
                set_name = arg.substring(3);
            } else {
                catalog = arg;
            }
            pos += 1;
        }

        if (catalog == null) {
            System.err.println("Usage: TestDriver [-s:test-set] [-t:test-case] catalog.xml");
            System.exit(1);
        }

        File cat = new File(catalog);
        if (!cat.exists()) {
            System.err.println("Cannot read catalog: " + catalog);
            System.exit(1);
        }

        processor = new Processor(true);
        DocumentBuilder builder = processor.newDocumentBuilder();

        TestConfiguration config = new TestConfiguration(builder.build(cat), set_name, case_name);
        runCatalogDocument(config);
        System.out.printf("Passed %d of %d attempted tests (%d fails)", passes, attempts, (attempts-passes));
    }

    private void runCatalogDocument(TestConfiguration config) throws SaxonApiException {
        XdmSequenceIterator<XdmNode> iter = config.testCatalog.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode elem = iter.next();
            if (elem.getNodeKind() == XdmNodeKind.ELEMENT && elem.getNodeName().equals(t_test_catalog)) {
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
        if (t_test_set_ref.equals(config.testSet.getNodeName())) {
            DocumentBuilder builder = processor.newDocumentBuilder();
            URI href = config.testSet.getBaseURI().resolve(config.testSet.getAttributeValue(_href));
            // FIXME: what if it isn't a file?
            File cat = new File(href.getPath());
            XdmNode catalog = builder.build(cat);
            catalog = config.find(catalog, t_test_catalog);
            TestConfiguration newConfig = new TestConfiguration(catalog, config.setName, config.caseName);
            newConfig.parent = config;
            runCatalog(newConfig);
            return;
        }

        XdmNode grammar = config.findOne(config.testSet, t_ixml_grammar, t_ixml_grammar_ref, t_vxml_grammar, t_vxml_grammar_ref);
        XdmNode grammarTest = config.find(config.testSet, t_grammar_test);

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
            System.err.println("EXCEPTION " + testCase.getAttributeValue(_name) + ": " + ex.getMessage());
        }
    }

    private void doRunTest(TestConfiguration config, XdmNode testCase) throws IOException, URISyntaxException, SaxonApiException {
        ranOne = true;
        if (testCase.getAttributeValue(_name) != null) {
            System.err.printf("RUN %s in %s\n", testCase.getAttributeValue(_name), config.testSet.getAttributeValue(_name));
        } else {
            System.err.printf("RUN test in %s\n", config.testSet.getAttributeValue(_name));
        }

        XdmNode result = config.find(testCase, t_result);
        if (result == null) {
            throw new RuntimeException("Test case has no result?: " + testCase.getAttributeValue(_name));
        }

        List<XdmNode> assertions = config.findAll(result, t_assert_xml, t_assert_xml_ref, t_assert_not_a_grammar, t_assert_not_a_sentence);
        if (assertions.isEmpty()) {
            throw new RuntimeException("Test case makes no assertion?: " + testCase.getAttributeValue(_name));
        }

        if (t_assert_not_a_grammar.equals(assertions.get(0).getNodeName())) {
            doNotAGrammar(config, testCase, assertions);
            return;
        }

        if (t_assert_not_a_sentence.equals(assertions.get(0).getNodeName())) {
            doNotASentence(config, testCase, assertions);
            return;
        }

        XdmNode testString = config.findOne(testCase, t_test_string, t_test_string_ref);
        if (testString == null) {
            throw new RuntimeException("Test case has no test string?: " + testCase.getAttributeValue(_name));
        }

        if (config.grammar == null) {
            throw new RuntimeException("Test case has no in-scope grammar?: " + testCase.getAttributeValue(_name));
        }

        doValidParse(config, testCase, assertions);
    }

    private void doValidParse(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        XdmNode testString = config.findOne(testCase, t_test_string, t_test_string_ref);

        assert testString != null;
        attempts++;

        InvisibleXmlParser parser = loadGrammar(config);

        String input = null;
        if (t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (t_test_string_ref.equals(testString.getNodeName())) {
            input = textFile(testString.getBaseURI().resolve(testString.getAttributeValue(_href)));
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
            if (t_assert_xml.equals(assertion.getNodeName())) {
                expected = assertion;
            } else if (t_assert_xml_ref.equals(assertion.getNodeName())) {
                expected = xmlFile(assertion.getBaseURI().resolve(assertion.getAttributeValue(_href)));
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
        if (t_ixml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            parser = InvisibleXml.parserFromString(ixml);
        } else if (t_ixml_grammar_ref.equals(config.grammar.getNodeName())) {
            ixml = textFile(config.grammar.getBaseURI().resolve(config.grammar.getAttributeValue(_href)));
            parser = InvisibleXml.parserFromString(ixml);
        } else if (t_vxml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            parser = InvisibleXml.parserFromVxmlString(ixml);
        } else if (t_vxml_grammar_ref.equals(config.grammar.getNodeName())) {
            String href = config.grammar.getAttributeValue(_href);
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
        XdmNode testString = config.findOne(testCase, t_test_string, t_test_string_ref);

        assert testString != null;
        attempts++;

        InvisibleXmlParser parser = loadGrammar(config);

        String input = null;
        if (t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (t_test_string_ref.equals(testString.getNodeName())) {
            input =textFile(testString.getBaseURI().resolve(testString.getAttributeValue(_href)));
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
            loadGrammar(config);
            System.err.println("FAIL");
        } catch (IxmlException ex) {
            System.err.println("PASS");
            passes++;
        }
    }
}
