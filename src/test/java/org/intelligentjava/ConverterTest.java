package org.intelligentjava;

import org.junit.Assert;
import org.junit.Test;

public class ConverterTest {
	
    @Test
    public void testHashToArray() {
        String hash = "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f";
        byte[] expectedArray = new byte[]{127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127};
        Assert.assertArrayEquals(Converter.convertToArray(hash), expectedArray);
        String hash2 = "00000000000000000000000000000000";
        byte[] expectedArray2 = new byte[16];
        Assert.assertArrayEquals((byte[])Converter.convertToArray(hash2), (byte[])expectedArray2);
    }

    @Test
    public void testHashToString() {
        byte[] hash = new byte[]{127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127, 127};
        String expectedString = "7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f7f";
        Assert.assertEquals((String)Converter.convertToString(hash), (String)expectedString);
        byte[] hash2 = new byte[16];
        String expectedString2 = "00000000000000000000000000000000";
        Assert.assertEquals((String)Converter.convertToString(hash2), (String)expectedString2);
    }
}