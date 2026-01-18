import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeSet;

class Point implements Comparable<Point> {
    long x;
    long y;
    boolean isTriangleVertex = false;
    Triangle triangle = null;

    Point(long x, long y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Point o) {
        int cmp = Long.compare(o.y, this.y);
        if (cmp != 0)
            return cmp;

        int p1 = priority(this);
        int p2 = priority(o);
        if (p1 != p2)
            return Integer.compare(p1, p2);

        // Tie-breaker to prevent equality
        cmp = Long.compare(this.x, o.x);
        if (cmp != 0)
            return cmp;

        return Integer.compare(
                System.identityHashCode(this),
                System.identityHashCode(o));
    }

    private int priority(Point p) {
        if (!p.isTriangleVertex)
            return 1;
        if (p.triangle.highestVertex == p)
            return 0;
        return 2;
    }

    @Override
    public String toString() {
        return "(" + this.x + " " + this.y + ")";
    }
}

class Triangle implements Comparable<Triangle> {
    Point[] vertices;
    Point highestVertex;
    Point lowestVertex;

    Triangle(Point[] vertices) {
        this.vertices = vertices;

        for (Point point : vertices) {
            point.isTriangleVertex = true;
            point.triangle = this;
        }

        Point[] thisSorted = this.vertices.clone();
        Arrays.sort(thisSorted, (a, b) -> {
            return Long.compare(a.y, b.y);
        });
        this.highestVertex = thisSorted[2];
        this.lowestVertex = thisSorted[0];
    }

    @Override
    public int compareTo(Triangle o) {
        long lx1 = this.minXAtY(Assignment2.currentY);
        long lx2 = o.minXAtY(Assignment2.currentY);
        if (lx1 != lx2)
            return Long.compare(lx1, lx2);

        long rx1 = this.maxXAtY(Assignment2.currentY);
        long rx2 = o.maxXAtY(Assignment2.currentY);
        if (rx1 != rx2)
            return Long.compare(rx1, rx2);

        return 0; // geometrically equal at this y
    }

    public static long getCrossProductDirection(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
    }

    public boolean pointInTriangle(Point p) {
        long ab = getCrossProductDirection(p, this.vertices[0], this.vertices[1]);
        long bc = getCrossProductDirection(p, this.vertices[1], this.vertices[2]);
        long ca = getCrossProductDirection(p, this.vertices[2], this.vertices[0]);

        return (ab >= 0 && bc >= 0 && ca >= 0) || (ab <= 0 && bc <= 0 && ca <= 0);
    }

    public long maxXAtY(long y) {
        long maxX = Long.MIN_VALUE;
        boolean found = false;

        for (int i = 0; i < 3; i++) {
            Point a = vertices[i];
            Point b = vertices[(i + 1) % 3];

            if ((a.y <= y && y <= b.y) || (b.y <= y && y <= a.y)) {

                if (a.y == b.y) {
                    maxX = Math.max(maxX, Math.max(a.x, b.x));
                    found = true;
                } else {
                    long x = a.x + (b.x - a.x) * (y - a.y) / (b.y - a.y);
                    maxX = Math.max(maxX, x);
                    found = true;
                }
            }
        }

        if (!found) {
            throw new IllegalStateException(
                    "Active triangle does not intersect sweep line y=" + y);
        }

        return maxX;
    }

    long minXAtY(long y) {
        long minX = Long.MAX_VALUE;
        boolean found = false;

        for (int i = 0; i < 3; i++) {
            Point a = vertices[i];
            Point b = vertices[(i + 1) % 3];

            // Check if edge intersects horizontal line y (including endpoints)
            if ((a.y <= y && y <= b.y) || (b.y <= y && y <= a.y)) {

                // Horizontal edge
                if (a.y == b.y) {
                    minX = Math.min(minX, Math.min(a.x, b.x));
                    found = true;
                } else {
                    // Proper intersection
                    long x = a.x + (b.x - a.x) * (y - a.y) / (b.y - a.y);
                    minX = Math.min(minX, x);
                    found = true;
                }
            }
        }

        if (!found) {
            throw new IllegalStateException(
                    "Active triangle does not intersect sweep line y=" + y);
        }

        return minX;
    }
}

public class Assignment2 {
    static long currentY;

    public static long run(Point[] points, Triangle[] triangles) {
        TreeSet<Triangle> T = new TreeSet<>();
        PriorityQueue<Point> Q = new PriorityQueue<>();

        for (int i = 0; i < points.length; i++) {
            Q.add(points[i]);
        }

        for (int i = 0; i < triangles.length; i++) {
            Q.addAll(Arrays.asList(triangles[i].vertices));
        }

        long counter = 0;

        while (!Q.isEmpty()) {
            Point v = Q.poll();
            Assignment2.currentY = v.y;

            if (v.isTriangleVertex) {
                if (v.triangle.highestVertex == v) {
                    T.add(v.triangle);
                }
                if (v.triangle.lowestVertex == v) {
                    T.remove(v.triangle);
                }
            } else {
                // Compare the triangles to the point to find the neighbouring ones
                Point temp = new Point(v.x, v.y);
                Point[] vertices = { temp, temp, temp };
                Triangle left = T.floor(new Triangle(vertices));
                Triangle right = T.ceiling(new Triangle(vertices));

                if (left != null && left.pointInTriangle(v)) {
                    counter++;
                }
                if (right != null && left != right && right.pointInTriangle(v)) {
                    counter++;
                }
            }
        }

        return counter;
    }

    public static void runOnFile(String filename, PrintWriter out) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(filename));

        int nPoints = Integer.parseInt(sc.nextLine().substring(8));
        Point[] points = new Point[nPoints];

        for (int i = 0; i < nPoints; i++) {
            points[i] = new Point(sc.nextLong(), sc.nextLong());
        }

        sc.nextLine();

        int nTriangles = Integer.parseInt(sc.nextLine().substring(11));
        Triangle[] triangles = new Triangle[nTriangles];

        for (int i = 0; i < nTriangles; i++) {
            Point[] vertices = { new Point(sc.nextLong(), sc.nextLong()), new Point(sc.nextLong(), sc.nextLong()),
                    new Point(sc.nextLong(), sc.nextLong()) };
            triangles[i] = new Triangle(vertices);
        }

        sc.close();

        long result = run(points, triangles);

        double average = 0;

        for (int iter = 0; iter < 100; iter++) {
            long start = System.nanoTime();

            run(points, triangles);

            long end = System.nanoTime();
            double timeMs = (end - start) / 1_000_000.0;

            average += timeMs;
        }

        out.println("File: " + new File(filename).getName());
        out.println("Algorithm: Plane-Sweep");
        out.println("Result: " + result);
        out.println("Running time: " + average / 100.0 + " ms");
        out.println();
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 2) {
            System.err.println("Usage: java Assignment2 <input-folder> <output-file>");
            return;
        }

        File folder = new File(args[0]);
        PrintWriter out = new PrintWriter(args[1]);

        if (!folder.isDirectory()) {
            System.err.println(args[0] + " is not a directory");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.err.println("No .txt files found in " + folder);
            return;
        }

        // Optional: sort for reproducible order
        Arrays.sort(files);

        for (File file : files) {
        runOnFile(file.getAbsolutePath(), out);
    }

        out.close();
    }
}