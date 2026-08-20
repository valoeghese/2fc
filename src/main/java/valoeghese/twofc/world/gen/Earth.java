package valoeghese.twofc.world.gen;

import valoeghese.twofc.util.Face;
import valoeghese.twofc.util.FastObjCache64;
import valoeghese.twofc.util.Out;
import valoeghese.twofc.util.maths.MathsUtils;
import valoeghese.twofc.util.maths.Vec2f;
import valoeghese.twofc.util.noise.Noise;
import valoeghese.twofc.world.kingdom.Voronoi;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * World generator for earth.
 */
public class Earth extends WorldGen {
    public Earth(long seed) {
        super(seed);
        this.seed = seed;
        this.jgSeed = (int)(this.seed & 0xFFFFFFFFL);
        this.noise = new Noise(new Random(seed));
    }

    private final long seed;
    private final int jgSeed;
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
    // (terrain) type: 0 = ocean, 1 = mountains, 2 = floodplains, 3 = (coastal) mountains
    public record RegionInfo(int type, @Nullable Face outflow1, @Nullable Face outflow2) {
        public static final int OCEAN = 0;
        public static final int MOUNTAINS = 1;
        public static final int FLOODPLAIN = 2;
        public static final int COASTAL_MOUNTAINS = 3;

        public static boolean isMountains(int type) {
            return (type & 1) == 1;
        }
        public static boolean isCoastal(int type) {
            return (type & 2) == 2;
        }
    }

    private RegionInfo regionInfo(int x, int z) {
        int centre = rootRegionType(x, z);
        if (centre == 0) {
            return new RegionInfo(0, null, null);
        }

        // check neighbours (names from current sun direction)
        int east = rootRegionType(x, z - 1);
        int north = rootRegionType(x - 1, z);
        int west = rootRegionType(x, z + 1);
        int south = rootRegionType(x + 1, z);

        // RegionInfo.OCEAN == 0
        if (east == 0 || north == 0 || west == 0 || south == 0) {
            int hash = Voronoi.random2(x, z, this.jgSeed, 1);
            int type = hash == 0 ? RegionInfo.FLOODPLAIN : RegionInfo.COASTAL_MOUNTAINS;

            return new RegionInfo(type, null, null);
        } else {
            return new RegionInfo(1, null, null);
        }
    }
    private final FastObjCache64<RegionInfo> regionTypeWithoutFlowsCache = new FastObjCache64<>(this::regionInfo);

    private RegionInfo regionTypeWithFlows(int x, int z) {
        RegionInfo centre = regionTypeWithoutFlowsCache.sample(x, z);

        if (!RegionInfo.isCoastal(centre.type)) {
            return centre;
        }

        // pick outflows
        int east = regionTypeWithoutFlowsCache.sample(x, z - 1).type;
        int north = regionTypeWithoutFlowsCache.sample(x - 1, z).type;
        int west = regionTypeWithoutFlowsCache.sample(x, z + 1).type;
        int south = regionTypeWithoutFlowsCache.sample(x + 1, z).type;

        List<Face> options = new ArrayList<>();
        if (east == RegionInfo.OCEAN) options.add(Face.EAST);
        if (north == RegionInfo.OCEAN) options.add(Face.NORTH);
        if (west == RegionInfo.OCEAN) options.add(Face.WEST);
        if (south == RegionInfo.OCEAN) options.add(Face.SOUTH);

        int hash = Voronoi.random2(x, z, this.jgSeed, -1);
        Random random = new Random(hash);

        Face selected;
        Face selected2;
        if (centre.type == RegionInfo.FLOODPLAIN) {
            selected = options.get(random.nextInt(options.size()));
            selected2 = options.get(random.nextInt(options.size()));
        } else {
            // allow assumptions that only selected1 can flow into a land mass to survive.
            selected2 = options.remove(random.nextInt(options.size()));

            if (east == RegionInfo.FLOODPLAIN) options.add(Face.EAST);
            if (north == RegionInfo.FLOODPLAIN) options.add(Face.NORTH);
            if (west == RegionInfo.FLOODPLAIN) options.add(Face.WEST);
            if (south == RegionInfo.FLOODPLAIN) options.add(Face.SOUTH);

            selected = options.isEmpty() ? null : options.get(random.nextInt(options.size()));

            if (selected == null) {
                selected = selected2;
                selected2 = null;
            }
        }

        return new RegionInfo(centre.type, selected, selected2);
    }
    private final FastObjCache64<RegionInfo> regionTypeCache = new FastObjCache64<>(this::regionTypeWithFlows);

