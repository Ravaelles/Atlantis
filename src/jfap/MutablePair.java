package jfap;

import java.util.Objects;

// Thanks to @adakitesystems for this class
public class MutablePair<K, V> {

    public K first;
    public V second;

    public MutablePair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "Pair{" + "first=" + first + ", second=" + second + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MutablePair<?, ?> mutablePair = (MutablePair<?, ?>) o;

        if (first != null ? !first.equals(mutablePair.first) : mutablePair.first != null) {
            return false;
        }
        if (second != null ? !second.equals(mutablePair.second) : mutablePair.second != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int first = this.first == null ? 0 : this.first.hashCode();
        final int second = this.second == null ? 0 : this.second.hashCode();

        return Objects.hashCode(new int[]{first, second});
    }
}
