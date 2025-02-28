package org.polytech.simulation;

import core.Body;
import org.junit.jupiter.api.Test;
import simulation.SimulationLogic;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SimulationLogicTest {

    @Test
    void testCreateBodies() {
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
        List<Body> bodies = simulationLogic.createBodies(SimulationLogic.BODY_COUNT);
        assertEquals(SimulationLogic.BODY_COUNT, bodies.size());
    }

    @Test
    void testComputeForces() {
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
        List<Body> bodies = simulationLogic.createBodies(2);
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        simulationLogic.computeForces(bodies, fx, fy);
        assertArrayEquals(new double[bodies.size()], fx);
        assertArrayEquals(new double[bodies.size()], fy);
    }

    @Test
    void testUpdatePositions() {
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
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
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
        List<Body> bodies = simulationLogic.createBodies(2);
        simulationLogic.simulateStep(bodies);
        assertNotEquals(0, bodies.getFirst().getX());
        assertNotEquals(0, bodies.getFirst().getY());
    }

    @Test
    void testBuildGrid() {
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
        List<Body> bodies = simulationLogic.createBodies(2);
        String grid = simulationLogic.buildGrid(bodies);
        assertNotNull(grid);
        assertFalse(grid.isEmpty());
    }
}