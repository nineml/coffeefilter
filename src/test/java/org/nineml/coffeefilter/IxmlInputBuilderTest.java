package org.nineml.coffeefilter;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeefilter.utils.IxmlInputBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.ParserAttribute;

public class IxmlInputBuilderTest {
    @Test
    public void testNothing() {
        String input = "S: 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
    }

    @Test
    public void testComment() {
        String input = "S: {ignore me} 'a'.";
        String output = "S: {} 'a'.";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("1", buffer[4].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("15", buffer[4].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("14", buffer[4].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testNestedComment() {
        String input = "S: {ignore me { and me } { and me too}} 'a'.";
        String output = "S: {} 'a'.";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("1", buffer[4].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("40", buffer[4].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("39", buffer[4].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testNestedCommentAndPragma() {
        String input = "S: {ignore me {[ and me ]} { and me too}} 'a'.";
        String output = "S: {} 'a'.";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("1", buffer[4].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("42", buffer[4].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("41", buffer[4].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testPragma() {
        String input = "S: {[ignore me]} 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testNestedPragma() {
        String input = "S: {[pragma {[ok]} {[and me too]}]} 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testNestedPragmaAndComment() {
        String input = "S: {[pragma {[ok]} {and me too}]} 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testPragmaInComment() {
        String input = "S: {nested {[ignore me]}} 'a'.";
        String output = "S: {} 'a'.";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("1", buffer[4].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("26", buffer[4].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("25", buffer[4].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testCommentInPragma() {
        String input = "S: {[pragma {don't ignore me}]} 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testBrokenComment() {
        String input = "S: {ignore me 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testBrokenPragma() {
        String input = "S: {[broken pragma 'a'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "0"));
        Assertions.assertEquals("0", buffer[3].getAttributeValue(ParserAttribute.OFFSET_NAME, "0"));
    }

    @Test
    public void testSingleQuotedComment() {
        String input = "S: 'a{not a comment'.";
        String output = input;
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
    }

    @Test
    public void testDoubleQuotedComment() {
        String input = "S: 'a{also not a comment}}}'.";
        String output = "S: 'a{also not a comment}}}'.";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
    }

    @Test
    public void testBrokenString() {
        String input = "S: 'a\n{comment}";
        String output = "S: 'a\n{}";
        Token[] buffer = IxmlInputBuilder.fromString(input);
        Assertions.assertEquals(output.length(), buffer.length);
        for (int pos = 0; pos < output.length(); pos++) {
            Assertions.assertEquals(output.charAt(pos), ((TokenCharacter) buffer[pos]).getCodepoint());
        }
    }

}
