package org.intelligentjava;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This HashSet is more memory efficient than the standard java implementation. It converts hexstrings from string to byte array format 
 * and keeps byte arrays (by default size is 16 bytes) as values. Good ONLY for the hexadecimal strings e.g. SHA256 hashes, uuids etc.
 * 
 */
public class HashSet implements Set<String> {

	/** Initial bucket size. */
    private static final int BUCKET_SIZE = 3;

    /** How much to resize bucket when needed. */
    private static final int BUCKET_RESIZE_BY = 2;

    /** Default length of the value */
    private static final int BYTE_ARRAY_VALUE_SIZE = 16;

    /**
     * Number of elements in hashTable and in data array (same). 
     * Data array size equals number of elements multiplied by the length of one element.
     */
    private int nbOfElements;

    /**
     * Is true when hashTable is full, and is inserting hash values restarts from index 0. 
     * This flag is required to know that old values in data array is overwritten so hash table must be updated too.
     */
    private boolean isFilled = false;
    
    /**
     * Array length of one element.
     */
    private int valueSize;
    
    /**
     * Index where the next value will be put in a data array. After reaching the end of array it restarts to 0.
     */
    private int currentDataArrayIndex;

    /**
     * Array where all the byte[] values are kept.
     */
    private byte[] data;

    /**
     * Hash codes table. The first array is a hash code and the second array is a bucket for hash code which has 
     * pointer indexes to the {@link data} array where the real value is.
     */
    private int[][] hashTable;

    /**
     * Constructor. Default length for entry is set to 16.
     * 
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
        this.nbOfElements = size;
        hashTable = new int[nbOfElements][];
        int tableSize = size*valueSize;
        data = new byte[tableSize];
    }
    
    /**
     * Add value to hash table. Converts hexadecimal string to byte[] array first.
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
     * Public method for search operation.
     */
    public boolean contains(Object o) {
        String hashValue = (String)o;
        return contains(Converter.convertToArray(hashValue));
    }
    

    /**
     * Finds out if the same value is already in a hash table.
     * 
     * @param element
     *            element which we compare with other values with same
     *            hashCode.
     * @return true if same hash value in hash table is found and false
     *         otherwise.
     */
    private boolean contains(byte[] element) {
        int hashCode = hashCode(element);
        if (hashTable[hashCode] != null) {
            int[] indexesBucket = (int[]) hashTable[hashCode];
            int i = 0;
            while (i < indexesBucket.length && indexesBucket[i] != -1) {
                if (Arrays.equals(element, getValue(indexesBucket[i]))) {
                    return true;
                }
                i++;
                if (i >= indexesBucket.length) {
                    indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                    hashTable[hashCode] = indexesBucket;
                }
            }
        }
        return false;
    }
    
    /**
     * Clears hash table.
     */
    public void clear() {
        isFilled = false;
        Arrays.fill(hashTable, null);
        currentDataArrayIndex = 0;
    }
    
    /**
     * Returns an array of values which are kept in cache.
     * 
     * @return Array of hashes represented as String.
     */
    public Object[] toArray() {
        int numberOfElements = isFilled ? nbOfElements : currentDataArrayIndex;
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
        return isFilled ? nbOfElements : currentDataArrayIndex;
    }
    
    /**
     * Gets value by index from data array.
     * 
     * @param index Value index in hashTable.
     * @return Value.
     */
    private byte[] getValue(int index) {
        byte[] value = new byte[valueSize];
        System.arraycopy(data, index*valueSize, value, 0, valueSize);
        return value;
    }
    
    /**
     * Sets value by index to data array.
     * 
     * @param index Index where value should be put.
     * @param value Value to set.
     */
    private void setValue(int index, byte[] value) {
        System.arraycopy(value, 0, data, index*valueSize, valueSize);
    }
    

    /**
     * Inserts  value to {@link data} array and pointers to it to {@link hashTable}.
     * 
     * @param value
     *            Value to insert.
     */
    private void insert(byte[] value) {

    	// once hash table is full new value overwrites old value in data array, for that reason pointer in hashTable to old value is removed
        if (isFilled)
            removeIndexFromHashTable(currentDataArrayIndex);

        int hashCode = hashCode(value);
        setValue(currentDataArrayIndex, value);

        if (hashTable[hashCode] == null) {
            int[] newIndexesBucket = new int[BUCKET_SIZE];
            Arrays.fill(newIndexesBucket, -1);
            hashTable[hashCode] = newIndexesBucket;
            newIndexesBucket[0] = currentDataArrayIndex;
        } else {
            int[] indexesBucket = (int[]) hashTable[hashCode];
            int index = emptySpaceIndex(indexesBucket);
            // if bucket is full resize it
            if (index == -1) {
                indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                hashTable[hashCode] = indexesBucket;
                index = emptySpaceIndex(indexesBucket);
            }
            indexesBucket[index] = currentDataArrayIndex;
        }
        currentDataArrayIndex++;
        if (currentDataArrayIndex >= nbOfElements) {
            currentDataArrayIndex = 0;
            isFilled = true;
        }
    }

    /**
     * Removes element index (or in other words pointer to data array) from {@link hashTable}. Gets value from data array by index,
     * calculates its hash code, finds index in a bucket (hashTable[hashCode]) and removes that index.
     * 
     * @param index
     *            Index to remove.
     */
    private void removeIndexFromHashTable(int index) {
        int hashCode = hashCode(getValue(index));
        int[] indexesBucket = (int[]) hashTable[hashCode];
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
                hashTable[hashCode] = indexesBucket;
            }
        }
    }

    /**
     * When removing index from {@link hashTable} sometimes it is
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
     * Searches bucket for an empty space index in bucket.
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
     * Gets index of the last element in a bucket.
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
     * Prime numbers used for hash code calculation.
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
        return hash % nbOfElements;
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