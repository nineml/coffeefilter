package org.nineml.coffeefilter;

import net.sf.saxon.s9api.XdmNode;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TestResults {
    public final ParserOptions options;
    public final HashMap<XdmNode, ArrayList<TestResult>> results = new HashMap<>();

    public TestResults(ParserOptions options) {
        this.options = options;
    }

    public TestResult createResult(XdmNode expectedResult) {
        return createResult(expectedResult, null);
    }

    public TestResult createResult(XdmNode expectedResult, List<XdmNode> assertions) {
        TestResult result = new TestResult(expectedResult, assertions);
        XdmNode testCase = expectedResult.getParent();
        if (results.containsKey(testCase)) {
            results.get(testCase).add(result);
        } else {
            ArrayList<TestResult> testout = new ArrayList<>();
            testout.add(result);
            results.put(testCase, testout);
        }
        return result;
    }

    public boolean failedTests() {
        for (XdmNode testCase : results.keySet()) {
            for (TestResult result : results.get(testCase)) {
                if (result.state == TestState.FAIL) {
                    return true;
                }
            }
        }

        return false;
    }

    public String summary() {
        int cases = 0;
        int reports = 0;
        int pass = 0;
        int fail = 0;
        int skip = 0;
        int inapp = 0;

        for (XdmNode testCase : results.keySet()) {
            cases++;
            for (TestResult result : results.get(testCase)) {
                reports++;
                switch (result.state) {
                    case PASS:
                        pass++;
                        break;
                    case FAIL:
                        fail++;
                        break;
                    case SKIP:
                        skip++;
                        break;
                    case INAPPLICABLE:
                        inapp++;
                        break;
                    default:
                        break;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(reports).append(" reports for ").append(cases).append(" cases. ");
        sb.append(pass).append(" pass, ").append(fail).append(" fail, ");
        sb.append(skip).append(" skip, ").append(inapp).append(" inapplicable.");
        return sb.toString();
    }

    public void publish(PrintStream out) {
        LocalDateTime date = LocalDateTime.now();
        String isodt = date.format(DateTimeFormatter.ISO_DATE_TIME);

        // Yuck. XML via print statements
        out.print("<test-results xmlns='https://nineml.org/ns/test-results'");
        out.printf(" date='%s'", isodt);
        out.printf(" coffeegrinder-version='%s'", org.nineml.coffeegrinder.BuildConfig.VERSION);
        out.printf(" coffeefilter-version='%s'", BuildConfig.VERSION);
        out.println(">");

        // Alas, the test driver makes copies of the set and case nodes,
        // so node-identity isn't useful here.

        HashSet<XdmNode> testSets = new HashSet<>();
        for (XdmNode testCase : results.keySet()) {
            XdmNode parent = testCase.getParent();
            if (TestDriver.t_test_set.equals(parent.getNodeName())) {
                testSets.add(parent);
            } else {
                throw new RuntimeException("Unexpected test case parent: " + parent.getNodeName());
            }
        }

        for (XdmNode testSet : testSets) {
            String name= testSet.getAttributeValue(TestDriver._name);
            out.println("<test-set name='" + name + "'>");
            for (XdmNode testCase : results.keySet()) {
                XdmNode parent = testCase.getParent();
                if (TestDriver.t_test_set.equals(parent.getNodeName())
                    && name != null && name.equals(parent.getAttributeValue(TestDriver._name))) {
                    String tname = testCase.getAttributeValue(TestDriver._name);
                    if (tname == null) {
                        out.println("<test-case>");
                    } else {
                        out.println("<test-case name='" + tname + "'>");
                    }
                    for (TestResult result : results.get(testCase)) {
                        result.publish(out);
                    }
                    out.println("</test-case>");
                }
            }
            out.println("</test-set>");
        }
        out.println("</test-results>");
    }
}
