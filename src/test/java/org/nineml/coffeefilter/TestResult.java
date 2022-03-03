package org.nineml.coffeefilter;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import java.util.ArrayList;

public class TestResult {
    public final XdmNode testCase;
    public long grammarParseTime = -1;
    public long documentParseTime = -1;
    public TestState state = TestState.UNKNOWN;
    public String xml = null;

    public ArrayList<XdmValue> expected = null;
    public ArrayList<XdmValue> actual = null;
    public ArrayList<String> deepEqualMessages = null;

    public TestResult(XdmNode testCase) {
        this.testCase = testCase;
    }

    public void summarize() {
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

    private String time(long duration) {
        return String.format("%5.3fs", duration / 1000.0);
    }
}
