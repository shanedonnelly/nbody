package simulation;

import core.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Point d'entrée de la simulation N-corps en mode texte,
 * affichant la grille (0 pour noir, 1 pour blanc).
 */
public class MainSim {
    private static final Logger logger = LoggerFactory.getLogger(MainSim.class);

    public static void main(String[] args) {
        List<Body> bodies = SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);

        // Exécute plusieurs pas de simulation, en affichant la grille à chaque fois
        for (int step = 0; step < 10000; step++) {
            SimulationLogic.simulateStep(bodies);
            String grid = SimulationLogic.buildGrid(bodies);
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
}