package org.intelligentjava;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class HashSetTest {
    
    @Test
    public void testRemoveIndexFromHashCodesTable() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        
        final Method removeIndexFromHashCodesTable = HashSet.class.getDeclaredMethod("removeIndexFromHashTable", int.class);
        removeIndexFromHashCodesTable.setAccessible(true);
        
        final Field hashTable = HashSet.class.getDeclaredField("hashTable");
        hashTable.setAccessible(true);
        
        final Field dataField = HashSet.class.getDeclaredField("data");
        dataField.setAccessible(true);
        
        HashSet cache = new HashSet(5);
        
        byte[] hashTableValues = new byte[]{121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        dataField.set(cache, hashTableValues);
        // hashcodes for those values. changing hashcode algorithm might fail this test.
        Object[] indexesByHashCodeTableValues = new int[][] {null, null, new int[]{1, -1, -1}, new int[]{0, 2, -1}, null, null};
        hashTable.set(cache, indexesByHashCodeTableValues);
        
        removeIndexFromHashCodesTable.invoke(cache, 0);
        
        assert (Arrays.deepEquals((Object[])hashTable.get(cache), new Object[] {null, null, new int[]{1, -1, -1}, new int[]{2, -1, -1}, null, null}));
        
        removeIndexFromHashCodesTable.invoke(cache, 2);
        
        assert (Arrays.deepEquals((Object[])hashTable.get(cache), new Object[] {null, null, new int[]{1, -1, -1}, null, null, null}));
        
    }
    
    @Test
    public void testInsert() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        final Method insert = HashSet.class.getDeclaredMethod("insert", byte[].class);
        insert.setAccessible(true);
        
        final Field currentDataArrayIndex = HashSet.class.getDeclaredField("currentDataArrayIndex");
        currentDataArrayIndex.setAccessible(true);
        
        HashSet cache = new HashSet(3);
        
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        byte[] value3 = { 2, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                2 };
        byte[] value4 = { 3, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                3 };
        
        final Method getValue = HashSet.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        
        insert.invoke(cache, value1);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        Assert.assertArrayEquals(returnedValue1, value1);
        Assert.assertEquals(currentDataArrayIndex.getInt(cache), 1);

        insert.invoke(cache, value2);
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertArrayEquals(returnedValue2, value2);
        Assert.assertEquals(currentDataArrayIndex.getInt(cache), 2);
        
        insert.invoke(cache, value3);
        byte[] returnedValue3 = (byte[]) getValue.invoke(cache, new Integer(2));
        Assert.assertArrayEquals(returnedValue3, value3);
        Assert.assertEquals(currentDataArrayIndex.getInt(cache), 0);

        insert.invoke(cache, value4);
        byte[] returnedValue4 = (byte[]) getValue.invoke(cache, new Integer(0));
        Assert.assertArrayEquals(returnedValue4, value4);
        Assert.assertEquals(currentDataArrayIndex.getInt(cache), 1);
        
    }

    @Test
    public void testResizeBucket() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method resizeBucket = HashSet.class.getDeclaredMethod("resizeBucket", int[].class, int.class);
        resizeBucket.setAccessible(true);
        int[] smallerArray = { 0, 1, 2, 3 };
        int[] resizedArray = (int[]) resizeBucket.invoke(new HashSet(50), smallerArray, new Integer(2));
        int[] expectedArray = { 0, 1, 2, 3, -1, -1 };
        Assert.assertTrue(Arrays.equals(resizedArray, expectedArray));
    }

    @Test
    public void testGetValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method getValue = HashSet.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        HashSet cache = new HashSet(50);
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        
        String value1Str = Converter.convertToString(value1);
        String value2Str = Converter.convertToString(value2);
        
        cache.add(value1Str);
        cache.add(value2Str);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertArrayEquals(value1, returnedValue1);
        Assert.assertArrayEquals(value2, returnedValue2);
    }
    
    @Test
    public void testShrinkBucketIfNeeded() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method shrinkBucketIfNeeded = HashSet.class.getDeclaredMethod("shrinkBucketIfNeeded", int[].class);
        shrinkBucketIfNeeded.setAccessible(true);
        int[] bucket = {1, 2, -1, -1, -1};
        int[] shrinkedBucket = (int[]) shrinkBucketIfNeeded.invoke(new HashSet(5), bucket);
        int[] expectedBucket = {1, 2, -1};
        assert (Arrays.equals(shrinkedBucket, expectedBucket));
        
        int[] emptyBucket = {-1, -1, -1};
        int[] deletedBucket = (int[]) shrinkBucketIfNeeded.invoke(new HashSet(5), emptyBucket);
        assert (deletedBucket == null);
        
        int[] normalBucket = {1, 2, 3, -1, -1};
        int[] notChangedBucket = (int[]) shrinkBucketIfNeeded.invoke(new HashSet(5), normalBucket);
        assert (Arrays.equals(normalBucket, notChangedBucket));
    }

    @Test
    public void testSetValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method setValue = HashSet.class.getDeclaredMethod("setValue", int.class, byte[].class);
        setValue.setAccessible(true);
        HashSet cache = new HashSet(50);
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        setValue.invoke(cache, new Integer(0), value1);
        setValue.invoke(cache, new Integer(1), value2);
        
        final Method getValue = HashSet.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertArrayEquals(returnedValue1, value1);
        Assert.assertArrayEquals(returnedValue2, value2);
    }

    @Test
    public void testEmtySpaceIndex() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method emptySpaceIndex = HashSet.class.getDeclaredMethod("emptySpaceIndex", int[].class);
        emptySpaceIndex.setAccessible(true);
        int[] sampleBucket = { 0, 1, 2, 3, -1, -1 };
        int emptySpace = (Integer) emptySpaceIndex.invoke(new HashSet(50), sampleBucket);
        Assert.assertEquals(emptySpace, 4);
        int[] fullBucket = { 0, 1, 2, 3 };
        int emptySpaceNotFound = (Integer) emptySpaceIndex.invoke(new HashSet(50), fullBucket);
        Assert.assertEquals(emptySpaceNotFound, -1);
    }
    
    @Test
    public void testLastElementIndex() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method lastElementIndex = HashSet.class.getDeclaredMethod("lastElementIndex", int[].class);
        lastElementIndex.setAccessible(true);
        int[] sampleBucket = { 0, 1, 2, -1, -1, -1 };
        int emptySpace = (Integer) lastElementIndex.invoke(new HashSet(50), sampleBucket);
        Assert.assertEquals(emptySpace, 2);
        int[] fullBucket = { 0, 1, 2, 3 };
        int emptySpaceNotFound = (Integer) lastElementIndex.invoke(new HashSet(50), fullBucket);
        Assert.assertEquals(emptySpaceNotFound, 3);
    }

    @Test
    public void testSize() {
        int size = 5;
        HashSet cache = new HashSet(size);
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        Assert.assertEquals(cache.size(), 2);
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        cache.add(UUID.randomUUID().toString().replaceAll("-", ""));
        Assert.assertEquals(cache.size(), 5);
    }

    @Test
    public void testContains() {
        int size = 5;
        HashSet cache = new HashSet(size);
        String value1 = UUID.randomUUID().toString().replaceAll("-", "");
        String value2 = UUID.randomUUID().toString().replaceAll("-", "");
        
        cache.add(value2);
        Assert.assertFalse(cache.contains(value1));
        Assert.assertTrue(cache.contains(value2));
    }

    @Test
    public void testClear() {
        int size = 5;
        HashSet cache = new HashSet(size);
        String value1 = UUID.randomUUID().toString().replaceAll("-", "");
        String value2 = UUID.randomUUID().toString().replaceAll("-", "");
        
        cache.add(value1);
        cache.add(value2);
        cache.clear();
        Assert.assertEquals(cache.size(), 0);
        Assert.assertEquals(cache.toArray().length, 0);
        Assert.assertFalse(cache.contains(value1));
        Assert.assertFalse(cache.contains(value2));
    }

    @Test
    public void testToArray() {
        int size = 5;
        HashSet cache = new HashSet(size);
        String value1 = UUID.randomUUID().toString().replaceAll("-", "");
        cache.add(value1);
        Assert.assertArrayEquals((byte[]) cache.toArray()[0], Converter.convertToArray(value1));
        Assert.assertEquals(cache.toArray().length, 1);
    }

    /**
     * Fill cache and then test its behavior after it starts adding elements
     * from start again.
     */
    @Test
    public void testPutUntilFilled() {
        byte[] firstHash = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] firstHashAfterRestart = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128, -128, -128 };
        
        String firstHashStr = Converter.convertToString(firstHash);
        String firstHashAfterRestartStr = Converter.convertToString(firstHashAfterRestart);
        
        int size = 50;
        HashSet cache = new HashSet(size);
        cache.add(firstHashStr);
        Random random = new Random();
        while (cache.size() < size) {
            byte[] randomBytes = new byte[16];
            random.nextBytes(randomBytes);
            // make sure random bytes not equal to
            // bytes we want to insert right after restart.
            if (!Arrays.equals(randomBytes, firstHashAfterRestart) && !Arrays.equals(randomBytes, firstHash))
                cache.add(Converter.convertToString(randomBytes));
        }
        Assert.assertTrue(cache.contains(firstHashStr));
        cache.add(firstHashAfterRestartStr);
        // make sure next firstHashAfterRestart value replaced firstHash
        Assert.assertTrue(cache.contains(firstHashAfterRestartStr));
        // make sure firstHash is not in the cache anymore
        Assert.assertFalse(cache.contains(firstHashStr));
        // make sure we can add first hash again
        Assert.assertTrue(cache.add(firstHashStr));
    }

    @Test
    public void testPutSame() {
        HashSet cache = new HashSet(50);
        String hashAsString = "112233445566778899aabbccddeefff0";
        Assert.assertTrue(cache.add(hashAsString));
        Assert.assertFalse(cache.add(hashAsString));
        Assert.assertEquals(cache.size(), 1);
    }

}

