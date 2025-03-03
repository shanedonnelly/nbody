package org.acme.simulation;

import org.acme.core.Body;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.acme.websocket.GridWebSocket;

@Singleton
public class SimulationLogic {

    // Dimensions de la grille
    public static final int WIDTH = 400;
    public static final int HEIGHT = 200;

    
    // Paramètres physiques
    public static final double G = 6.67430e-7;  // Constante gravitationnelle augmentée pour voir les effets
    public static final double TIME_STEP = 0.1; // Pas de temps plus petit pour plus de précision
    
    // Paramètres de la galaxie
    public static final int BODY_COUNT = 180;   // Plus de corps pour une org.acme.simulation plus riche
    public static final double CENTER_MASS = 2e10; // Masse centrale plus importante (trou noir supermassif)
    
    // Paramètres de distribution
    public static final double GALAXY_RADIUS = Math.min(WIDTH, HEIGHT) * 0.4; // Rayon maximal de la galaxie
    public static final double SPIRAL_FACTOR = 0.3; // Facteur pour créer un effet de spirale

    // Paramètres de dynamique
    public static final double VELOCITY_DISPERSION = 0.2; // Variation dans les vitesses orbitales (0-1)
    public static final double MASS_MIN = 1e5;  // Masse minimale des corps
    public static final double MASS_MAX = 5e6;  // Masse maximale des corps

    private static final Random RAND = new Random();

    private static final Logger logger = LoggerFactory.getLogger(SimulationLogic.class);

    private Thread simulationThread;
    
    // Utiliser volatile pour garantir la visibilité entre les threads
    private volatile boolean running = false;

    private final List<Body> bodies;

    //functions to delete 10% of the bodies randomly
    public void deleteBodies() {
        logger.info("Deleting bodies");
        int toDelete = BODY_COUNT / 10;
        if (bodies.size() <= toDelete) {
            logger.info("there is not enough bodies to delete");
            return;
        } else {
            int deletedCount = 0;
            while (deletedCount < toDelete) {
                int index = RAND.nextInt(bodies.size());
                if (index != 0) {
                    bodies.remove(index);
                    deletedCount++;
                }
            }
        }
        logger.info("there is now {} bodies after delete", bodies.size());
    }
    //same to add 10% of the bodies randomly
    public void addBodies() {
        logger.info("Adding bodies");
        int toAdd = BODY_COUNT / 10;
        if(bodies.size() >= 3 * BODY_COUNT) {
            logger.info("there is too much bodies to add");
            return;
        }
        for (int i = 0; i < toAdd; i++) {
            bodies.add(createOneBody());
        }
        logger.info("there is now {} bodies after add", bodies.size());
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



    /**
     * Méthode à exécuter en continu dans le thread org.acme.simulation
     */
   private void runSimulationThread() {
    try {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                simulateOneStep();

                // Envoyer les positions des corps au lieu de la grille
                byte[] positionsBinary = getBodyPositionsBinary();
                GridWebSocket.broadcastBinary(positionsBinary);

                // Ajouter un petit délai pour éviter une utilisation excessive du CPU
                Thread.sleep(50);
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
     * Démarre le thread de org.acme.simulation
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
            logger.info("Simulation started");
        }
    }



    /**
     * Arrête le thread org.acme.simulation de manière propre
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
                logger.error("Interruption lors de l'attente de l'arrêt", e);            }
            logger.info("Simulation stopped");
        }
    }
    
    /**
     * Vérifie si la org.acme.simulation est en cours d'exécution
     */
    public boolean isRunning() {
        return running && simulationThread != null && simulationThread.isAlive();
    }

    // Le reste de votre code reste inchangé
    public void simulateOneStep() {
        simulateStep(bodies);
    }


        // Ajouter cette méthode pour générer une représentation binaire de la grille
    public byte[] getGridBinary() {
        int[][] grid = new int[HEIGHT][WIDTH];
        for (Body b : bodies) {
            int px = (int) Math.round(b.getX());
            int py = (int) Math.round(b.getY());
            if (px >= 0 && px < WIDTH && py >= 0 && py < HEIGHT) {
                grid[py][px] = 1;
            }
        }
    
        // Compression : 8 cellules par octet (1 bit par cellule)
        int bytesPerRow = (WIDTH + 7) / 8;
        byte[] result = new byte[HEIGHT * bytesPerRow];
        
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < WIDTH; col++) {
                if (grid[row][col] == 1) {
                    int byteIndex = row * bytesPerRow + (col / 8);
                    int bitPosition = 7 - (col % 8); // MSB first
                    result[byteIndex] |= (1 << bitPosition);
                }
            }
        }
        
