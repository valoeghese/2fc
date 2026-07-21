package valoeghese.twofc.util;

import javax.annotation.Nullable;

/**
 * Direct-mapped cache that stores 64 values (8x8 -> 3 bits,3 bits).
 */
public class FastObjCache64<T> {
    public FastObjCache64(FastObjCache64.Sampler<T> wrapped) {
        this.wrapped = wrapped;
    }

    private final Point[] storedKeys = new Point[64];
    private final Object[] storedValues = new Object[64];
    private final FastObjCache64.Sampler<T> wrapped;

    @SuppressWarnings("unchecked")
    public T sample(int x, int y) {
        // get index: lowest 3 bits of parameters
        int index = ((x & 0x7) << 3) | (y & 0x7);
        // check key match
        @Nullable Point stored = this.storedKeys[index];
        if (stored != null && stored.x==x && stored.y==y) {
            // cache hit
            return (T)this.storedValues[index];
        } else {
            // cache miss
            T value = this.wrapped.sample(x, y);
            this.storedValues[index] = value;
            this.storedKeys[index] = new Point(x, y);
            return value;
        }
    }

    private record Point(int x, int y) {}

    @FunctionalInterface
    public interface Sampler<T> {
        T sample(int x, int z);
    }
}
