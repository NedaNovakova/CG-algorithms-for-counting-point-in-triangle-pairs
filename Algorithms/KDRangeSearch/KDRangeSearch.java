import de.biomedical_imaging.edu.wlu.cs.levy.CG.KDTree;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeyDuplicateException;
import de.biomedical_imaging.edu.wlu.cs.levy.CG.KeySizeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

class Point {
    int id;
    long x;
    long y;

    Point(int i, long x, long y) {
        this.id = i;
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Point))
            return false;
        Point p = (Point) o;
        return Double.compare(p.x, x) == 0 &&
                Double.compare(p.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

class Triangle {
    Point[] vertices;

    Triangle(Point[] vertices) {
        this.vertices = vertices;
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

    public long[] boundingBox() {
        long[] x = { this.vertices[0].x, this.vertices[1].x, this.vertices[2].x };
        long[] y = { this.vertices[0].y, this.vertices[1].y, this.vertices[2].y };

        long xmin = Math.min(x[0], Math.min(x[1], x[2]));
        long xmax = Math.max(x[0], Math.max(x[1], x[2]));
        long ymin = Math.min(y[0], Math.min(y[1], y[2]));
        long ymax = Math.max(y[0], Math.max(y[1], y[2]));

        long[] box = { xmin, ymin, xmax, ymax };
        return box;
    }
}

public class KDRangeSearch {

    public static long run(Point[] points, Triangle[] triangles) throws KeySizeException, KeyDuplicateException {
        HashMap<Point, Integer> pointCounts = new HashMap<>();

        for (int i = 0; i < points.length; i++) {
            if (pointCounts.containsKey(points[i])) {
                pointCounts.put(points[i], pointCounts.get(points[i]) + 1);
            } else {
                pointCounts.put(points[i], 1);
            }
        }

        int counter = 0;

        KDTree<Integer> tree = new KDTree<>(2);

        for (Point p : pointCounts.keySet()) {
            tree.insert(new double[] { (double) p.x, (double) p.y }, p.id);
        }

        for (int i = 0; i < triangles.length; i++) {
            long[] boundingBox = triangles[i].boundingBox();

            List<Integer> candidates = tree.range(new double[] { (double) boundingBox[0], (double) boundingBox[1] },
                    new double[] { (double) boundingBox[2], (double) boundingBox[3] });

            for (Integer pointID : candidates) {
                Point p = points[pointID];

                if (triangles[i].pointInTriangle(p)) {
                    counter += pointCounts.get(p);
                }
            }
        }

        return counter;
    }

    public static void runOnFile(String filename, PrintWriter out) throws FileNotFoundException, KeySizeException, KeyDuplicateException {
        Scanner sc = new Scanner(new File(filename));

        int nPoints = Integer.parseInt(sc.nextLine().substring(8));
        Point[] points = new Point[nPoints];

        for (int i = 0; i < nPoints; i++) {
            points[i] = new Point(i, sc.nextLong(), sc.nextLong());
        }

        sc.nextLine();

        int nTriangles = Integer.parseInt(sc.nextLine().substring(11));
        Triangle[] triangles = new Triangle[nTriangles];

        for (int i = 0; i < nTriangles; i++) {
            Point[] vertices = { new Point(i, sc.nextLong(), sc.nextLong()), new Point(i, sc.nextLong(), sc.nextLong()),
                    new Point(i, sc.nextLong(), sc.nextLong()) };
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
        out.println("Algorithm: KD-Search");
        out.println("Result: " + result);
        out.println("Running time: " + average / 100.0 + " ms");
        out.println();
    }

    public static void main(String[] args) throws KeySizeException, KeyDuplicateException, FileNotFoundException {
        if (args.length != 2) {
            System.err.println("Usage: java KDRangeSearch <input-folder> <output-file>");
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
