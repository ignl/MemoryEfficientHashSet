package org.intelligentjava;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This HashSet is more memory efficient than the standard java implementation. It converts hexstrings from string to byte array format 
 * and keeps byte arrays (by default size is 16 bytes) as values. Those strings must represent encoded hash value so they can contain only 
 * characters [a-f0-9] - e.g SHA256 hashes.
 * 
 */
public class HashSet implements Set<String> {

    private static final int BUCKET_SIZE = 3;

    private static final int BUCKET_RESIZE_BY = 2;

    private static final int BYTE_ARRAY_VALUE_SIZE = 16;

    /**
     * Number of elements in hashTable. 
     * Table size is number of elements multiplied with length of one element.
     * 
     */
    private int numberOfElementsInTable;

    /**
     * Is true when hashTable is full, and is filling hash values from start
     */
    private boolean isFilled = false;
    
    /**
     * Length of one element.
     */
    private int valueSize;
    
    /**
     * Index where next value will be put in hash table. After reaching end of
     * hash table it restarts to 0.
     */
    private int currentHashTableIndex;

    /**
     * Hash table where all hash values(byte arrays) are kept.
     */
    private byte[] hashTable;

    /**
     * Hash codes table, where each index represents hash code. Each field saves
     * bucket of indexes (array of integers), those are indexes of {@link hashTable}.
     * So this table has pointers where values with provided hash code
     * are kept in hash table.
     */
    private Object[] indexesByHashCodeTable;

    /**
     * Constructor. Because no length of value is provided it assumes that length is 16.
     * @param size Maximum number of elements in cache.
     */
    public HashSet(int size) {
        this(size, BYTE_ARRAY_VALUE_SIZE);
    }
    
    /**
     * Constructor.
     * 
     * @param size Maximum number of elements in cache.
     * @param valueSize Value length.
     */
    public HashSet(int size, int valueSize) {
        super();
        this.valueSize = valueSize;
        this.numberOfElementsInTable = size;
        indexesByHashCodeTable = new Object[numberOfElementsInTable];
        int tableSize = size*valueSize;
        hashTable = new byte[tableSize];
    }
    
    /**
     * Add value to 
     */
    public boolean add(String s) {
    	// convert to byte array first
        byte[] hashValue = Converter.convertToArray(s);
        if (contains(hashValue)) {
            return false;
        }
        insert(hashValue);
        return true;
    }
    
    /**
     * Public method for search operation that takes in string.
     */
    public boolean contains(Object o) {
        String hashValue = (String)o;
        return contains(Converter.convertToArray(hashValue));
    }
    

    /**
     * Finds out if there is same hash value in hash table.
     * 
     * @param hashValue
     *            Hash value which we compare with other values with same
     *            hashCode.
     * @return true if same hash value in hash table is found and false
     *         otherwise.
     */
    private boolean contains(byte[] o) {
        int hashCode = hashCode(o);
        if (indexesByHashCodeTable[hashCode] != null) {
            int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
            int i = 0;
            while (i < indexesBucket.length && indexesBucket[i] != -1) {
                if (Arrays.equals(o, getValue(indexesBucket[i]))) {
                    return true;
                }
                i++;
                if (i >= indexesBucket.length) {
                    indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                    indexesByHashCodeTable[hashCode] = indexesBucket;
                }
            }
        }
        return false;
    }
    
    /**
     * Clears cache.
     */
    public void clear() {
        isFilled = false;
        Arrays.fill(indexesByHashCodeTable, null);
        currentHashTableIndex = 0;
    }
    
    /**
     * Return array of hashes which are kept in cache.
     * 
     * @return Array of hashes represented as String.
     */
    public Object[] toArray() {
        int numberOfElements = isFilled ? numberOfElementsInTable : currentHashTableIndex;
        Object[] hashes = new Object[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            byte[] hashValue = getValue(i);
            if (hashValue != null) {
                hashes[i] = hashValue;
            }
        }
        return hashes;
    }
    
    /**
     * Returns the number of elements in this set.
     * 
     * @return Number of elements.
     */
    public int size() {
        return isFilled ? numberOfElementsInTable : currentHashTableIndex;
    }
    
    /**
     * Gets value by index from hashTable.
     * 
     * @param index Value index in hashTable.
     * @return Value.
     */
    private byte[] getValue(int index) {
        byte[] value = new byte[valueSize];
        System.arraycopy(hashTable, index*valueSize, value, 0, valueSize);
        return value;
    }
    
    /**
     * Sets value by index to hashTable.
     * 
     * @param index Index where value should be put.
     * @param value Value to set.
     */
    private void setValue(int index, byte[] value) {
        System.arraycopy(value, 0, hashTable, index*valueSize, valueSize);
    }
    

    /**
     * Inserts hash value in {@link hashTable}. It also inserts index of hash
     * table to {@link hashCodesTable}, so the value could be easily found by
     * its hash code. Of course removes old index from {@link hashCodesTable}
     * before inserting new. ({@link hashCodesTable} will always have index i
     * 
     * @param hashValue
     *            Value to insert.
     */
    private void insert(byte[] hashValue) {

        if (isFilled)
            removeIndexFromHashCodesTable(currentHashTableIndex);

        int hashCode = hashCode(hashValue);
        setValue(currentHashTableIndex, hashValue);

        if (indexesByHashCodeTable[hashCode] == null) {
            int[] newIndexesBucket = new int[BUCKET_SIZE];
            Arrays.fill(newIndexesBucket, -1);
            indexesByHashCodeTable[hashCode] = newIndexesBucket;
            newIndexesBucket[0] = currentHashTableIndex;
        } else {
            int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
            int index = emptySpaceIndex(indexesBucket);
            // if bucket is full resize it
            if (index == -1) {
                indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                indexesByHashCodeTable[hashCode] = indexesBucket;
                index = emptySpaceIndex(indexesBucket);
            }
            indexesBucket[index] = currentHashTableIndex;
        }
        currentHashTableIndex++;
        if (currentHashTableIndex >= numberOfElementsInTable) {
            currentHashTableIndex = 0;
            isFilled = true;
        }
    }

