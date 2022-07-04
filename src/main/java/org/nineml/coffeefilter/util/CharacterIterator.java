package org.nineml.coffeefilter.util;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharacterIterator implements Iterator<Token> {
    private final CharSequence seq;
    private int pos;
    private char prevChar = ' ';
    public final String filename;
    public int lineNumber;
    public int columnNumber;
    public int offset;

    public CharacterIterator(String input) {
        this(null, input);
    }

    public CharacterIterator(String filename, String input) {
        this.filename = filename;
        seq = input;
        pos = 0;
        lineNumber = 1;
        columnNumber = 0;
        offset = 0;
    }

    @Override
    public boolean hasNext() {
        return pos < seq.length();
    }

    @Override
    public Token next() {
        if (pos >= seq.length()) {
            throw new NoSuchElementException("No more characters");
        }

        final TokenCharacter tok;
        char ch = seq.charAt(pos);
        if (ch >= 0xD800 && ch <= 0xDFFF && pos+1 < seq.length()) {
            // Is there a faster way to do this?
            char ch2 = seq.charAt(pos+1);
            String s = "" + ch + ch2;
            tok = TokenCharacter.get(s.codePointAt(0));
            pos += 2;
        } else {
            tok = TokenCharacter.get(ch);
            pos++;
        }

        offset++;
        if (prevChar == '\n') {
            columnNumber = 0;
            lineNumber++;
        } else {
            columnNumber++;
        }

        prevChar = ch;
        return tok;
    }
}
