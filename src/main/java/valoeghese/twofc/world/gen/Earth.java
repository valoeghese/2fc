package valoeghese.twofc.world.gen;

import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.util.noise.Noise;

import java.util.Random;

/**
 * World generator for earth.
 */
public class Earth extends WorldGen {
    public Earth(long seed) {
        super(seed);
        this.noise = new Noise(new Random(seed));
    }

    private final Noise noise;
    private double sampleNoise(double x, double z) {
        return this.noise.sample(x, z);
    }

    private int rootRegionType(int x, int z) {
        double distSqr = x * x + z * z;
        double falloff = 2 - 0.5 * distSqr;

        double noise = this.sampleNoise(x / 4.0, z / 4.0);
        double connector = this.sampleNoise(x / 2.0, z / 2.0);
        double k = 1 - Math.abs(2 * noise) + Math.max(0, falloff) + 0.1 * connector;
        if (k > 0.35) {
            return 0;
        } else {
            return 1;
        }
    }

    private int regionType(Vec2f pos) {
        int x = (int) pos.getX();
        int z = (int) pos.getY();

        int centre = rootRegionType(x, z);
        if (centre == 0) {
            return 0;
        }

        // check neighbours (names from current sun direction)
        int east = rootRegionType(x, z - 1);
        int north = rootRegionType(x - 1, z);
        int west = rootRegionType(x, z + 1);
        int south = rootRegionType(x + 1, z);

        if (east == 0 || north == 0 || west == 0 || south == 0) {
            return (pos.id() & 1) == 0 ? 2 : 1;
        } else {
            return 1;
        }
    }

    @Override
    protected double sampleHeight(double x, double z) {
        // Stage one: sample continent shape
//        double continent = 43 + 20 * this.sampleNoise((x / 810.0) - 0.3, (z / 810.0) - 0.3);

        // Stage two: sample mountains and hills
//        double mountains = this.sampleMountains(x, z);

//        double hills = 20 * this.sampleNoise(x / 90.0, z / 90.0) + 12 * this.sampleNoise(x / 32.0, z / 32.0);

        // Stage three: bias mountains and hills
//        double bias = 0.5 + 0.5 * this.sampleNoise(x / 600.0, (z / 600.0) - 1);
//        return continent + (bias * hills) + ((1.0 - bias) * mountains);
        return 52;
    }

    // For debug
    public static final class Debug {
        public Debug(long seed) {
            this.earth = new Earth(seed);
            this.seed = seed;
        }

        private final Earth earth;
        private final long seed;

        public long getSeed() {
            return seed;
        }

        // Exposed methods for debug

        public int regionType(Vec2f pos) {
            return this.earth.regionType(pos);
        }
    }
}
