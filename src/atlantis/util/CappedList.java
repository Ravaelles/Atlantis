package atlantis.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

public class CappedList<T> extends AbstractCollection<T> {

    private final int size;

    private ArrayList<T> list;

    public CappedList(int size) {
        super();
        this.size = size;
        list = new ArrayList<>(size);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    public T get(int index) {
        return list.size() > index ? list.get(index) : null;
    }

    @Override
    public boolean add(T e) {
        if (list.size() >= size) {
            list.remove(this.size - 1);
        }
        list.add(0, e);
        return true;
    }
//    @Override
//    public boolean add(T e) {
//        if (deque.size() == size) {
//            deque.pollLast();
//        }
//        deque.addFirst(e);
//        return true;
//    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    public T first() {
        return get(0);
    }
}