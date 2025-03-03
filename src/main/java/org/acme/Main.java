package org.acme;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acme.simulation.SimulationLogic;

@QuarkusMain
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        // Catch exceptions from any thread
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Uncaught exception in thread {}: {}", thread.getName(), throwable.getMessage(), throwable);
            // Optionally, you can trigger a restart mechanism here (e.g. set a flag or similar)
        });

        boolean restart;
        do {
            restart = false;
            try {
                Quarkus.run(MyApp.class, args);
            } catch (Throwable t) {
                logger.error("Exception occurred in QuarkusApplication, restarting main method", t);
                restart = true;
                // Optionally add a delay before restart:
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } while (restart);
    }

    public static class MyApp implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            logger.info("Running main method");
            SimulationLogic simulationLogic = SimulationLogic.getInstance();
            simulationLogic.startSimulation(); // Assuming this starts simulation in its own thread(s)
            logger.info("Simulation started");
            Quarkus.waitForExit();
            return 0;
        }
    }
}