package org.polytech.websocket;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.websockets.next.WebSocketConnection;
import org.acme.simulation.SimulationLogic;
import org.acme.websocket.GridWebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;

@QuarkusTest
class GridWebSocketTest {

    private GridWebSocket gridWebSocket;
    private SimulationLogic simulationLogicMock;
    private WebSocketConnection connectionMock;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        simulationLogicMock = mock(SimulationLogic.class);
        connectionMock = mock(WebSocketConnection.class);

        gridWebSocket = new GridWebSocket();

        // Set the mock SimulationLogic
        Field simulationLogicField = GridWebSocket.class.getDeclaredField("simulationLogic");
        simulationLogicField.setAccessible(true);
        simulationLogicField.set(null, simulationLogicMock);

        // Clear the existing connections map
        Map<String, WebSocketConnection> connections = getConnections();
        connections.clear();

        // We can't mock the logger as it's final, so we'll skip that part
    }

    @Test
    void testOnOpen() {
        when(connectionMock.id()).thenReturn("123");

        gridWebSocket.onOpen(connectionMock);

        assertTrue(getConnections().containsKey("123"));
        // Can't verify logger interactions
    }

    @Test
    void testOnClose() {
        when(connectionMock.id()).thenReturn("123");
        getConnections().put("123", connectionMock);

        gridWebSocket.onClose(connectionMock);

        assertFalse(getConnections().containsKey("123"));
        // Can't verify logger interactions
    }

    @Test
    void testBroadcast() {
        when(connectionMock.id()).thenReturn("123");
        getConnections().put("123", connectionMock);

        GridWebSocket.broadcast("test message");
        verify(connectionMock).sendTextAndAwait("test message");
    }

    @Test
    void testBroadcastBinary() {
        when(connectionMock.id()).thenReturn("123");
        getConnections().put("123", connectionMock);
        byte[] data = new byte[]{1, 2, 3};

        GridWebSocket.broadcastBinary(data);
        verify(connectionMock).sendBinaryAndAwait(data);
    }

    @Test
    void testOnMessageStart() {
        gridWebSocket.onMessage("start");
        verify(simulationLogicMock).startSimulation();
    }

    @Test
    void testOnMessageStop() {
        gridWebSocket.onMessage("stop");
        verify(simulationLogicMock).stopSimulation();
    }

    @Test
    void testOnMessageAdd() {
        gridWebSocket.onMessage("add");
        verify(simulationLogicMock).addBodies();
    }

    @Test
    void testOnMessageDelete() {
        gridWebSocket.onMessage("delete");
        verify(simulationLogicMock).deleteBodies();
    }

    @SuppressWarnings("unchecked")
    private Map<String, WebSocketConnection> getConnections() {
        try {
            Field connectionsField = GridWebSocket.class.getDeclaredField("connections");
            connectionsField.setAccessible(true);
            return (Map<String, WebSocketConnection>) connectionsField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}