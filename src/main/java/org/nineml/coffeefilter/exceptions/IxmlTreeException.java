package org.nineml.coffeefilter.exceptions;

/**
 * An Ixml tree building exception.
 *
 * <p>IxmlTreeException represents errors that occurred during tree building.</p>
 */
public class IxmlTreeException extends IxmlException {
    /**
     * Create an IxmlTreeException with a message.
     * @param message The message.
     */
    public IxmlTreeException(String message) {
        super(message);
    }

    /**
     * Create an IxmlTreeException that wraps another exception.
     * @param message A message for the wrapper.
     * @param cause The underlying exception.
     */
    public IxmlTreeException(String message, Throwable cause) {
        super(message, cause);
    }
}