    private RegionInfo voronoiZoom(final int x, final int z, float zoom, FastObjCache64<RegionInfo> cache) {
        Vec2f centre = Voronoi.sampleVoronoi((float) x / zoom, (float)z / zoom, (int)(seed & (long)0xFFFFFFFF), 0.2f);
        return cache.sample(MathsUtils.floor(centre.getX()), MathsUtils.floor(centre.getY()));
    }

    private RegionInfo downgradeCoast(final int x, final int z, FastObjCache64<RegionInfo> cache) {
        RegionInfo centre = cache.sample(x, z);
        if (centre.type == RegionInfo.OCEAN || centre.type == RegionInfo.FLOODPLAIN) {
            return centre;
        }

        RegionInfo north = cache.sample(x - 1, z);
        RegionInfo south = cache.sample(x + 1, z);
        RegionInfo east = cache.sample(x, z - 1);
        RegionInfo west = cache.sample(x, z + 1);

        if (north.type == RegionInfo.OCEAN || south.type == RegionInfo.OCEAN
        || east.type == RegionInfo.OCEAN || west.type == RegionInfo.OCEAN) {
            int type = centre.type == RegionInfo.MOUNTAINS ? RegionInfo.COASTAL_MOUNTAINS : RegionInfo.FLOODPLAIN;
            return new RegionInfo(type, centre.outflow1, centre.outflow2);
        } else {
            return centre;
        }
    }

    private RegionInfo zoom(final int x, final int z, FastObjCache64.Sampler<RegionInfo> cache) {
        int upX = x >> 1;
        int upZ = z >> 1;

        RegionInfo minusXZ = cache.sample(upX, upZ);

        if (upX << 1 == x) {
            if (upZ << 1 == z) {
                return minusXZ;
            } else { // only z is advanced
                RegionInfo plusZ = cache.sample(upX, upZ + 1);
                return Voronoi.random2(x, z, this.jgSeed - 123, 1) == 1 ? plusZ : minusXZ;
            }
        } else {
            if (upZ << 1 == z) { // only x is advanced
                RegionInfo plusX = cache.sample(upX + 1, upZ);
                return Voronoi.random2(x, z, this.jgSeed - 123, 1) == 1 ? plusX : minusXZ;
            } else {
                RegionInfo plusZ = cache.sample(upX, upZ + 1);
                RegionInfo plusX = cache.sample(upX + 1, upZ);
                RegionInfo plusXZ = cache.sample(upX + 1, upZ + 1);

//                RegionInfo plusXEdge = Voronoi.random2(x, z - 1, this.jgSeed - 123, 1) == 1 ? plusX : minusXZ;
//                RegionInfo plusZEdge = Voronoi.random2(x - 1, z, this.jgSeed - 123, 1) == 1 ? plusZ : minusXZ;
//                RegionInfo advancedPlusXEdge = Voronoi.random2(x, z + 1, this.jgSeed - 123, 1) == 1 ? plusXZ : plusZ;
//                RegionInfo advancedPlusZEdge = Voronoi.random2(x + 1, z, this.jgSeed - 123, 1) == 1 ? plusXZ : plusX;

                return switch(Voronoi.random2(x, z, this.jgSeed - 123, 3)) {
                    case 0 -> Voronoi.random2(x, z - 1, this.jgSeed - 123, 1) == 1 ? plusX : minusXZ; // plusXEdge (relative to minusXZ)
                    case 1 -> Voronoi.random2(x - 1, z, this.jgSeed - 123, 1) == 1 ? plusZ : minusXZ; // plusZEdge
                    case 2 -> Voronoi.random2(x, z + 1, this.jgSeed - 123, 1) == 1 ? plusXZ : plusZ; // advancedPlusXEdge (plusXEdge relative to plusZ)
                    default -> Voronoi.random2(x + 1, z, this.jgSeed - 123, 1) == 1 ? plusXZ : plusX; // advancedPlusZEdge
                };
            }
        }
    }
    // Root: 16 block size
    // + zoom = 32 block size
    private FastObjCache64<RegionInfo> coastline1 = new FastObjCache64<>((x, z) -> zoom(x, z, (x_, z_) -> voronoiZoom(x_, z_, 16, this.regionTypeCache)));
    private FastObjCache64<RegionInfo> coastline15 = new FastObjCache64<>((x, z) -> downgradeCoast(x, z, this.coastline1));
    // + zoom = 64 block size
    private FastObjCache64<RegionInfo> coastline2 = new FastObjCache64<>((x, z) -> zoom(x, z, this.coastline15::sample));
    // + final voronoi (x16) = 1024 block size
    private final int blockRegionSize = 1024;

