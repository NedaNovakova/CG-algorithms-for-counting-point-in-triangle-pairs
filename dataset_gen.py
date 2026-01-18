import math
import random

def generate_horizontal_dataset(filename, N=1000):
    with open(filename, "w") as f:
        # Points
        f.write(f"points: {N}\n")
        for i in range(N):
            if i % 2 == 0:
                # inside triangle i
                x = i * 200 + 40
                y = 50
            else:
                # outside any triangle
                x = i * 200 + 150
                y = 50
            f.write(f"{x} {y}\n")

        # Triangles
        f.write(f"triangles: {N}\n")
        for i in range(N):
            x0 = i * 200
            # same height = 100, non-intersecting
            f.write(f"{x0} 0 {x0 + 80} 0 {x0 + 40} 100\n")

def generate_vertical_dataset(filename, N=1000):
     with open(filename, "w") as f:
        # Points
        f.write(f"points: {N}\n")
        for i in range(N):
            if i % 2 == 0:
                # inside triangle i
                y = i * 200 + 40
                x = 50
            else:
                # outside any triangle
                y = i * 200 + 150
                x = 50
            f.write(f"{x} {y}\n")

        # Triangles
        f.write(f"triangles: {N}\n")
        for i in range(N):
            y0 = i * 200
            # same height = 100, non-intersecting
            f.write(f"0 {y0} 0 {y0 + 80} 100 {y0 + 40}\n")


def generate_few_triangles_many_points(filename):
    NUM_POINTS = 5000
    NUM_TRIANGLES = 3
    INSIDE_POINTS = 4000
    OUTSIDE_POINTS = NUM_POINTS - INSIDE_POINTS

    with open(filename, "w") as f:
        # -------- Points --------
        f.write(f"points: {NUM_POINTS}\n")

        # Triangles are placed horizontally, height = 100
        # Triangle i covers x in [i*300, i*300+100], y in [0,100]

        # --- Points inside triangles ---
        per_triangle = INSIDE_POINTS // NUM_TRIANGLES
        remainder = INSIDE_POINTS % NUM_TRIANGLES

        point_count = 0
        for i in range(NUM_TRIANGLES):
            count = per_triangle + (1 if i < remainder else 0)
            base_x = i * 300
            for _ in range(count):
                # pick a guaranteed interior point
                x = base_x + 50
                y = 50
                f.write(f"{x} {y}\n")
                point_count += 1

        # --- Points outside all triangles ---
        for i in range(OUTSIDE_POINTS):
            x = 1000 + i * 10   # far away from triangles
            y = 150             # above all triangles
            f.write(f"{x} {y}\n")
            point_count += 1

        # -------- Triangles --------
        f.write(f"triangles: {NUM_TRIANGLES}\n")
        for i in range(NUM_TRIANGLES):
            x0 = i * 300
            f.write(f"{x0} 0 {x0 + 100} 0 {x0 + 50} 100\n")

def generate_many_triangles_few_points_50(filename):
    NUM_TRIANGLES = 5000
    NUM_POINTS = 50
    INSIDE_POINTS = 20
    OUTSIDE_POINTS = NUM_POINTS - INSIDE_POINTS

    with open(filename, "w") as f:
        # -------- Points --------
        f.write(f"points: {NUM_POINTS}\n")

        # 20 points inside different triangles
        for i in range(INSIDE_POINTS):
            tri_index = i * 10          # spread them out
            x = tri_index * 200 + 50
            y = 50
            f.write(f"{x} {y}\n")

        # 30 points outside all triangles
        for i in range(OUTSIDE_POINTS):
            x = 1000000 + i * 10
            y = 150
            f.write(f"{x} {y}\n")

        # -------- Triangles --------
        f.write(f"triangles: {NUM_TRIANGLES}\n")

        # Arrange triangles horizontally, non-intersecting
        for i in range(NUM_TRIANGLES):
            x0 = i * 200
            f.write(f"{x0} 0 {x0 + 100} 0 {x0 + 50} 100\n")

def generate_random_dataset(
    filename,
    num_triangles,
    num_points,
    num_points_inside,
    seed=42
):
    
    random.seed(seed)
    print(num_points_inside)
    TRI_HEIGHT = 100
    TRI_WIDTH = 100
    GAP = 50

    with open(filename, "w") as f:
        # ---------- Triangles ----------
        triangles = []

        # Place triangles horizontally, non-intersecting
        for i in range(num_triangles):
            base_x = i * (TRI_WIDTH + GAP)
            skew = random.randint(-30, 30)

            A = (base_x, 0)
            B = (base_x + TRI_WIDTH, 0)
            C = (base_x + TRI_WIDTH // 2 + skew, TRI_HEIGHT)

            triangles.append((A, B, C))

        # ---------- Points ----------
        points = []

        # Points inside triangles
        for i in range(num_points_inside):
            t = triangles[i % num_triangles]
            (x1, y1), (x2, y2), (x3, y3) = t

            # generate guaranteed interior point
            r1 = random.random()
            r2 = random.random()
            if r1 + r2 > 1:
                r1 = 1 - r1
                r2 = 1 - r2

            x = int(x1 + r1 * (x2 - x1) + r2 * (x3 - x1))
            y = int(y1 + r1 * (y2 - y1) + r2 * (y3 - y1))
            points.append((x, y))

        # Points outside triangles
        for _ in range(num_points - num_points_inside):
            x = random.randint(num_triangles * (TRI_WIDTH + GAP) + 100,
                               num_triangles * (TRI_WIDTH + GAP) + 10000)
            y = random.randint(0, TRI_HEIGHT * 2)
            points.append((x, y))

        # ---------- Write file ----------
        f.write(f"points: {num_points}\n")
        for x, y in points:
            f.write(f"{x} {y}\n")

        f.write(f"triangles: {num_triangles}\n")
        for (A, B, C) in triangles:
            f.write(f"{A[0]} {A[1]} {B[0]} {B[1]} {C[0]} {C[1]}\n")

if __name__ == "__main__":
    generate_horizontal_dataset("horizontal.txt", 10000)
    generate_vertical_dataset("vertical.txt", 10000)
    generate_few_triangles_many_points("few_triangles_many_points.txt")
    generate_few_triangles_many_points("many_triangles_few_points.txt")

    for i in range(5):
        num_triangles=5000 + i*3000
        generate_random_dataset(
            filename=f"random_dataset_{num_triangles}tr_5000p.txt",
            num_triangles=num_triangles,
            num_points = 5000,
            num_points_inside= math.ceil(random.random()*5000)
        )

    for i in range(5):
        num_pointss=5000 + i*3000
        generate_random_dataset(
            filename=f"random_dataset_5000tr_{num_pointss}p.txt",
            num_triangles=5000,
            num_points = num_pointss,
            num_points_inside= math.ceil(random.random()*5000)
        )