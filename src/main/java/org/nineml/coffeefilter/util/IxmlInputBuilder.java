package org.nineml.coffeefilter.util;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class IxmlInputBuilder {
    private enum State { IN_TEXT, IN_DOUBLE, IN_SINGLE, IN_COMMENT, IN_PRAGMA };

    private int offset = 0;
    private int line = 1;
    private int column = 1;
    private boolean mark = false;
    private int pragmadepth = 0;
    private int commentdepth = 0;
    private State state = State.IN_TEXT;
    private final Iterator<Token> input;
    private TokenCharacter peek = null;
    private ArrayList<Token> tokens = null;
    private ArrayList<Token> buffer = null;

    private IxmlInputBuilder(Iterator<Token> input) {
        this.input = input;
        tokens = new ArrayList<>();
    }

    public static Token[] fromString(String input) {
        return IxmlInputBuilder.fromIterator(Iterators.characterIterator(input));
    }

    public static Token[] fromArray(Token[] input) {
        return IxmlInputBuilder.fromIterator(Arrays.stream(input).iterator());
    }

    public static Token[] fromIterator(Iterator<Token> input) {
        IxmlInputBuilder builder = new IxmlInputBuilder(input);
        return builder.parseInput();
    }

    private Token[] parseInput() {
        while (peek != null || input.hasNext()) {
            TokenCharacter token = peek == null ? (TokenCharacter) input.next() : peek;
            peek = null;

            if (mark) {
                token.addAttribute(new ParserAttribute(ParserAttribute.LINE_NUMBER_NAME, "" + line));
                token.addAttribute(new ParserAttribute(ParserAttribute.COLUMN_NUMBER_NAME, "" + column));
                token.addAttribute(new ParserAttribute(ParserAttribute.OFFSET_NAME, "" + offset));
                mark = false;
            }

            offset++;
            column++;
            if (token.getCodepoint() == '\n') {
                line++;
                column = 1;
            }

            switch (state) {
                case IN_TEXT:
                    advanceText(token);
                    break;
                case IN_COMMENT:
                    advanceComment(token);
                    break;
                case IN_PRAGMA:
                    advancePragma(token);
                    break;
                case IN_DOUBLE:
                    advanceLiteral(token, '"');
                    break;
                case IN_SINGLE:
                    advanceLiteral(token, '\'');
                    break;
                default:
                    throw new IllegalStateException("Unexpected state: " + state);
            }
        }

        if (buffer != null) {
            tokens.addAll(buffer);
            buffer = null;
        }

        Token[] result = new Token[tokens.size()];
        for (int pos = 0; pos < tokens.size(); pos++) {
            result[pos] = tokens.get(pos);
        }
        return result;
    }

    private void advanceText(TokenCharacter token) {
        tokens.add(token);
        switch (token.getCodepoint()) {
            case '{':
                if (input.hasNext()) {
                    peek = (TokenCharacter) input.next();
                }
                if (peek == null || peek.getCodepoint() != '[') {
                    commentdepth = 1;
                    state = State.IN_COMMENT;
                    buffer = new ArrayList<>();
                } else {
                    pragmadepth = 1;
                    state = State.IN_PRAGMA;
                    buffer = new ArrayList<>();
                }
                break;
            case '\'':
                state = State.IN_SINGLE;
                break;
            case '"':
                state = State.IN_DOUBLE;
                break;
            default:
                break;
        }
    }

    private void advanceComment(TokenCharacter token) {
        buffer.add(token);
        switch (token.getCodepoint()) {
            case '{':
                commentdepth++;
                break;
            case '}':
                commentdepth--;
                if (commentdepth == 0) {
                    state = State.IN_TEXT;
                    buffer = null;
                    peek = token;
                    mark = true;
                }
                break;
            default:
                break;
        }
    }

    private void advancePragma(TokenCharacter token) {
        buffer.add(token);
        switch (token.getCodepoint()) {
            case '{':
                pragmadepth++;
                break;
            case '}':
                pragmadepth--;
                if (pragmadepth == 0) {
                    state = State.IN_TEXT;
                    tokens.addAll(buffer);
                    buffer = null;
                }
                break;
            default:
                break;
        }
    }

    private void advanceLiteral(TokenCharacter token, char quote) {
        tokens.add(token);
        int cp = token.getCodepoint();

        if (cp == '\n') {
            state = State.IN_TEXT; // this is an error condition
            return;
        }

        if (cp == quote) {
            if (input.hasNext()) {
                peek = (TokenCharacter) input.next();
            }
            if (peek != null && peek.getCodepoint() == quote) {
                tokens.add(peek);
                peek = null;
            } else {
                state = State.IN_TEXT;
            }
        }
    }
}