    public RegionInfo sampleRegionByBlock(int x, int z) {
        return voronoiZoom(x, z, 16, this.coastline2);
    }

    private RegionInfo farFlow(int x, int z, int offset, float chance, FastObjCache64<RegionInfo> cache) {
        RegionInfo regionType = cache.sample(x, z);

        // we have already done coastal and anything with outflow1. also skip ocean
        if (regionType.type != RegionInfo.MOUNTAINS || regionType.outflow1 != null) {
            return regionType;
        }

        RegionInfo north = cache.sample(x - 1, z);
        RegionInfo east = cache.sample(x, z - 1);
        RegionInfo south = cache.sample(x + 1, z);
        RegionInfo west = cache.sample(x, z + 1);

        List<Face> options = new ArrayList<>();
        if (north.outflow1 != null) {
            options.add(Face.NORTH);
            if (north.type == RegionInfo.FLOODPLAIN) options.add(Face.NORTH);
        }
        if (east.outflow1 != null) {
            options.add(Face.EAST);
            if (east.type == RegionInfo.FLOODPLAIN) options.add(Face.EAST);
        }
        if (south.outflow1 != null) {
            options.add(Face.SOUTH);
            if (south.type == RegionInfo.FLOODPLAIN) options.add(Face.SOUTH);
        }
        if (west.outflow1 != null) {
            options.add(Face.WEST);
            if (west.type == RegionInfo.FLOODPLAIN) options.add(Face.WEST);
        }

        if (options.isEmpty()) {
            return regionType;
        }

        Random random = new Random(Voronoi.random2(x, z, this.jgSeed + offset, -1));
        random.nextInt();
        Face selected = options.get(random.nextInt(options.size()));

        if (chance < 1.0f && random.nextFloat() > chance) {
            return regionType;
        }

        return new RegionInfo(regionType.type, selected, null);
    }
    private final FastObjCache64<RegionInfo> flow1cache = new FastObjCache64<>((x, z) -> farFlow(x, z, 1, 0.6f, this.regionTypeCache));
    private final FastObjCache64<RegionInfo> flow2cache = new FastObjCache64<>((x, z) -> farFlow(x, z, 2, 1.0f, this.flow1cache));

    private record Vertex(Vec2f point, Type type, int regionType, boolean inSquare, List<GraphEdge> edges, AtomicBoolean taken) {
        Vertex(Vec2f point, Type type, int regionType, boolean inSquare) {
            this(point, type, regionType, inSquare, new ArrayList<>(), new AtomicBoolean(false));
        }

        // makes code nicer
        enum Type {
            CENTRAL,
            EDGE,
            CORNER,
            OCEAN
        }
    }
    private record GraphEdge(Vertex from, Vertex to, double weight) implements Comparable<GraphEdge> {
        @Override
        public int compareTo(GraphEdge edge) {
            return Double.compare(weight, edge.weight);
        }

        RiverEdge seal() {
            return new RiverEdge(from.point, to.point);
        }
    }

    public record RiverEdge(Vec2f flowFrom, Vec2f flowTo) {
    }

    private static final class RiverGen {
        RiverGen(Vertex vertex, Random random) {
            this.origin = vertex.point;
            vertex.taken.set(true);

            int length = getSuitableConnections(options, vertex);
            if (length > 0) {
                GraphEdge selected = options[random.nextInt(length)];
                this.add(selected);
            } else {
                // for now, shouldn't happen with current algorithm.
                throw new IllegalStateException("No possible vertex connection from vertex " + vertex);
            }
        }

        // Weight to bias direction away from source over randomness
        static final double farBias = 0.5;
        // unimplemented biases
        static final double straightBias = 0.5; // using previous edge, bias away looping, a sort of counter to farBias
        static final double spreadBias = 0.5; // bias to promote connecting closer to the origin, another sort of counter for farBias
        static final double prominenceBias = -10000; // do not go down a grade (perhaps better implemented in the alg)

        final Vec2f origin;
        final GraphEdge[] options = new GraphEdge[4];
        PriorityQueue<GraphEdge> toVisit = new PriorityQueue<>();
        List<RiverEdge> added = new ArrayList<>();

