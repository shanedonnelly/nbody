package org.acme.websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acme.simulation.SimulationLogic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@io.quarkus.websockets.next.WebSocket(path = "/websocket")
public class GridWebSocket {

    protected static final Logger logger = LoggerFactory.getLogger(GridWebSocket.class);

    protected static SimulationLogic simulationLogic = SimulationLogic.getInstance();

    // Store all connections in a concurrent map
    protected static final Map<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        logger.info("Connexion établie avec un client: {}", connection.id());
        // Add the connection to the map
        connections.put(connection.id(), connection);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        logger.info("Connexion fermée avec le client: {}", connection.id());
        // Remove the connection from the map
        connections.remove(connection.id());
    }

    /**
     * Method to send a message to all connected clients
     * @param message the message to broadcast
     */
    public static void broadcast(String message) {
        for (WebSocketConnection conn : connections.values()) {
            try {
                conn.sendTextAndAwait(message);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi au client {}: {}", conn.id(), e.getMessage());
            }
        }
    }

    public static void broadcastBinary(byte[] data) {
        for (WebSocketConnection conn : connections.values()) {
            try {
                conn.sendBinaryAndAwait(data);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi binaire au client {}: {}", conn.id(), e.getMessage());
            }
        }
    }

    @OnTextMessage
    public void onMessage(String message) {
        logger.info("Message reçu : {}", message);
        if(message.equals("start")) {
            simulationLogic.startSimulation();
        } else if(message.equals("stop")) {
            simulationLogic.stopSimulation();
        } else if (message.equals("add")) {
            simulationLogic.addBodies();
        } else if (message.equals("delete")) {
            simulationLogic.deleteBodies();
        }
    }
}