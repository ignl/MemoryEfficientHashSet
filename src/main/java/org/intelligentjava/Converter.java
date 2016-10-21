package org.intelligentjava;

import java.text.StringCharacterIterator;

public class Converter {
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] convertToArray(String magicNumberString) {
        int byteCount = magicNumberString.length() / 2 + magicNumberString.length() % 2;
        byte[] decoded = new byte[byteCount];
        StringCharacterIterator it = new StringCharacterIterator(magicNumberString);
        int i = 0;
        char firstHex = it.first();
        char secondHex = it.next();
        while (firstHex != '\uffff' && secondHex != '\uffff') {
            int hashByte = Character.digit(firstHex, 16);
            hashByte <<= 4;
            decoded[i] = (byte)(hashByte += Character.digit(secondHex, 16));
            ++i;
            firstHex = it.next();
            secondHex = it.next();
        }
        return decoded;
    }

    public static String convertToString(byte[] magicNumberArray) {
        StringBuilder result = new StringBuilder(magicNumberArray.length * 2);
        int i = 0;
        while (i < magicNumberArray.length) {
            byte b = magicNumberArray[i];
            result.append(DIGITS[(b & 240) >> 4]);
            result.append(DIGITS[b & 15]);
            ++i;
        }
        return result.toString();
    }
}