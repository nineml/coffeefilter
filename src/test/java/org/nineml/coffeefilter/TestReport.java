package org.nineml.coffeefilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TestReport {
    private int testsToRun = 0;
    private int testsSkipped = 0;
    private int passed = 0;
    private final ArrayList<TestResult> results = new ArrayList<>();
    private final HashSet<Integer> timers = new HashSet<>();
    private final HashMap<Integer, String> messages = new HashMap<>();

    public void toRun(int toRun) {
        testsToRun = toRun;
        System.err.println("Tests to run: " + toRun);
    }

    public void toSkip(int toSkip) {
        testsSkipped = toSkip;
        System.err.println("Tests skipped: " + toSkip);
    }

    public boolean passedAll() {
        return passed == testsToRun;
    }

    public int getPassed() {
        return passed;
    }

    public int getTotal() {
        return testsToRun;
    }

    public int getSkipped() {
        return testsSkipped;
    }

    public void start(int count, String name, String setName) {
        while (results.size() <= count) {
            results.add(null);
        }

        if (name == null) {
            messages.put(count, String.format("Running test %d of %d (from %s):", count, testsToRun, setName));
        } else {
            messages.put(count, String.format("Running %s, test %d of %d:", name, count, testsToRun));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    // nop
                }

                if (results.get(count) == null) {
                    timers.add(count);
                    System.err.println(messages.get(count));
                }
            }
        }).start();
    }

    public void result(int count, TestResult result) {
        results.set(count, result);
        if (result != null) {
            if (result.state == TestState.PASS) {
                passed++;
                if (timers.contains(count)) {
                    result.summarize();
                }
            } else {
                if (!timers.contains(count)) {
                    System.err.println(messages.get(count));
                }

                if (result.actual != null) {
                    System.err.println("Actual result:");
                    System.err.println(result.actual.get(0));
                }

                for (int pos = 0; pos < result.expected.size(); pos++) {
                    System.err.println("Expected result:");
                    System.err.println(result.expected.get(pos));
                    System.err.println(result.deepEqualMessages.get(pos));
                }

                result.summarize();;
            }
        }
    }

    public void finished() {
        System.err.printf("Passed %d of %d tests.%n", passed, testsToRun);
    }
}
