package valoeghese.twofc.world.gen;

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
}
