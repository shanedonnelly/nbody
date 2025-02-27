package org.polytech.simulation;

import core.Body;
import org.junit.jupiter.api.Test;
import simulation.SimulationLogic;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SimulationLogicTest {

    @Test
    void testCreateBodies() {
        List<Body> bodies = SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);
        assertEquals(SimulationLogic.BODY_COUNT, bodies.size());
    }

    @Test
    void testComputeForces() {
        List<Body> bodies = SimulationLogic.createBodies(2);
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        SimulationLogic.computeForces(bodies, fx, fy);
        assertArrayEquals(new double[bodies.size()], fx);
        assertArrayEquals(new double[bodies.size()], fy);
    }

    @Test
    void testUpdatePositions() {
        List<Body> bodies = SimulationLogic.createBodies(2);
        double[] fx = new double[bodies.size()];
        double[] fy = new double[bodies.size()];
        SimulationLogic.computeForces(bodies, fx, fy);
        SimulationLogic.updatePositions(bodies, fx, fy);
        assertNotEquals(0, bodies.getFirst().getX());
        assertNotEquals(0, bodies.getFirst().getY());
    }

    @Test
    void testSimulateStep() {
        List<Body> bodies = SimulationLogic.createBodies(2);
        SimulationLogic.simulateStep(bodies);
        assertNotEquals(0, bodies.getFirst().getX());
        assertNotEquals(0, bodies.getFirst().getY());
    }

    @Test
    void testBuildGrid() {
        List<Body> bodies = SimulationLogic.createBodies(2);
        String grid = SimulationLogic.buildGrid(bodies);
        assertNotNull(grid);
        assertFalse(grid.isEmpty());
    }
}