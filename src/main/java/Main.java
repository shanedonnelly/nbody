
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.Quarkus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.SimulationTest;
import simulation.SimulationLogic;


@QuarkusMain
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static SimulationLogic simulationLogic;

    public static void main(String... args) {
        Quarkus.run(MyApp.class, args);
    }

    public static class MyApp implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            logger.info("Running main method");
            simulationLogic = SimulationLogic.getInstance();
            simulationLogic.startSimulation();
            logger.info("Simulation started");
            Quarkus.waitForExit();
            return 0;
        }
    }

}