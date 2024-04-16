package deque;

public class ArrayDeque<T> {
    private int first;
    private T[] items;
    private int size;

    public ArrayDeque() {
        first = 0;
        items = (T[]) new Object[8];
        size = 0;
    }

    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        for (int i = 0; i < size; i++) {
            a[i] = items[(first + i) % items.length];
        }
        items = a;
    }

    public void addFirst(T item) {
        if (size == items.length) {
            resize(2 * items.length);
        }

        first = (first - 1 + items.length) % items.length;
        items[first] = item;
        size += 1;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(2 * items.length);
        }

        items[(first + size) % items.length] = item;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[(first + i) % items.length].toString() + ' ');

        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) return null;

        if ((size < items.length / 4) && (size > 8)) {
            resize(items.length / 4);
        }

        T item = items[first];
        items[first] = null;
        first = (first + 1) % items.length;
        size -= 1;
        return item;
    }

    public T removeLast() {
        if (size == 0) return null;

        if ((size < items.length / 4) && (items.length > 16)) {
            resize(items.length / 4);
        }

        T item = items[(first + size - 1 + items.length) % items.length];
        items[(first + size - 1 + items.length) % items.length] = null;
        size -= 1;
        return item;
    }

    public T get(int index) {
        if ((index >= size) || (index < 0)) return null;
        return items[(first + index) % items.length];
    }
}
