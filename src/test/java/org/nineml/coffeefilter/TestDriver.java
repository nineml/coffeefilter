package org.nineml.coffeefilter;

import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.StandardErrorReporter;
import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.DataTree;
import org.nineml.coffeefilter.trees.DataTreeBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String USAGE = "Usage: TestDriver [-s:set] [-t:test] [-e:exceptions] catalog.xml";

    public static ParserOptions options = new ParserOptions();
    public static InvisibleXml invisibleXml = new InvisibleXml(options);
    public static boolean runningEE = true;
    public static Processor processor = null;
    public static boolean ranOne = false;
    public static int attempts = 0;
    public static final TestReport report = new TestReport(options);

    public DataTree exceptions = null;
    public ArrayList<XdmNode> testsToRun = new ArrayList<>();
    public HashMap<XdmNode, TestConfiguration> testConfigurations = new HashMap<>();
    public ArrayList<XdmNode> testsToSkip = new ArrayList<>();
    public HashMap<XdmNode, TestResult> testResults = new HashMap<>();

    public static void main(String[] args) throws IOException, SaxonApiException, URISyntaxException {
        TestDriver driver = new TestDriver();
        driver.run(args);
    }

    private void run(String[] args) throws IOException, SaxonApiException, URISyntaxException {
        String catalog = null;
        String exfile = null;
        String set_name = null;
        String case_name = null;

        int pos = 0;
        while (pos < args.length) {
            String arg = args[pos];
            if (arg.startsWith("-t:")) {
                case_name = arg.substring(3);
            } else if (arg.startsWith("-s:")) {
                set_name = arg.substring(3);
            } else if (arg.startsWith("-e:")) {
                exfile = arg.substring(3);
            } else if ("--pedantic".equals(arg)) {
                options.pedantic = true;
            } else {
                catalog = arg;
            }
            pos += 1;
        }

        if (catalog == null) {
            System.err.println(USAGE);
            System.exit(1);
        }

        if (exfile != null) {
            loadExceptions(exfile);
        }

        File cat = new File(catalog);
        if (!cat.exists()) {
            System.err.println("Cannot read catalog: " + catalog);
            System.exit(1);
        }

        processor = new Processor(true);
        runningEE = processor.isSchemaAware();
        DocumentBuilder builder = processor.newDocumentBuilder();

        TestConfiguration config = new TestConfiguration(builder.build(cat), set_name, case_name);
        readCatalogDocument(config);

        report.toRun(testsToRun.size());
        report.toSkip(testsToSkip.size());

        int count = 0;
        for (XdmNode testCase : testsToRun) {
            XdmNode testSet = testCase.getParent();
            count++;
            String name = testCase.getAttributeValue(_name);
            report.start(count, name, testSet.getAttributeValue(_name));
            runTest(testConfigurations.get(testCase), testCase);
            TestResult result = testResults.get(testCase);
            report.result(count, result);
        }

        report.finished();
        if (!report.passedAll()) {
            System.exit(1);
        }
    }

    private void loadExceptions(String exfile) throws IOException, URISyntaxException {
        InputStream stream = getClass().getResourceAsStream("/exceptions.ixml");
        InvisibleXmlParser parser = invisibleXml.getParser(stream, "/tmp/irrelevant.xml");
        InvisibleXmlDocument doc = parser.parse(new File(exfile));
        DataTreeBuilder builder = new DataTreeBuilder(new ParserOptions());
        doc.getTree(builder);
        exceptions = builder.getTree();
    }

    private void readCatalogDocument(TestConfiguration config) throws SaxonApiException {
        XdmSequenceIterator<XdmNode> iter = config.testCatalog.axisIterator(Axis.CHILD);
        while (iter.hasNext()) {
            XdmNode elem = iter.next();
            if (elem.getNodeKind() == XdmNodeKind.ELEMENT && elem.getNodeName().equals(t_test_catalog)) {
                readCatalog(new TestConfiguration(elem, config.setName, config.caseName));
                return;
            }
        }
        System.err.println("Did not find test-catalog in " + config.testCatalog.getBaseURI());
        System.exit(1);
    }

    private void readCatalog(TestConfiguration config) throws SaxonApiException {
        for (XdmNode testSet : config.testSets()) {
            readTestSet(new TestConfiguration(config, testSet));
        }
    }

    private void readTestSet(TestConfiguration config) throws SaxonApiException {
        if (t_test_set_ref.equals(config.testSet.getNodeName())) {
            DocumentBuilder builder = processor.newDocumentBuilder();
            URI href = config.testSet.getBaseURI().resolve(config.testSet.getAttributeValue(_href));
            // FIXME: what if it isn't a file?
            File cat = new File(href.getPath());
            XdmNode catalog = builder.build(cat);
            catalog = config.find(catalog, t_test_catalog);
            TestConfiguration newConfig = new TestConfiguration(catalog, config.setName, config.caseName);
            newConfig.parent = config;
            readCatalog(newConfig);
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
            readTestSet(new TestConfiguration(config, testSet));
        }

        for (XdmNode testCase : config.testCases()) {
            readTest(config, testCase);
        }
    }

    private void readTest(TestConfiguration config, XdmNode testCase) {
        testsToRun.add(testCase);
        testConfigurations.put(testCase, config);
    }

    private void runTest(TestConfiguration config, XdmNode testCase) {
        try {
            if (t_grammar_test.equals(testCase.getNodeName())) {
                doGrammarTest(config, testCase);
            } else {
                doRunTest(config, testCase);
            }
        } catch (Exception ex) {
            System.err.println("EXCEPTION " + testCase.getAttributeValue(_name) + ": " + ex.getMessage());
        }
    }

    private void doRunTest(TestConfiguration config, XdmNode testCase) throws IOException, URISyntaxException, SaxonApiException {
        ranOne = true;

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

    private void doGrammarTest(TestConfiguration config, XdmNode testCase) throws IOException, URISyntaxException, SaxonApiException {
        XdmNode result = config.find(testCase, t_result);
        if (result == null) {
            throw new RuntimeException("Test case has no result?: " + testCase.getAttributeValue(_name));
        }

        List<XdmNode> assertions = config.findAll(result, t_assert_xml, t_assert_xml_ref, t_assert_not_a_grammar, t_assert_not_a_sentence);
        if (assertions.isEmpty()) {
            throw new RuntimeException("Test case makes no assertion?: " + testCase.getAttributeValue(_name));
        }

        TestResult tresult = new TestResult(testCase);
        testResults.put(testCase, tresult);

        if (config.grammar == null) {
            throw new RuntimeException("Test case has no in-scope grammar?: " + testCase.getAttributeValue(_name));
        }

        InvisibleXmlParser parser = invisibleXml.getParser();
        tresult.grammarParseTime = parser.getParseTime();

        InvisibleXmlDocument doc = parseGrammar(config);
        tresult.documentParseTime = doc.parseTime();

        // But is it a legitimate grammar?
        InvisibleXmlParser grammarParser = loadGrammar(config);

        DocumentBuilder builder = processor.newDocumentBuilder();
        BuildingContentHandler bch = builder.newBuildingContentHandler();
        doc.getTree(bch);
        XdmNode node = bch.getDocumentNode();

        XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.CHILD);
        while (iter.hasNext() && node.getNodeKind() != XdmNodeKind.ELEMENT) {
            node = iter.next();
        }

        boolean grammarExpected = true;
        boolean same = false;
        for (XdmNode assertion : assertions) {
            XdmNode expected = null;
            if (t_assert_xml.equals(assertion.getNodeName())) {
                iter = assertion.axisIterator(Axis.CHILD);
                while (iter.hasNext()) {
                    XdmNode anode = iter.next();
                    if (anode.getNodeKind() == XdmNodeKind.ELEMENT) {
                        expected = anode;
                    }
                }
            } else if (t_assert_xml_ref.equals(assertion.getNodeName())) {
                expected = xmlFile(assertion.getBaseURI().resolve(assertion.getAttributeValue(_href)));
            } else if (t_assert_not_a_grammar.equals(assertion.getNodeName())) {
                grammarExpected = false;
            } else {
                throw new RuntimeException("Unexpected assertion: " + assertion);
            }

            if (grammarExpected) {
                assert expected != null;
                iter = expected.axisIterator(Axis.CHILD);
                while (iter.hasNext() && expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                    expected = iter.next();
                }
                if (expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                    throw new RuntimeException("Did not find element to compare against in assertion?: " + testCase.getBaseURI());
                }

                same = deepEqual(expected, node, tresult);
                if (same) {
                    break;
                }
            } else {
                same = !grammarParser.constructed();
                break;
            }
        }

        if (same) {
            tresult.state = TestState.PASS;
        } else {
            System.err.println("Actual result:");
            System.err.println(node);
            tresult.state = TestState.FAIL;
        }
    }

    private void doValidParse(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        TestResult result = new TestResult(testCase);
        testResults.put(testCase, result);

        XdmNode testString = config.findOne(testCase, t_test_string, t_test_string_ref);

        assert testString != null;

        InvisibleXmlParser parser = loadGrammar(config);
        result.grammarParseTime = parser.getParseTime();

        String input = null;
        if (t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (t_test_string_ref.equals(testString.getNodeName())) {
            input = textFile(testString.getBaseURI().resolve(testString.getAttributeValue(_href)));
        } else {
            throw new RuntimeException("Unexpected test string: " + testString.getNodeName());
        }

        InvisibleXmlDocument doc = parser.parse(input);
        result.documentParseTime = doc.parseTime();

        DocumentBuilder builder = processor.newDocumentBuilder();
        BuildingContentHandler bch = builder.newBuildingContentHandler();
        doc.getTree(bch);
        XdmNode node = bch.getDocumentNode();

        XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.CHILD);
        while (iter.hasNext() && node.getNodeKind() != XdmNodeKind.ELEMENT) {
            node = iter.next();
        }

        boolean same = false;
        for (XdmNode assertion : assertions) {
            XdmNode expected = null;
            if (t_assert_xml.equals(assertion.getNodeName())) {
                iter = assertion.axisIterator(Axis.CHILD);
                while (iter.hasNext()) {
                    XdmNode anode = iter.next();
                    if (anode.getNodeKind() == XdmNodeKind.ELEMENT) {
                        expected = anode;
                    }
                }
            } else if (t_assert_xml_ref.equals(assertion.getNodeName())) {
                expected = xmlFile(assertion.getBaseURI().resolve(assertion.getAttributeValue(_href)));
            } else {
                throw new RuntimeException("Unexpected assertion: " + assertion);
            }

            assert expected != null;
            iter = expected.axisIterator(Axis.CHILD);
            while (iter.hasNext() && expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                expected = iter.next();
            }
            if (expected.getNodeKind() != XdmNodeKind.ELEMENT) {
                throw new RuntimeException("Did not find element to compare against in assertion?: " + testCase.getBaseURI());
            }

            same = deepEqual(expected, node, result);
            if (same) {
                break;
            }
        }

        if (same) {
            result.state = TestState.PASS;
        } else {
            result.state = TestState.FAIL;
        }
    }

    private InvisibleXmlParser loadGrammar(TestConfiguration config) throws IOException, URISyntaxException {
        InvisibleXmlParser parser;
        String ixml;
        if (t_ixml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            parser = invisibleXml.getParserFromIxml(ixml);
        } else if (t_ixml_grammar_ref.equals(config.grammar.getNodeName())) {
            URI grammarURI = config.grammar.getBaseURI().resolve(config.grammar.getAttributeValue(_href));
            if (grammarURI.toString().endsWith("/ixml/tests/reference/ixml.ixml")) {
                parser = invisibleXml.getParser();
            } else {
                ixml = textFile(grammarURI);
                parser = invisibleXml.getParserFromIxml(ixml);
            }
        } else if (t_vxml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            ByteArrayInputStream bais = new ByteArrayInputStream(ixml.getBytes(StandardCharsets.UTF_8));
            parser = invisibleXml.getParser(bais, null);
        } else if (t_vxml_grammar_ref.equals(config.grammar.getNodeName())) {
            String href = config.grammar.getAttributeValue(_href);
            if (href.endsWith("/reference/ixml.xml")) {
                parser = invisibleXml.getParser();
            } else {
                parser = invisibleXml.getParser(config.grammar.getBaseURI().resolve(href));
            }
        } else {
            throw new RuntimeException("Unexpected grammar: " + config.grammar.getNodeName());
        }
        return parser;
    }

    private InvisibleXmlDocument parseGrammar(TestConfiguration config) throws IOException, URISyntaxException {
        InvisibleXmlParser parser = invisibleXml.getParser();
        InvisibleXmlDocument doc = null;
        String ixml;
        if (t_ixml_grammar.equals(config.grammar.getNodeName())) {
            ixml = config.grammar.getStringValue();
            doc = parser.parse(ixml);
        } else if (t_ixml_grammar_ref.equals(config.grammar.getNodeName())) {
            URI grammarURI = config.grammar.getBaseURI().resolve(config.grammar.getAttributeValue(_href));
            ixml = textFile(grammarURI);
            doc = parser.parse(ixml);
        } else if (t_vxml_grammar.equals(config.grammar.getNodeName())) {
            throw new UnsupportedOperationException("Cannot parse a vxml grammar");
        } else if (t_vxml_grammar_ref.equals(config.grammar.getNodeName())) {
            throw new UnsupportedOperationException("Cannot parse a vxml grammar");
        } else {
            throw new RuntimeException("Unexpected grammar: " + config.grammar.getNodeName());
        }
        return doc;
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

    private boolean deepEqual(XdmValue left, XdmValue right, TestResult result) throws SaxonApiException {
        if (result.expected == null) {
            result.expected = new ArrayList<>();
            result.expected.add(left);
            result.actual = new ArrayList<>();
            result.actual.add(right);
            result.deepEqualMessages = new ArrayList<>();
        }

        QName a = new QName("", "a");
        QName b = new QName("", "b");
        XPathCompiler compiler = processor.newXPathCompiler();
        compiler.declareVariable(a);
        compiler.declareVariable(b);
        XPathExecutable exec;
        if (runningEE) {
            compiler.declareNamespace("saxon", "http://saxon.sf.net/");
            exec = compiler.compile("saxon:deep-equal($a,$b, (), '?')");
        } else {
            exec = compiler.compile("deep-equal($a,$b)");
        }
        XPathSelector selector = exec.load();
        selector.setVariable(a, left);
        selector.setVariable(b, right);

        CaptureErrors capture = new CaptureErrors();
        ErrorReporter saveReporter = selector.getUnderlyingXPathContext().getErrorReporter();
        selector.getUnderlyingXPathContext().setErrorReporter(capture);

        XdmSequenceIterator<XdmItem> iter = selector.iterator();
        XdmAtomicValue item = (XdmAtomicValue) iter.next();

        selector.getUnderlyingXPathContext().setErrorReporter(saveReporter);

        if (capture.messages.isEmpty()) {
            result.deepEqualMessages.add("");
        } else {
            result.deepEqualMessages.add(capture.messages.get(0));
        }

        return item.getBooleanValue();
    }

    private void doNotASentence(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        TestResult result = new TestResult(testCase);
        testResults.put(testCase, result);

        XdmNode testString = config.findOne(testCase, t_test_string, t_test_string_ref);

        assert testString != null;
        attempts++;

        InvisibleXmlParser parser = loadGrammar(config);
        result.grammarParseTime = parser.getParseTime();

        String input = null;
        if (t_test_string.equals(testString.getNodeName())) {
            input = testString.getStringValue();
        } else if (t_test_string_ref.equals(testString.getNodeName())) {
            input = textFile(testString.getBaseURI().resolve(testString.getAttributeValue(_href)));
        } else {
            throw new RuntimeException("Unexpected test string: " + testString.getNodeName());
        }

        InvisibleXmlDocument doc = parser.parse(input);
        result.documentParseTime = doc.parseTime();

        if (doc.getNumberOfParses() == 0) {
            result.state = TestState.PASS;
        } else {
            result.state = TestState.FAIL;
            result.xml = doc.getTree();
        }
    }

    private void doNotAGrammar(TestConfiguration config, XdmNode testCase, List<XdmNode> assertions) throws IOException, URISyntaxException, SaxonApiException {
        TestResult result = new TestResult(testCase);
        testResults.put(testCase, result);

        try {
            InvisibleXmlParser parser = loadGrammar(config);
            result.grammarParseTime = parser.getParseTime();
            if (parser.constructed()) {
                result.state = TestState.FAIL;
            } else {
                result.state = TestState.PASS;
            }
        } catch (IxmlException ex) {
            result.state = TestState.PASS;
        }
    }

    private class TestConfiguration {
        public final XdmNode testCatalog;
        public final String setName;
        public final String caseName;
        public XdmNode testSet = null;
        public XdmNode grammar = null;
        public XdmNode grammarTest = null;
        public TestConfiguration parent = null;

        public TestConfiguration(XdmNode catalog, String setName, String caseName) {
            testCatalog = catalog;
            this.setName = setName;
            this.caseName = caseName;
        }

        public TestConfiguration(TestConfiguration copy, XdmNode newSet) {
            this.testCatalog = copy.testCatalog;
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

            XdmSequenceIterator<XdmNode> iter = testSet.axisIterator(Axis.CHILD);
            while (iter.hasNext()) {
                XdmNode child = iter.next();
                if (child.getNodeKind() == XdmNodeKind.ELEMENT
                        && (child.getNodeName().equals(t_test_case) || child.getNodeName().equals(t_grammar_test))) {
                    String thisCase = child.getAttributeValue(_name);
                    if (caseName == null || caseName.equals(thisCase)) {
                        boolean processCase = true;
                        if (caseName == null && dataSet != null) {
                            for (DataTree excase : dataSet.getAll("case")) {
                                if (thisCase.equals(excase.get("id").getValue())) {
                                    processCase = false;
                                }
                            }
                        }
                        if (process && processCase) {
                            nodes.add(child);
                        } else {
                            testsToSkip.add(child);
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

    public static class CaptureErrors implements ErrorReporter {
        public final ArrayList<String> messages = new ArrayList<>();

        @Override
        public void report(XmlProcessingError xmlProcessingError) {
            messages.add(xmlProcessingError.getMessage());
        }
    }
}
