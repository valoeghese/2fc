package valoeghese.twofc.util;

import valoeghese.twofc.util.maths.MathsUtils;
import valoeghese.twofc.util.maths.Vec2f;

import javax.annotation.Nullable;

/**
 * Direct-mapped cache that stores 64 values (8x8 -> 3 bits,3 bits).
 */
public class FastJitteredGridObjCache64<T> {
    public FastJitteredGridObjCache64(FastJitteredGridObjCache64.Sampler<T> wrapped) {
        this.wrapped = wrapped;
    }

    private final Point[] storedKeys = new Point[64];
    private final Object[] storedValues = new Object[64];
    private final FastJitteredGridObjCache64.Sampler<T> wrapped;

    @SuppressWarnings("unchecked")
    public T sample(Vec2f pos) {
        int x = MathsUtils.floor(pos.getX());
        int y = MathsUtils.floor(pos.getY());

        // get index: lowest 3 bits of parameters
        int index = ((x & 0x7) << 3) | (y & 0x7);
        // check key match
        @Nullable Point stored = this.storedKeys[index];
        if (stored != null && stored.x==x && stored.y==y) {
            // cache hit
            return (T)this.storedValues[index];
        } else {
            // cache miss
            T value = this.wrapped.sample(x, y, pos);
            this.storedValues[index] = value;
            this.storedKeys[index] = new Point(x, y);
            return value;
        }
    }

    private record Point(int x, int y) {}

    @FunctionalInterface
    public interface Sampler<T> {
        T sample(int x, int y, Vec2f vec2f);
    }
}
