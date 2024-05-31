package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private double loadFactor;
    private HashSet<K> keySet = new HashSet<>();

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        if (initialSize < 1 || maxLoad < 0) {
            throw new IllegalArgumentException();
        }

        buckets = createTable(initialSize);
        size = 0;
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    private int hashCode(K key, int length) {
        return Math.floorMod(key.hashCode(), length);
    }

    private int hashCode(K key) {
        return hashCode(key, buckets.length);
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }

        size = 0;
    }

    private Node find(K key) {
        Collection<Node> bucket =  buckets[hashCode(key)];
        if (bucket == null) {
            return null;
        }

        for (Node node: bucket) {
            if (node.key.equals(key)) {
                return node;
            }
        }

        return null;
    }

    @Override
    public boolean containsKey(K key) {
        return find(key) != null;
    }

    @Override
    public V get(K key) {
       Node node = find(key);
       return (node == null) ? null : node.value;
    }

    @Override
    public int size(){
        return size;
    }

    private void expand(int expandSize) {
        Collection<Node>[] expandBuckets = createTable(expandSize);
        for (Collection<Node> bucket : buckets) {
            if (bucket != null) {
                int expandInd = hashCode(bucket.iterator().next().key, expandSize);
                expandBuckets[expandInd] = bucket;
            }
        }

        buckets = expandBuckets;
    }
    @Override
    public void put(K key, V value){
        if (size + 1 >= Math.floor(buckets.length * loadFactor)) {
            expand(2 * buckets.length);
        }

        int putInd = hashCode(key);
        if (buckets[putInd] == null) {
            buckets[putInd] = createBucket();
        }

        for(Node node : buckets[putInd]) {
            if (node.key.equals(key)) {
                buckets[putInd].remove(node);
                size -= 1;
                break;
            }
        }

        size += 1;
        buckets[putInd].add(createNode(key, value));
        keySet.add(key);
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();

    }
 }
