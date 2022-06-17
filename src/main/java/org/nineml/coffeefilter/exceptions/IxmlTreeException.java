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
}