    /**
     * Removes index from indexesByHashCodeTable. Finds value in hash table,
     * calculates its hash code, finds index in bucket
     * indexesByHashCodeTable[hashcode] and removes that index.
     * 
     * @param index
     *            Index to remove.
     */
    private void removeIndexFromHashCodesTable(int index) {
        int hashCode = hashCode(getValue(index));
        int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
        if (indexesBucket != null) {
            int i = 0;
            while ((i < indexesBucket.length) && (indexesBucket[i] != -1 && indexesBucket[i] != index)) {
                i++;
            }
            if (i == indexesBucket.length || indexesBucket[i] == index) {
                int lastElementIndex = lastElementIndex(indexesBucket);
                // if bucket is full take last element
                if (lastElementIndex == -1) {
                    lastElementIndex = indexesBucket.length - 1;
                }
                indexesBucket[i] = indexesBucket[lastElementIndex];
                indexesBucket[lastElementIndex] = -1;
                indexesBucket = shrinkBucketIfNeeded(indexesBucket);
                indexesByHashCodeTable[hashCode] = indexesBucket;
            }
        }
    }

    /**
     * When removing index from {@link indexesByHashCodeTable} sometimes it is
     * needed to shrink the bucket because it was big, and a lot of indexes were
     * removed from it. This method do that to save memory. Also if bucket
     * becomes empty it is removed.
     * 
     * @param bucket
     *            Array of indexes.
     * @return Null if bucket is empty and should be removed, and smaller array
     *         with same values if bucket had to be shrinked. If nothing had to
     *         be done returns same bucket.
     */
    private int[] shrinkBucketIfNeeded(int[] bucket) {
        if (bucket.length > BUCKET_SIZE) {
            int freeSpace = 0;
            for (int i = 0; i < bucket.length; i++) {
                if (bucket[i] == -1) {
                    freeSpace++;
                }
            }
            if (freeSpace >= (BUCKET_RESIZE_BY + 1)) {
                return resizeBucket(bucket, -1 * BUCKET_RESIZE_BY);
            }
        } else {
            if (isBucketEmty(bucket)) {
                return null;
            }
        }
        return bucket;
    }

    /**
     * Checks is bucket is empty.
     * 
     * @param bucket
     *            Array to check.
     * @return True if bucket is empty and false if it has at least one value.
     */
    private boolean isBucketEmty(int[] bucket) {
        for (int i = 0; i < bucket.length; i++) {
            if (bucket[i] != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Searches bucket for a empty space index in bucket.
     * 
     * @param bucket
     *            Bucket to check.
     * @return Index of the last element in indexes bucket. If bucket is full
     *         returns -1.
     */
    private int emptySpaceIndex(int[] bucket) {
        try {
            int index = 0;
            while (bucket[index] != -1)
                index++;
            return index;
        } catch (IndexOutOfBoundsException e) {
            return -1; // bucket is full
        }
    }

    /**
     * Searches last element index in bucket.
     * 
     * @param bucket
     *            Bucket to check.
     * @return Last element index.
     */
    private int lastElementIndex(int[] bucket) {
        int index = 0;
        while (index < bucket.length && bucket[index] != -1)
            index++;
        return index - 1;
    }

    /**
     * Resize bucket of indexes.
     * 
     * @param bucket
     *            Full bucket that need resize.
     * @return Bucket bigger size with all new fields filled with -1, or bucket
     *         smaller size.
     */
    private int[] resizeBucket(int[] bucket, int sizeDifference) {
        int length = bucket.length;
        int newLength = length + sizeDifference;
        int[] resizedBucket = new int[newLength];
        System.arraycopy(bucket, 0, resizedBucket, 0, sizeDifference < 0 ? newLength : length);
        if (sizeDifference > 0) {
            for (int i = bucket.length; i < bucket.length + sizeDifference; i++) {
                resizedBucket[i] = -1;
            }
        }
        return resizedBucket;
    }

    /**
     * Prime numbers used for hashcode calculation.
     */
    private static int[] PRIMES = { 244217, 244219, 244243, 244247, 244253, 244261, 244291, 244297, 244301, 244303,
            244313, 244333, 244339, 244351, 244357, 244367 };
    
    /**
     * Calculates hash code for provided hash value represented as byte array.
     * 
     * @param hashValue
     *            Sequence of bytes that represents hash value.
     * @return HashCode.
     */
    private int hashCode(byte[] value) {
        int hash = 1;
        for (int i = 0; i < value.length; i++) {
            int unsignedByte = value[i] < 0 ? ((int) (-1 * value[i])) : value[i];
            int primeNumberIndex = i < PRIMES.length ? i : i % PRIMES.length;
            hash += unsignedByte * PRIMES[primeNumberIndex];
        }
        return hash % numberOfElementsInTable;
    }

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
    
}