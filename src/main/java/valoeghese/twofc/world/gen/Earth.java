package valoeghese.twofc.world.gen;

import valoeghese.twofc.util.Face;
import valoeghese.twofc.util.FastObjCache64;
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

    private RegionInfo flow(int x, int z, int offset, float chance, FastObjCache64<RegionInfo> cache) {
        RegionInfo regionType = cache.sample(x, z);

        if (regionType.type != 1 || regionType.outflow1 != null) {
            return regionType;
        }

        RegionInfo north = cache.sample(x - 1, z);
        RegionInfo east = cache.sample(x, z - 1);
        RegionInfo south = cache.sample(x + 1, z);
        RegionInfo west = cache.sample(x, z + 1);

        List<Face> options = new ArrayList<>();
        if (north.outflow1 != null) {
            options.add(Face.NORTH);
            if (north.type == 2) options.add(Face.NORTH);
        }
        if (east.outflow1 != null) {
            options.add(Face.EAST);
            if (east.type == 2) options.add(Face.EAST);
        }
        if (south.outflow1 != null) {
            options.add(Face.SOUTH);
            if (south.type == 2) options.add(Face.SOUTH);
        }
        if (west.outflow1 != null) {
            options.add(Face.WEST);
            if (west.type == 2) options.add(Face.WEST);
        }

        if (options.isEmpty()) {
            return regionType;
        }

        Random random = new Random(Voronoi.random2(x, z, (int)(this.seed & 0xFFFFFFFFL) + offset, -1));
        random.nextInt();
        Face selected = options.get(random.nextInt(options.size()));

        if (chance < 1.0f && random.nextFloat() > chance) {
            return regionType;
        }

        return new RegionInfo(regionType.type, selected, null);
    }
    private final FastObjCache64<RegionInfo> flow1cache = new FastObjCache64<>((x, z) -> flow(x, z, 1, 0.6f, this.regionTypeCache));
    private final FastObjCache64<RegionInfo> flow2cache = new FastObjCache64<>((x, z) -> flow(x, z, 2, 1.0f, this.flow1cache));

    public record RiverEdge(Vec2f flowFrom, Vec2f flowTo) {
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

        // makes code nicer
        enum Type {
            CENTRAL,
            EDGE,
            CORNER,
            OCEAN
        }
        interface E {}
        record Vertex(Vec2f point, Type type, boolean inSquare, List<E> edges, AtomicBoolean taken) {
            Vertex(Vec2f point, Type type, boolean inSquare) {
                this(point, type, inSquare, new ArrayList<>(), new AtomicBoolean(false));
            }
        }
        record GraphEdge(Vertex from, Vertex to, double weight) implements E, Comparable<GraphEdge> {
            @Override
            public int compareTo(GraphEdge edge) {
                return Double.compare(weight, edge.weight);
            }

            RiverEdge seal() {
                return new RiverEdge(from.point, to.point);
            }
        }

        final int DETAIL = 4;
        Vertex[][] vertices = new Vertex[DETAIL][DETAIL];

        // other side overlapping with neighbour's start
        // thus - 1 in scale of x/z
        for (int i = 0; i < DETAIL; i++) {
            int innerX = x * (DETAIL - 1) + i;
            boolean edgeX = i == 0 || i == DETAIL - 1;
            boolean isOceanX = i == 0 && north.type == 0 || i == DETAIL - 1 && south.type == 0;

            for (int j = 0; j < DETAIL; j++) {
                int innerZ = z * (DETAIL - 1) + j;
                boolean edgeZ = j == 0 || j == DETAIL - 1;
                boolean isOceanZ = j == 0 && east.type == 0 || j == DETAIL - 1 && west.type == 0;

                Vec2f pt = Voronoi.sampleVoronoiGrid(innerX, innerZ, (int)(this.seed & 0xFFFFFFFFL) + 123, 0.3f);
                Type type = (isOceanX || isOceanZ) ? Type.OCEAN : (
                        edgeZ && edgeX ? Type.CORNER : (edgeZ || edgeX) ? Type.EDGE : Type.CENTRAL
                );
                boolean edgeOwner;
                if (type == Type.EDGE) {
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

                vertices[i][j] = new Vertex(pt, type, edgeOwner);
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

                if (v.type == Type.CORNER && jz != 0) {
                    continue;
                }
                if (v1.type == Type.CORNER) {
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

                if (v.type == Type.CORNER && ix != 0) {
                    continue;
                }
                if (v1.type == Type.CORNER) {
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
        final double fanWeight = 0.5;

        class RiverGen {
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

            final Vec2f origin;
            final GraphEdge[] options = new GraphEdge[4];
            PriorityQueue<GraphEdge> toVisit = new PriorityQueue<>();
            List<RiverEdge> added = new ArrayList<>();

            int getSuitableConnections(GraphEdge[] edges, Vertex vertex) {
                int i = 0;
                for (E e : vertex.edges) {
                    GraphEdge edge = (GraphEdge) e;
                    Vertex flowTo = vertex;
                    Vertex flowFrom = edge.from == vertex ? edge.to : edge.from;

                    // corners already filtered out if not good
                    if (flowFrom.type == Type.OCEAN) {
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
                    double newWeight = edge.weight - fanWeight * (newSqrDist - oldSqrDist);

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
        }

        // FIXME implement this
        RiverGen[] gen = new RiverGen[region.outflow2 == null ? 1 : 2];
        gen[0] = new RiverGen(null, null);

        if (gen.length > 1) {
            gen[1] = new RiverGen(null, null);
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
            return this.earth.regionType(x, z).type;
        }

        public RegionInfo riverInfo(int x, int z) {
            return this.earth.flow2cache.sample(x, z);
        }
    }
}
