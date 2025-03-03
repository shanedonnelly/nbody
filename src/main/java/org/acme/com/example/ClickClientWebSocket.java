package org.acme.com.example;

/*
@ApplicationScoped
@WebSocket(path = "/click-client")
public class ClickClientWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(ClickClientWebSocket.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private WebSocketConnection connection;  // Stocke la connexion active

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        logger.info("Connexion établie avec un client");
        this.connection = connection;  // Sauvegarde la connexion actuelle
        executorService.submit(this::countSheep);
    }

    private void countSheep() {
        if (connection == null) {
            logger.error("Pas de connexion WebSocket active !");
            return;
        }

        while (true) {
            for (int i = 1; i <= 10; i++) {
                logger.info("{} sheep", i);
                try {
                    connection.sendTextAndAwait("There is currently " + i + " sheep");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    logger.error("Erreur d'envoi WebSocket : ", e);
                    return;
                }
            }
        }
    }

    @OnTextMessage
    public String onMessage(String message) {
        logger.info("Message reçu : {}", message);
        return message;
    }
}

 */