        /**
         * Generate new graph edges to consider for the MST algorithm.
         * @param edges the output to store vertices into.
         * @param vertex the vertex to generate edges from.
         * @return the number of vertices added to edges.
         */
        int getSuitableConnections(@Out GraphEdge[] edges, Vertex vertex) {
            int i = 0;
            for (GraphEdge edge : vertex.edges) {
                // This deals with edges not necessarily being specified in a uniform direction relative to our vertex
                Vertex flowTo = vertex;
                Vertex flowFrom = edge.from == vertex ? edge.to : edge.from;

                // corners already filtered out if not good
                if (flowFrom.type == Vertex.Type.OCEAN) {
                    continue;
                }
                if (!flowFrom.inSquare) {
                    continue;
                }
                // this will need to be checked after remove from priority queue too.
                if (flowFrom.taken.get()) {
                    continue;
                }

                // Bias the weight and sort connection: create new edge copy.
                double oldSqrDist = origin.squaredDist(flowTo.point);
                double newSqrDist = origin.squaredDist(flowFrom.point);
                double newWeight = edge.weight - farBias * (newSqrDist - oldSqrDist);

                edges[i++] = new GraphEdge(flowFrom, flowTo, newWeight);
            }

            return i; // length
        }

        /**
         * Add the edge to this RiverGen and mark new edges to visit.
         * @param edge the edge added. Should be adjusted to flow FROM the higher elevation TO lower elevation, with
         *             the new weight.
         */
        void add(GraphEdge edge) {
            edge.from.taken.set(true);

            // Add new options to priority queue
            int length = getSuitableConnections(options, edge.from);
            for (int i = 0; i < length; i++) {
                toVisit.add(options[i]);
            }
        }

        static final int MASK = 3;

        static RiverGen create(Face outflow, int x, int z, int voronoiSeed, Vertex[][] vertices, Random random) {
            int xSample = outflow.isNegative() && outflow.getX() != 0 ? x - 1 : x;
            int zSample = outflow.isNegative() && outflow.getZ() != 0 ? z - 1 : z;

            int randomOffset = Voronoi.random2(xSample, zSample, voronoiSeed, MASK);
            Vertex vertex;
            switch (outflow) {
                case NORTH:
                    vertex = vertices[0][1 + randomOffset];
                    break;
                case SOUTH:
                    vertex = vertices[vertices.length - 1][1 + randomOffset];
                    break;
                case EAST:
                    vertex = vertices[1 + randomOffset][0];
                    break;
                case WEST:
                    vertex = vertices[1 + randomOffset][vertices.length - 1];
                    break;
                default:
                    throw new IllegalArgumentException("Invalid outflow face " + outflow);
            }

            // already selected
            if (vertex.taken.get()) {
                randomOffset = (randomOffset + (MASK - 1)) & MASK;

                switch (outflow) {
                    case NORTH:
                        vertex = vertices[0][1 + randomOffset];
                        break;
                    case SOUTH:
                        vertex = vertices[vertices.length - 1][1 + randomOffset];
                        break;
                    case EAST:
                        vertex = vertices[1 + randomOffset][0];
                        break;
                    case WEST:
                        vertex = vertices[1 + randomOffset][vertices.length - 1];
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid outflow face " + outflow);
                }
            }

            return new RiverGen(vertex, random);
        }
    }

