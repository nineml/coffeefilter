package org.nineml.coffeefilter.exceptions;

import org.nineml.coffeefilter.model.Ixml;

/**
 * An Ixml exception.
 *
 * <p>IxmlException is just a wrapper around a {@link RuntimeException}. It's provided as
 * a convenience for catching errors raised by the Ixml parser.</p>
 */
public class IxmlException extends RuntimeException {
    private final String code;

    /**
     * Create an IxmlException with a message.
     * @param code the code.
     * @param message The message.
     */
    public IxmlException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Create an IxmlException that wraps another exception.
     * @param code the code.
     * @param message A message for the wrapper.
     * @param cause The underlying exception.
     */
    public IxmlException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Get the error code.
     * @return the error code
     */
    public String getCode() {
        return code;
    }

    private static IxmlException getException(String code) {
        return getException(code, new String[] {});
    }

    private static IxmlException getException(String code, String param1) {
        return getException(code, new String[] {param1});
    }

    private static IxmlException getException(String code, String[] params) {
        return new IxmlException(code, MessageGenerator.getMessage(code, params));
    }

    private static IxmlException getException(String code, String[] params, Exception ex) {
        return new IxmlException(code, MessageGenerator.getMessage(code, params), ex);
    }

    public static IxmlException failedToLoadIxmlGrammar(String name) { return getException("I001", name); }
    public static IxmlException failedToLoadIxmlGrammar(Exception ex) { return getException("I002", new String[] { ex.getMessage() }, ex); }
    public static IxmlException internalError(String message) { return getException("I003", message); }
    public static IxmlException internalError(String message, Exception ex) { return getException("I002", new String[] { message }, ex); }
    public static IxmlException sniffingFailed(String systemId) { return getException("L001", systemId); }
    public static IxmlException failedtoParse(String systemId, Exception ex) {
        return getException("P004", new String[] {systemId, ex.getMessage() }, ex);
    }
    public static IxmlException parseFailed(Exception ex) {
        return getException("P005", new String[] { ex.getMessage() }, ex);
    }
    public static IxmlException noRuleForSymbol(String name) { return getException("E001", name); }
    public static IxmlException invalidCharacterClass(String name) { return getException("E002", name); }
    public static IxmlException invalidXmlName(String name) { return getException("E003", name); }
    public static IxmlException duplicateRuleForSymbol(String name) { return getException("E004", name); }
    public static IxmlException invalidMark(char mark) { return getException("E005", ""+mark); }
    public static IxmlException invalidTMark(char mark) { return getException("E006", ""+mark); }
    public static IxmlException invalidHex(String hex) { return getException("E007", hex); }
    public static IxmlException attributeRoot(String name) { return getException("E008", name); }
    public static IxmlException multipleRoots(String name) { return getException("E009", name); }
    public static IxmlException invalidRange(String from, String to) { return getException("E010", new String[] {from, to}); }

}
