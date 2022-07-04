package org.nineml.coffeefilter.exceptions;

/**
 * An Ixml tree building exception.
 *
 * <p>IxmlTreeException represents errors that occurred during tree building.</p>
 */
public class IxmlTreeException extends IxmlException {
    /**
     * Create an IxmlTreeException with a message.
     * @param code the code.
     * @param message The message.
     */
    public IxmlTreeException(String code, String message) {
        super(code, message);
    }

    /**
     * Create an IxmlTreeException that wraps another exception.
     * @param code the code.
     * @param message A message for the wrapper.
     * @param cause The underlying exception.
     */
    public IxmlTreeException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static IxmlTreeException getException(String code) {
        return getException(code, new String[] {});
    }

    private static IxmlTreeException getException(String code, String param1) {
        return getException(code, new String[] {param1});
    }

    private static IxmlTreeException getException(String code, String[] params) {
        return new IxmlTreeException(code, MessageGenerator.getMessage(code, params));
    }

    public static IxmlTreeException noMixedContent() { return getException("DT01"); }
    public static IxmlTreeException duplicatesForbidden(String name) { return getException("DT02", name); }
    public static IxmlTreeException noCsv() { return getException("CV01"); }

    public static IxmlTreeException documentStarted() {
        return getException("XB01");
    }
    public static IxmlTreeException documentNotStarted() {
        return getException("XB02");
    }
    public static IxmlTreeException namespaceRedefined(String prefix) {
        return getException("XB04", prefix);
    }
    public static IxmlTreeException attributeNotAllowed() {
        return getException("XB05");
    }
    public static IxmlTreeException attributeRedefined(String name) {
        return getException("XB06", name);
    }
    public static IxmlTreeException unbalancedTags() {
        return getException("XB15");
    }
    public static IxmlTreeException unbalancedTags(String unexpected) {
        return getException("XB07", unexpected);
    }
    public static IxmlTreeException unbalancedTags(String expected, String actual) {
        return getException("XB08", new String[] { expected, actual });
    }
    public static IxmlTreeException endElementNotAllowed() {
        return getException("XB09");
    }
    public static IxmlTreeException commentNotAllowed() {
        return getException("XB10");
    }
    public static IxmlTreeException processingInstructionNotAllowed() {
        return getException("XB11");
    }
    public static IxmlTreeException textNotAllowed() {
        return getException("XB12");
    }
    public static IxmlTreeException invalidName(String name) {
        return getException("XB13", name);
    }
    public static IxmlTreeException invalidPrefix(String name) {
        return getException("XB14", name);
    }
    public static IxmlTreeException invalidText(String codePoint) {
        return getException("XB16", codePoint);
    }
}