        return result;
    }

public Body createOneBody() {
    // Distribution logarithmique pour concentrer les corps vers le centre
    double r = GALAXY_RADIUS * Math.pow(RAND.nextDouble(), 0.5);
    
    // Angle qui inclut un facteur spiral pour créer des bras
    double angle = RAND.nextDouble() * 2 * Math.PI;
    double spiralOffset = SPIRAL_FACTOR * r;
    angle += spiralOffset;
    
    double x = (WIDTH / 2.0) + r * Math.cos(angle);
    double y = (HEIGHT / 2.0) + r * Math.sin(angle);
    
    // Vitesse orbitale képlérienne avec dispersion pour créer du mouvement réaliste
    double v = Math.sqrt((G * CENTER_MASS) / (r + 1e-9));
    double velocityVariation = 1.0 - VELOCITY_DISPERSION + VELOCITY_DISPERSION * 2 * RAND.nextDouble();
    
    // Vitesse perpendiculaire à la direction radiale (orbite circulaire)
    double vx = -v * Math.sin(angle) * velocityVariation;
    double vy = v * Math.cos(angle) * velocityVariation;
    
    // Distribution logarithmique des masses
    double mass = MASS_MIN * Math.exp(Math.log(MASS_MAX/MASS_MIN) * RAND.nextDouble());
    
    return new Body(x, y, vx, vy, mass);
}
    public List<Body> createBodies(int count) {
        List<Body> newbodies = new ArrayList<>();
        newbodies.add(new Body(WIDTH / 2.0, HEIGHT / 2.0, 0, 0, CENTER_MASS));
        for (int i = 0; i < count - 1; i++) {
            newbodies.add(createOneBody());
        }
        return newbodies;
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
        for (int i = 1; i < bodies.size(); i++) {
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
     public List<Body> getBodies() {
         return bodies;
     }

         /**
     * Génère une représentation binaire des positions des corps au lieu de la grille entière
     */
    public byte[] getBodyPositionsBinary() {
        // Format : nombre de corps (4 bytes) + (x, y) pour chaque corps (2 x 2 bytes par corps)
        byte[] result = new byte[4 + bodies.size() * 4];
        
        // Nombre de corps (int = 4 bytes)
        int bodyCount = bodies.size();
        result[0] = (byte) ((bodyCount >> 24) & 0xFF);
        result[1] = (byte) ((bodyCount >> 16) & 0xFF);
        result[2] = (byte) ((bodyCount >> 8) & 0xFF);
        result[3] = (byte) (bodyCount & 0xFF);
        
        // Coordonnées de chaque corps
        for (int i = 0; i < bodies.size(); i++) {
            Body b = bodies.get(i);
            int x = (int) Math.round(b.getX());
            int y = (int) Math.round(b.getY());
            
            // Chaque coordonnée prend deux bytes (short).
            result[4 + i*4] = (byte) ((x >> 8) & 0xFF);
            result[4 + i*4 + 1] = (byte) (x & 0xFF);
            result[4 + i*4 + 2] = (byte) ((y >> 8) & 0xFF);
            result[4 + i*4 + 3] = (byte) (y & 0xFF);
        }
        
        return result;
    }
}