package org.nineml.coffeefilter.exceptions;

/**
 * An Ixml exception.
 *
 * <p>IxmlException is just a wrapper around a {@link RuntimeException}. It's provided as
 * a convenience for catching errors raised by the Ixml parser.</p>
 */
public class IxmlException extends RuntimeException {
    /**
     * Create an IxmlException with a message.
     * @param message The message.
     */
    public IxmlException(String message) {
        super(message);
    }

    /**
     * Create an IxmlException that wraps another exception.
     * @param message A message for the wrapper.
     * @param cause The underlying exception.
     */
    public IxmlException(String message, Throwable cause) {
        super(message, cause);
    }
}
