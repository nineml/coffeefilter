package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TestResult {
    public final XdmNode testResult;
    public final XdmNode testCase;
    public long grammarParseTime = -1;
    public long documentParseTime = -1;
    public TestState state = TestState.UNKNOWN;
    public String errorCode = null;
    public String xml = null;

    public ArrayList<XdmValue> expected = null;
    public List<XdmNode> assertions = null;
    public ArrayList<XdmValue> actual = null;
    public ArrayList<String> deepEqualMessages = null;

    public TestResult(XdmNode testResult, List<XdmNode> assertions) {
        this.testResult = testResult;
        this.assertions = assertions;
        this.testCase = testResult.getParent();
    }

    public void summarize() {
        if (state == TestState.SKIP) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        switch (state) {
            case FAIL:
                sb.append("FAIL: ");
                break;
            case PASS:
                sb.append("Pass: ");
                break;
            default:
                sb.append("\t??? Unknown: ");
        }
        if (grammarParseTime >= 0) {
            sb.append("parsed grammar in ").append(time(grammarParseTime));
            if (documentParseTime >= 0) {
                sb.append("; ");
            } else {
                sb.append(".");
            }
        }
        if (documentParseTime >= 0) {
            sb.append("parsed document in ").append(time(documentParseTime));
            sb.append(".");
        }
        System.err.println(sb);
        System.err.println("------------------------------------------------------------");
    }

    public void publish(PrintStream out) {
        out.printf("<result state='%s' ", state.toString());

        if (errorCode != null) {
            out.printf("error-code='%s' ", errorCode);
        }

        if (grammarParseTime >= 0) {
            out.printf("grammarParse='%d' ", grammarParseTime);
        }

        if (documentParseTime >= 0) {
            out.printf("documentParse='%d' ", documentParseTime);
        }

        out.printf(">%n");

        XdmSequenceIterator<XdmNode> iter = null;
        if (testResult.getNodeName().equals(TestDriver.t_app_info)) {
            out.print("<appinfo");
            iter = testResult.axisIterator(Axis.NAMESPACE);
            while (iter.hasNext()) {
                XdmNode attr = iter.next();
                if (!"xml".equals(attr.getNodeName().getLocalName())) {
                    out.printf(" xmlns:%s=\"%s\"", attr.getNodeName(). getLocalName(), attr.getStringValue());
                }
            }
            out.println(">");
            iter = testResult.axisIterator(Axis.CHILD);
            while (iter.hasNext()) {
                XdmNode child = iter.next();
                if (child.getNodeKind().equals(XdmNodeKind.ELEMENT) && child.getNodeName().equals(TestDriver.t_options)) {
                    out.print("<options");
                    XdmSequenceIterator<XdmNode> aiter = child.axisIterator(Axis.ATTRIBUTE);
                    while (aiter.hasNext()) {
                        XdmNode attr = aiter.next();
                        out.printf(" %s=\"%s\"", attr.getNodeName(), attr.getStringValue());
                    }
                    out.println("/>");
                }
            }
            out.println("</appinfo>");
        }

        if (assertions != null) {
            out.println("<assertions>");
            for (XdmNode assertion : assertions) {
                out.printf("<%s", assertion.getNodeName().getLocalName());
                iter = assertion.axisIterator(Axis.ATTRIBUTE);
                while (iter.hasNext()) {
                    XdmNode attr = iter.next();
                    out.printf(" %s='%s'", attr.getNodeName().getLocalName(), attr.getStringValue());
                }
                out.println("/>");
            }
            out.println("</assertions>");
        }

        if (expected != null && state == TestState.FAIL) {
            assert actual != null;
            assert deepEqualMessages != null;
            assert expected.size() == actual.size();
            assert deepEqualMessages.size() == actual.size();

            for (int pos = 0; pos < expected.size(); pos++) {
                out.printf("<comparison>%n");
                out.printf("<expected><ns xmlns=''>%s</ns></expected>%n", expected.get(pos));
                out.printf("<actual><ns xmlns=''>%s</ns></actual>%n", actual.get(pos));
                out.printf("<message>%s</message>%n", deepEqualMessages.get(pos));
                out.printf("</comparison>%n");
            }
        }

        out.println("</result>");
    }

    private String time(long duration) {
        return String.format("%5.3fs", duration / 1000.0);
    }
}
