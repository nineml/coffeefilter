package org.nineml.coffeefilter.model;

/**
 * The TMarked interface identifies those nodes that have a tmark.
 *
 * <p>It feels like this should be possible hierarchically, but they're present
 * on a mixture of terminas and nonterminals, so that isn't convenient.</p>
 */
public interface TMarked {
    /**
     * Return the tmark.
     * @return The tmark.
     */
    char getTMark();
}
