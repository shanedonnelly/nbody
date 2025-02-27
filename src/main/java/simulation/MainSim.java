package simulation;

import core.Body;

import java.util.List;

/**
 * Point d'entrée de la simulation N-corps en mode texte,
 * affichant la grille (0 pour noir, 1 pour blanc).
 */
public class MainSim {
    public static void main(String[] args) {
        List<Body> bodies = SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);

        // Exécute plusieurs pas de simulation, en affichant la grille à chaque fois
        for (int step = 0; step < 10000; step++) {
            SimulationLogic.simulateStep(bodies);
            String grid = SimulationLogic.buildGrid(bodies);
            System.out.println("=== État de la simulation, étape " + step + " ===");
            System.out.print(grid);
            // Optionnel: ralentir l'affichage
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}