package de.ronnywalter.websocket_test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final int PING_INTERVAL_SECONDS = 10;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("Neue WebSocket-Verbindung hergestellt - Session ID: {}", session.getId());
        sessions.put(session.getId(), session);
        logger.debug("Aktive Verbindungen: {}", sessions.size());
        
        // Starte Ping-Timer für diese Session
        startPingTimer(session);
    }

    private void startPingTimer(WebSocketSession session) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    logger.debug("Sende Ping an Session {}", session.getId());
                    session.sendMessage(new PingMessage());
                }
            } catch (Exception e) {
                logger.error("Fehler beim Senden des Pings an Session {}: {}", session.getId(), e.getMessage());
            }
        }, PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        logger.debug("Pong empfangen von Session {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket-Verbindung geschlossen - Session ID: {}, Status: {}", session.getId(), status);
        sessions.remove(session.getId());
        logger.debug("Aktive Verbindungen: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Nachricht empfangen von Session {}: {}", session.getId(), message.getPayload());
        
        // Reagiere auf Client-Pings
        if ("ping".equals(message.getPayload())) {
            session.sendMessage(new TextMessage("pong"));
            logger.debug("Pong gesendet an Session {} (Client-Ping)", session.getId());
            return;
        }
        
        // Echo die Nachricht zurück
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
        logger.debug("Echo-Nachricht gesendet an Session {}", session.getId());
    }

    public void broadcast(String message) {
        logger.info("Broadcast-Nachricht wird gesendet: {}", message);
        TextMessage textMessage = new TextMessage(message);
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(textMessage);
                logger.debug("Broadcast-Nachricht gesendet an Session {}", session.getId());
            } catch (Exception e) {
                logger.error("Fehler beim Senden der Broadcast-Nachricht an Session {}: {}", session.getId(), e.getMessage());
            }
        });
    }

    public int getActiveConnections() {
        return sessions.size();
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 