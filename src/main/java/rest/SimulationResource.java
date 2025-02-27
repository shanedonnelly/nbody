package rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import core.Body;
import simulation.SimulationLogic;

@Path("/simulation")
public class SimulationResource {

    @GET
    @Path("/bodies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Body> getBodies() {
        return SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);
    }

    @GET
    @Path("/step")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Body> simulateStep() {
        List<Body> bodies = SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);
        SimulationLogic.simulateStep(bodies);
        return bodies;
    }

    @GET
    @Path("/grid")
    @Produces(MediaType.TEXT_PLAIN)
    public String getGrid() {
        List<Body> bodies = SimulationLogic.createBodies(SimulationLogic.BODY_COUNT);
        SimulationLogic.simulateStep(bodies);
        return SimulationLogic.buildGrid(bodies);
    }
}