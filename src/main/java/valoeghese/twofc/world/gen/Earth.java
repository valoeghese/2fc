package valoeghese.twofc.world.gen;

import valoeghese.twofc.util.Face;
import valoeghese.twofc.util.FastJitteredGridObjCache64;
import valoeghese.twofc.util.FastObjCache64;
import valoeghese.twofc.util.maths.MathsUtils;
import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.util.maths.Vec2i;
import valoeghese.twofc.util.noise.Noise;
import valoeghese.twofc.world.kingdom.Voronoi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * World generator for earth.
 */
public class Earth extends WorldGen {
    public Earth(long seed) {
        super(seed);
        this.seed = seed;
        this.noise = new Noise(new Random(seed));
    }

    private final long seed;
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

    // Public for debug
    // (terrain) type: 0 = ocean, 1 = mountains, 2 = floodplains
    public record RegionInfo(int type, @Nullable Face outflow1, @Nullable Face outflow2) {
    }

    private RegionInfo regionType(int x, int z) {
        int centre = rootRegionType(x, z);
        if (centre == 0) {
            return new RegionInfo(0, null, null);
        }

        // check neighbours (names from current sun direction)
        int east = rootRegionType(x, z - 1);
        int north = rootRegionType(x - 1, z);
        int west = rootRegionType(x, z + 1);
        int south = rootRegionType(x + 1, z);

        if (east == 0 || north == 0 || west == 0 || south == 0) {
            int hash = Voronoi.random2(x, z, (int)(this.seed & 0xFFFFFFFFL), -1);
            int type = (hash & 1) == 0 ? 2 : 1;
            // pick outflows
            List<Face> options = new ArrayList<>();
            if (east == 0) options.add(Face.EAST);
            if (north == 0) options.add(Face.NORTH);
            if (west == 0) options.add(Face.WEST);
            if (south == 0) options.add(Face.SOUTH);
            Random random = new Random(hash);

            Face selected;
            Face selected2;
            if (type == 2) {
                selected = options.get(random.nextInt(options.size()));
                selected2 = options.get(random.nextInt(options.size()));
            } else {
                selected = options.remove(random.nextInt(options.size()));
                selected2 = options.isEmpty() ? null : options.get(random.nextInt(options.size()));
            }

            return new RegionInfo(type, selected, selected2);
        } else {
            return new RegionInfo(1, null, null);
        }
    }
    private final FastObjCache64<RegionInfo> regionTypeCache = new FastObjCache64<>(this::regionType);

    private RegionInfo kruskal(int x, int z, int offset, FastObjCache64<RegionInfo> cache) {
        RegionInfo regionType = cache.sample(x, z);

        if (regionType.type != 1 || regionType.outflow1 != null) {
            return regionType;
        }

        RegionInfo north = cache.sample(x - 1, z);
        RegionInfo east = cache.sample(x, z - 1);
        RegionInfo south = cache.sample(x + 1, z);
        RegionInfo west = cache.sample(x, z + 1);

        List<Face> options = new ArrayList<>();
        if (north.outflow1 != null) options.add(Face.NORTH);
        if (east.outflow1 != null) options.add(Face.EAST);
        if (south.outflow1 != null) options.add(Face.SOUTH);
        if (west.outflow1 != null) options.add(Face.WEST);

        if (options.isEmpty()) {
            return regionType;
        }

        Random random = new Random(Voronoi.random2(x, z, (int)(this.seed & 0xFFFFFFFFL) + offset, -1));
        Face selected = options.get(random.nextInt(options.size()));
        return new RegionInfo(regionType.type, selected, null);
    }
    private final FastObjCache64<RegionInfo> kruskal1Cache = new FastObjCache64<>((x, z) -> kruskal(x, z, 1, this.regionTypeCache));
    private final FastObjCache64<RegionInfo> kruskal2Cache = new FastObjCache64<>((x, z) -> kruskal(x, z, 2, this.kruskal1Cache));

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

        public int regionType(int x, int z) {
            return this.earth.regionType(x, z).type;
        }

        public RegionInfo riverInfo(int x, int z) {
            return this.earth.kruskal2Cache.sample(x, z);
        }
    }
}
