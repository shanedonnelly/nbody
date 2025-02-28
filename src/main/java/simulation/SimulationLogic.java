package simulation;

import core.Body;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import websocket.GridWebSocket;

@Singleton
public class SimulationLogic {
    public static final int WIDTH = 40;
    public static final int HEIGHT = 30;
    public static final double G = 6.67430e-8;
    public static final double TIME_STEP = 0.3;
    public static final int BODY_COUNT = 10;
    public static final double CENTER_MASS = 5e9;
    private static final Random RAND = new Random();

    private static final Logger logger = LoggerFactory.getLogger(SimulationLogic.class);

    private Thread simulationThread;
    
    // Utiliser volatile pour garantir la visibilité entre les threads
    private volatile boolean running = false;

    private List<Body> bodies;

    //functions to delete 10% of the bodies randomly
    public void deleteBodies() {
        logger.info("Deleting bodies");
        int toDelete = BODY_COUNT/ 10;
        if (bodies.size() <= toDelete) {
            logger.info("there is not enough bodies to delete");
            return;
        }
        else{
            for (int i = 0; i < toDelete; i++) {
                int index = RAND.nextInt(bodies.size());
                bodies.remove(index);
            }
        }

        logger.info("there is now {} bodies", bodies.size());
    }
    //same to add 10% of the bodies randomly
    public void addBodies() {
        logger.info("Adding bodies");
        int toAdd = BODY_COUNT / 10;
        for (int i = 0; i < toAdd; i++) {
            bodies.add(createOneBody());
        }
        logger.info("there is now {} bodies", bodies.size());
    }


    private static SimulationLogic instance;

    private SimulationLogic() {
        bodies = createBodies(BODY_COUNT);
        // Initialiser le thread sans le démarrer
        simulationThread = new Thread(this::runSimulationThread, "SimulationThread");
        simulationThread.setDaemon(true); // Empêche le thread de bloquer l'arrêt de l'application
    }

    public static synchronized SimulationLogic getInstance() {
        if (instance == null) {
            instance = new SimulationLogic();
        }
        return instance;
    }

    public static synchronized void initiateInstance() {
        if (instance == null) {
            instance = new SimulationLogic();
        }
    }

    /**
     * Méthode à exécuter en continu dans le thread de simulation
     */
    private void runSimulationThread() {
        try {
            int steps = 0;
            while (running && !Thread.currentThread().isInterrupted()) {

                try {
                    steps++;
                    simulateOneStep();
                    String grid = getGrid();
                    // Envoyer la grille à tous les clients connectés
                    GridWebSocket.broadcast(grid);
                    //logger.info("Simulating step {}", steps);
                    //logger.info(grid);
                    // Ajouter un petit délai pour éviter une utilisation excessive du CPU
                    Thread.sleep(50);
                    //Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            logger.info("Simulation thread stopped");
        }
    }

    /**
     * Démarre le thread de simulation
     */
    public synchronized void startSimulation() {
        if (!running) {
            running = true;
            // Si le thread a déjà été lancé et arrêté, il faut en créer un nouveau
            if (simulationThread == null || !simulationThread.isAlive()) {
                simulationThread = new Thread(this::runSimulationThread, "SimulationThread");
                simulationThread.setDaemon(true);
            }
            simulationThread.start();
            System.out.println("Simulation started");
        }
    }

    /**
     * Arrête le thread de simulation de manière propre
     */
    public synchronized void stopSimulation() {
        running = false;
        if (simulationThread != null && simulationThread.isAlive()) {
            simulationThread.interrupt();
            try {
                // Attendre la fin du thread avec un timeout
                simulationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
               logger.error("Interruption lors de l'attente de l'arrêt: " + e.getMessage());
            }
            logger.info("Simulation stopped");
        }
    }
    
    /**
     * Vérifie si la simulation est en cours d'exécution
     */
    public boolean isRunning() {
        return running && simulationThread != null && simulationThread.isAlive();
    }

    // Le reste de votre code reste inchangé
    public void simulateOneStep() {
        simulateStep(bodies);
    }

    public String getGrid() {
        return buildGrid(bodies);
    }


    public Body createOneBody() {
        double r = 1 + RAND.nextDouble() * (Math.min(WIDTH, HEIGHT) / 2.0 - 1);
        double angle = RAND.nextDouble() * 2 * Math.PI;
        double x = (WIDTH / 2.0) + r * Math.cos(angle);
        double y = (HEIGHT / 2.0) + r * Math.sin(angle);

        double v = Math.sqrt((G * CENTER_MASS) / (r + 1e-9));
        double velocityVariation = 0.8 + 0.4 * RAND.nextDouble();
        double vx = -v * Math.sin(angle) * velocityVariation;
        double vy = v * Math.cos(angle) * velocityVariation;
        double mass = 1e6 + RAND.nextDouble() * (1e7 - 1e6);
        return new Body(x, y, vx, vy, mass);
    }
    public List<Body> createBodies(int count) {
        List<Body> bodies = new ArrayList<>();
        bodies.add(new Body(WIDTH / 2.0, HEIGHT / 2.0, 0, 0, CENTER_MASS));

        for (int i = 0; i < count - 1; i++) {
            bodies.add(createOneBody());
        }
        return bodies;
    }

    public void computeForces(List<Body> bodies, double[] fx, double[] fy) {
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

    public void updatePositions(List<Body> bodies, double[] fx, double[] fy) {
        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            b.setVx(b.getVx() + (fx[i] / b.getMass()) * TIME_STEP);
            b.setVy(b.getVy() + (fy[i] / b.getMass()) * TIME_STEP);
            b.setX(b.getX() + b.getVx() * TIME_STEP);
            b.setY(b.getY() + b.getVy() * TIME_STEP);
        }
    }

    public void simulateStep(List<Body> bodies) {
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        computeForces(bodies, fx, fy);
        updatePositions(bodies, fx, fy);
    }

    public String buildGrid(List<Body> bodies) {
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