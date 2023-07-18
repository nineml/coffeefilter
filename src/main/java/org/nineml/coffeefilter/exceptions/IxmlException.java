package org.nineml.coffeefilter.exceptions;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;

import java.util.Set;

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
    public static IxmlException parseFailed(String message) {
        return getException("P005", new String[] { message });
    }
    public static IxmlException repeatedAttribute(String name) { return getException("D02", name); }
    public static IxmlException invalidCharacterClass(String name) { return getException("S10", name); }
    public static IxmlException invalidXmlName(String name) { return getException("D03", name); }
    public static IxmlException invalidXmlNameCharacter(String name, int ch) { return getException("D03", new String[] {name, ""+ch}); }
    public static IxmlException invalidXmlCharacter(String ch) { return getException("D04", ch); }
    public static IxmlException multipleDefinitionsOfSymbol(String name) { return getException("S03", name); }
    public static IxmlException invalidMark(char mark) { return getException("S05", ""+mark); }
    public static IxmlException invalidTMark(char mark) { return getException("E006", ""+mark); }
    public static IxmlException invalidHexCharacters(String hex) { return getException("S06", hex); }
    public static IxmlException invalidHexTooLarge(String hex) { return getException("S07", hex); }
    public static IxmlException invalidHex(String hex) { return getException("S08", hex); }
    public static IxmlException attributeRoot(String name) { return getException("D05", name); }
    public static IxmlException notSingleRooted(String name) { return getException("D06", name); }
    public static IxmlException attributeNameForbidden(String name) { return getException("D07", name); }
    public static IxmlException invalidRange(String from, String to) { return getException("S09", new String[] {from, to}); }
    public static IxmlException undefinedSymbols(Set<NonterminalSymbol> symbols) { return getException("S02", symbolList(symbols)); }
    public static IxmlException unreachableSymbols(Set<NonterminalSymbol> symbols) { return getException("E012", symbolList(symbols)); }
    public static IxmlException unproductiveSymbols(Set<NonterminalSymbol> symbols) { return getException("E013", symbolList(symbols)); }
    public static IxmlException cannotParseFailure() {
        return getException("E014");
    }
    public static IxmlException renameUnavailable(String name, String rename) { return getException("E015", new String[] {name, rename}); }
    public static IxmlException repeatedPragma(String name, String data1, String data2) { return getException("E016", new String[] {name, data1, data2}); }

    private static String symbolList(Set<NonterminalSymbol> symbols) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (NonterminalSymbol symbol : symbols) {
            sb.append(sep);
            sb.append(symbol);
            sep = ", ";
        }
        return sb.toString();
    }
}