    private void createRivers(int x, int z) {
        final FastObjCache64<RegionInfo> cache = this.flow2cache;

        RegionInfo region = cache.sample(x, z);

        // No rivers :(
        if (region.outflow1 == null) {
            return;
        }

        RegionInfo north = cache.sample(x - 1, z);
        RegionInfo east = cache.sample(x, z - 1);
        RegionInfo south = cache.sample(x + 1, z);
        RegionInfo west = cache.sample(x, z + 1);

        // Create vertices
        // mask to pick edge positions, not corners
        // + 1 because mask->size, +2 for each edge.
        final int DETAIL = RiverGen.MASK + 1 + 2;

        Vertex[][] vertices = new Vertex[DETAIL][DETAIL];

        // other side overlapping with neighbour's start
        // thus - 1 in scale of x/z
        for (int i = 0; i < DETAIL; i++) {
            int innerX = x * (DETAIL - 1) + i;
            boolean edgeX = i == 0 || i == DETAIL - 1;
            boolean isOceanX = i == 0 && north.type == RegionInfo.OCEAN || i == DETAIL - 1 && south.type == RegionInfo.OCEAN;

            for (int j = 0; j < DETAIL; j++) {
                int innerZ = z * (DETAIL - 1) + j;
                boolean edgeZ = j == 0 || j == DETAIL - 1;
                boolean isOceanZ = j == 0 && east.type == RegionInfo.OCEAN || j == DETAIL - 1 && west.type == RegionInfo.OCEAN;

                Vec2f pt = Voronoi.sampleVoronoiGrid(innerX, innerZ, this.jgSeed + 123, 0.3f);
                Vertex.Type type = (isOceanX || isOceanZ) ? Vertex.Type.OCEAN : (
                        edgeZ && edgeX ? Vertex.Type.CORNER : (edgeZ || edgeX) ? Vertex.Type.EDGE : Vertex.Type.CENTRAL
                );
                boolean edgeOwner;
                if (type == Vertex.Type.EDGE) {
                    if (i == 0) {
                        edgeOwner = (pt.id() & 1) == 0;
                    } else if (i == DETAIL - 1) {
                        edgeOwner = (pt.id() & 1) == 1;
                    } else if (j == 0) {
                        edgeOwner = (pt.id() & 1) == 0;
                    } else {
                        edgeOwner = (pt.id() & 1) == 1;
                    }
                } else {
                    // doesn't fall under edge ownership rules
                    edgeOwner = true;
                }

                final float innerToRegionScale = DETAIL - 1;
                RegionInfo info = this.coastline2.sample(
                        (int)(innerX / innerToRegionScale * blockRegionSize),
                        (int)(innerZ / innerToRegionScale * blockRegionSize)
                );
                vertices[i][j] = new Vertex(pt, type, info.type, edgeOwner);
            }
        }

        // Create edges. Points are jittered for more randomness.
        List<GraphEdge> edges = new ArrayList<>();
        Random random = new Random(this.seed + x + z);

        ///  Horizontal Edges
        for (int jz = 0; jz < DETAIL; jz++) {
            for (int ix = 0; ix < DETAIL - 1; ix++) {
                // skip corners that aren't (0,0)
                Vertex v = vertices[ix][jz]; // could only be left corner
                Vertex v1 = vertices[ix + 1][jz]; // could only be right corner

                if (v.type == Vertex.Type.CORNER && jz != 0) {
                    continue;
                }
                if (v1.type == Vertex.Type.CORNER) {
                    continue;
                }

                GraphEdge e = new GraphEdge(v, v1, random.nextDouble());
                edges.add(e);

                v.edges.add(e);
                v1.edges.add(e);
            }
        }
        /// Vertical Edges
        for (int ix = 0; ix < DETAIL; ix++) {
            for (int jz = 0; jz < DETAIL - 1; jz++) {
                // skip corners that aren't (0,0)
                Vertex v = vertices[ix][jz]; // could only be top corner
                Vertex v1 = vertices[ix][jz + 1]; // could only be bottom corner

                if (v.type == Vertex.Type.CORNER && ix != 0) {
                    continue;
                }
                if (v1.type == Vertex.Type.CORNER) {
                    continue;
                }

                GraphEdge e = new GraphEdge(v, v1, random.nextDouble());
                edges.add(e);

                v.edges.add(e);
                v1.edges.add(e);
            }
        }

        // Start at outflow and work way in using kruskal. Outflows cannot merge.
        // Edge weights will be additionally biased by direction away from source.

        // Pick positions for the outflows of this region
        RiverGen[] gen = new RiverGen[region.outflow2 == null ? 1 : 2];

        gen[0] = RiverGen.create(region.outflow1, x, z, this.jgSeed, vertices, random);

        if (gen.length > 1) {
            gen[1] = RiverGen.create(region.outflow2, x, z, this.jgSeed + 321, vertices, random);
        }

        // -----

        boolean stillGoing;
        do {
            stillGoing = false;

            for (RiverGen rg : gen) {
                @Nullable GraphEdge ge;

                while (true) {
                    ge = rg.toVisit.poll();
                    if (ge == null) {
                        break;
                    }
                    if (!ge.from.taken.get()) {
                        // found available option
                        rg.add(ge);
                        // -- continue --
                        stillGoing = true;
                        break;
                    }
                }
            }
        } while (stillGoing);
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

        public int regionType(int x, int z) {
            return this.earth.regionInfo(x, z).type;
        }

        public RegionInfo riverInfo(int x, int z) {
            return this.earth.flow2cache.sample(x, z);
        }

        public int regionTypeCoastline(int x, int z) {
            return this.earth.sampleRegionByBlock(x, z).type;
        }
    }
}
