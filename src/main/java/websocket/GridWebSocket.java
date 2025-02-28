package websocket;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.SimulationLogic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@io.quarkus.websockets.next.WebSocket(path = "/websocket")
public class GridWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(GridWebSocket.class);

    private static SimulationLogic simulationLogic = SimulationLogic.getInstance();

    
    // Stocker toutes les connexions dans une map concurrente
    private static final Map<String, WebSocketConnection> connections = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        logger.info("Connexion établie avec un client: {}", connection.id());
        // Ajouter la connexion à la map
        connections.put(connection.id(), connection);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        logger.info("Connexion fermée avec le client: {}", connection.id());
        // Supprimer la connexion de la map
        connections.remove(connection.id());
    }
    
    /**
     * Méthode pour envoyer un message à tous les clients connectés
     * @param message le message à diffuser
     */
    public static void broadcast(String message) {
        //logger.info("Diffusion du message à {} clients", connections.size());
        // Envoyer à tous les clients
        for (WebSocketConnection conn : connections.values()) {
            try {
                //logger.info("Envoi du message au client {}", conn.id());
                conn.sendTextAndAwait(message);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi au client {}: {}", conn.id(), e.getMessage());
            }
        }
    }

    // Modifier la méthode runSimulationThread dans SimulationLogic ou ajouter cette méthode
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
        }else if (message.equals("add")){
            simulationLogic.addBodies();
        } else if (message.equals("delete")){
            simulationLogic.deleteBodies();
        }
    }
}