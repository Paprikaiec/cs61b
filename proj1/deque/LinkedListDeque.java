package deque;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class LinkedListDeque<T> implements Iterable<T>, deque.Deque<T> {
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private ListNode wizNode;

        public LinkedListDequeIterator() {
            wizNode = sentinel;
        }

        public boolean hasNext() {
            return wizNode.next != sentinel;
        }

        public T next() {
            T item = wizNode.next.item;
            wizNode = wizNode.next;
            return item;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (!(o instanceof Deque)) return false;

        LinkedListDeque<T> list_o = (LinkedListDeque<T>) o;
        if (list_o.size() != this.size()) return false;

        LinkedListDequeIterator thisIterator = new LinkedListDequeIterator();
        for (T item : list_o) {
            if (thisIterator.next() != item) return false;
        }

        return true;
    }

    private class ListNode {
        public ListNode prev;
        public T item;
        public ListNode next;

        public ListNode() {
            prev = null;
            item = null;
            next = null;
        }

        public ListNode(ListNode p, T x, ListNode n) {
            prev = p;
            item = x;
            next = n;
        }
    }

    private ListNode sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new ListNode();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public void addFirst(T item) {
        ListNode node = new ListNode(sentinel, item, sentinel.next);
        sentinel.next.prev = node;
        sentinel.next = node;
        size += 1;
    }

    public void addLast(T item) {
        ListNode node = new ListNode(sentinel.prev, item, sentinel);
        sentinel.prev.next = node;
        sentinel.prev = node;
        size += 1;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        ListNode node = sentinel.next;
        while(node != sentinel) {
            System.out.print(node.item.toString() + ' ');
            node = node.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) return null;

        T item = sentinel.next.item;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size -= 1;
        return item;
    }

    public T removeLast() {
        if (size == 0) return null;

        T item = sentinel.prev.item;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size -= 1;
        return item;
    }

    public T get(int index) {
        if ((index >= size) || (index < 0)) return null;
        ListNode node = sentinel;
        for (int i = 0; i <= index; i++) node = node.next;
        return node.item;
    }

    public T getRecursive(int index) {
        if ((index >= size) || (index < 0)) return null;
        return getRecursive(sentinel.next, index);
    }

    private T getRecursive(ListNode node, int index) {
        if (index == 0) return node.item;
        return getRecursive(node.next, index - 1);
    }

}
