package org.polytech.simulation;

import org.acme.core.Body;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.acme.simulation.SimulationLogic;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SimulationLogicTest {
    private static final int TEST_STEPS = 10;
    private SimulationLogic simulationLogic;

    @BeforeEach
    void setUp() {
        simulationLogic = SimulationLogic.getInstance();
        // Reset the simulation state before each test
        if (simulationLogic.isRunning()) {
            simulationLogic.stopSimulation();
        }
    }

    @Test
    void testGridGeneration() {
        List<Body> bodies = simulationLogic.createBodies(5); // Petit nombre pour le test

        String grid = simulationLogic.buildGrid(bodies);

        // Vérifier que la grille est générée correctement
        assertNotNull(grid);
        assertFalse(grid.isEmpty());
        assertTrue(grid.contains("0") || grid.contains("1"), "La grille devrait contenir des 0 ou des 1");
    }

    @Test
    void testSimulationLogic() {
        // Tester démarrage/arrêt de simulation
        simulationLogic.startSimulation();
        assertTrue(simulationLogic.isRunning());

        simulationLogic.stopSimulation();
        assertFalse(simulationLogic.isRunning());
    }

    @Test
    void testCreateBodies() {
        List<Body> bodies = simulationLogic.createBodies(SimulationLogic.BODY_COUNT);
        assertEquals(SimulationLogic.BODY_COUNT, bodies.size());
    }

    @Test
    void testComputeForces() {
        // Create two bodies with zero mass so they don't generate forces
        Body body1 = new Body(0, 0, 0, 0, 0);
        Body body2 = new Body(10, 10, 0, 0, 0);
        List<Body> bodies = List.of(body1, body2);

        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        simulationLogic.computeForces(bodies, fx, fy);

        assertArrayEquals(new double[bodies.size()], fx);
        assertArrayEquals(new double[bodies.size()], fy);
    }

    @Test
    void testUpdatePositions() {
        List<Body> bodies = simulationLogic.createBodies(2);
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        simulationLogic.computeForces(bodies, fx, fy);
        simulationLogic.updatePositions(bodies, fx, fy);
        assertNotEquals(0, bodies.getFirst().getX());
        assertNotEquals(0, bodies.getFirst().getY());
    }

    @Test
    void testSimulateStep() {
        List<Body> bodies = simulationLogic.createBodies(2);
        simulationLogic.simulateStep(bodies);
        assertNotEquals(0, bodies.getFirst().getX());
        assertNotEquals(0, bodies.getFirst().getY());
    }

    @Test
    void testBuildGrid() {
        List<Body> bodies = simulationLogic.createBodies(2);
        String grid = simulationLogic.buildGrid(bodies);
        assertNotNull(grid);
        assertFalse(grid.isEmpty());
    }

    @Test
    void testDeleteBodies() {
        simulationLogic.startSimulation(); // Initialize bodies
        int initialSize = simulationLogic.getBodies().size();
        simulationLogic.deleteBodies();

        // After deletion, we expect roughly 10% fewer bodies
        assertTrue(simulationLogic.getBodies().size() < initialSize);
        assertTrue(simulationLogic.getBodies().size() >= initialSize - (SimulationLogic.BODY_COUNT / 10) - 1);

        simulationLogic.stopSimulation();
    }

    @Test
    void testAddBodies() {
        simulationLogic.startSimulation(); // Initialize bodies
        int initialSize = simulationLogic.getBodies().size();
        simulationLogic.addBodies();

        // After this addition, we expect 10% more bodies
        assertEquals(initialSize + (SimulationLogic.BODY_COUNT / 10), simulationLogic.getBodies().size());

        simulationLogic.stopSimulation();
    }

    @Test
    void testSimulationLifecycle() {
        assertFalse(simulationLogic.isRunning());

        simulationLogic.startSimulation();
        assertTrue(simulationLogic.isRunning());

        simulationLogic.stopSimulation();
        assertFalse(simulationLogic.isRunning());
    }

    @Test
    void testGetGridBinary() {
        byte[] gridBinary = simulationLogic.getGridBinary();
        assertNotNull(gridBinary);

        int expectedSize = SimulationLogic.HEIGHT * ((SimulationLogic.WIDTH + 7) / 8);
        assertEquals(expectedSize, gridBinary.length);
    }

    @Test
    void testGravitationalForce() {
        // Create two bodies with known masses and positions
        Body body1 = new Body(0, 0, 0, 0, 1000);
        Body body2 = new Body(10, 0, 0, 0, 2000);
        List<Body> bodies = List.of(body1, body2);

        double[] fx = new double[2];
        double[] fy = new double[2];

        simulationLogic.computeForces(bodies, fx, fy);

        // Expected force: G * m1 * m2 / r² = 6.67430e-7 * 1000 * 2000 / 100 = 0.01334860
        double expectedForce = SimulationLogic.G * 1000 * 2000 / 100;
        assertEquals(expectedForce, fx[0], 0.0001);
        assertEquals(-expectedForce, fx[1], 0.0001);
        assertEquals(0, fy[0], 0.0001);
        assertEquals(0, fy[1], 0.0001);
    }

    @Test
    void testEmptyBodyList() {
        List<Body> bodies = List.of();
        double[] fx = new double[0];
        double[] fy = new double[0];

        // Should not throw exceptions
        assertDoesNotThrow(() -> simulationLogic.computeForces(bodies, fx, fy));
        assertDoesNotThrow(() -> simulationLogic.updatePositions(bodies, fx, fy));

        String grid = simulationLogic.buildGrid(bodies);
        assertNotNull(grid);
    }
}