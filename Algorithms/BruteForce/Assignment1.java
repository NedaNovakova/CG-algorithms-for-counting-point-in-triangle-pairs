import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

class Point {
    long x;
    long y;

    Point(long x, long y) {
        this.x = x;
        this.y = y;
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
}

public class Assignment1 {
    public static long run(Point[] points, Triangle[] triangles) {
        long counter = 0;
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < triangles.length; j++) {
                if (triangles[j].pointInTriangle(points[i]))
                    counter++;
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
        out.println("Algorithm: Brute-Force");
        out.println("Result: " + result);
        out.println("Running time: " + average / 100.0 + " ms");
        out.println();
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 2) {
            System.err.println("Usage: java Assignment1 <input-folder> <output-file>");
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