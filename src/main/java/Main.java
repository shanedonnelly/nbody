
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.Quarkus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.SimulationTest;

@QuarkusMain
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String ... args) {
        logger.info("Running main method");
        Quarkus.run(args);
        //SimulationTest.testSim();
    }
}