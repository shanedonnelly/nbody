package simulation;

import core.Body;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationLogic {
    public static final int WIDTH = 40;
    public static final int HEIGHT = 30;
    public static final double G = 6.67430e-8;
    public static final double TIME_STEP = 0.3;
    public static final int BODY_COUNT = 10;
    private static final Random RAND = new Random();

    private SimulationLogic() {
        // Private constructor to prevent instantiation
    }

    public static List<Body> createBodies(int count) {
        List<Body> bodies = new ArrayList<>();
        double centerMass = 5e9;
        bodies.add(new Body(WIDTH / 2.0, HEIGHT / 2.0, 0, 0, centerMass));

        for (int i = 0; i < count - 1; i++) {
            double r = 1 + RAND.nextDouble() * (Math.min(WIDTH, HEIGHT) / 2.0 - 1);
            double angle = RAND.nextDouble() * 2 * Math.PI;
            double x = (WIDTH / 2.0) + r * Math.cos(angle);
            double y = (HEIGHT / 2.0) + r * Math.sin(angle);

            double v = Math.sqrt((G * centerMass) / (r + 1e-9));
            double velocityVariation = 0.8 + 0.4 * RAND.nextDouble();
            double vx = -v * Math.sin(angle) * velocityVariation;
            double vy = v * Math.cos(angle) * velocityVariation;
            double mass = 1e6 + RAND.nextDouble() * (1e7 - 1e6);

            bodies.add(new Body(x, y, vx, vy, mass));
        }
        return bodies;
    }


    public static void computeForces(List<Body> bodies, double[] fx, double[] fy) {
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                Body bi = bodies.get(i);
                Body bj = bodies.get(j);
                double dx = bj.getX() - bi.getX();
                double dy = bj.getY() - bi.getY();
                double distSq = dx * dx + dy * dy + 1e-9;
                double force = G * bi.getMass() * bj.getMass() / distSq;
                double dist = Math.sqrt(distSq);
                double fxi = force * dx / dist;
                double fyi = force * dy / dist;
                fx[i] += fxi;
                fy[i] += fyi;
                fx[j] -= fxi;
                fy[j] -= fyi;
            }
        }
    }

    public static void updatePositions(List<Body> bodies, double[] fx, double[] fy) {
        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            b.setVx(b.getVx() + (fx[i] / b.getMass()) * TIME_STEP);
            b.setVy(b.getVy() + (fy[i] / b.getMass()) * TIME_STEP);
            b.setX(b.getX() + b.getVx() * TIME_STEP);
            b.setY(b.getY() + b.getVy() * TIME_STEP);
        }
    }

    public static void simulateStep(List<Body> bodies) {
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        computeForces(bodies, fx, fy);
        updatePositions(bodies, fx, fy);
    }

    public static String buildGrid(List<Body> bodies) {
        int[][] grid = new int[HEIGHT][WIDTH];
        for (Body b : bodies) {
            int px = (int) Math.round(b.getX());
            int py = (int) Math.round(b.getY());
            if (px >= 0 && px < WIDTH && py >= 0 && py < HEIGHT) {
                grid[py][px] = 1;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                sb.append(grid[row][col]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}