package org.acme.simulation;

import org.acme.core.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Point d'entrée de la simulation N-corps en mode texte,
 * affichant la grille (0 pour noir, 1 pour blanc).
 */
public class SimulationTest {
    private static final Logger logger = LoggerFactory.getLogger(SimulationTest.class);

    public static void testSim() {
        SimulationLogic simulationLogic = SimulationLogic.getInstance();
        List<Body> bodies = simulationLogic.createBodies(SimulationLogic.BODY_COUNT);

        // Exécute plusieurs pas de simulation, en affichant la grille à chaque fois
        for (int step = 0; step < 10000; step++) {
            simulationLogic.simulateStep(bodies);
            String grid = simulationLogic.buildGrid(bodies);
            logger.info("=== État de la simulation, étape {} ===", step);
            logger.info("\n{}", grid);
            // Optional: ralentir l'affichage
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public static void main(String[] args) {
        testSim();
    }
}