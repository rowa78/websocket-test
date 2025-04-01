package de.ronnywalter.websocket_test.service;

import de.ronnywalter.websocket_test.config.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PeriodicMessageService {
    private static final Logger logger = LoggerFactory.getLogger(PeriodicMessageService.class);
    private final WebSocketHandler webSocketHandler;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PeriodicMessageService(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 5000) // Alle 5 Sekunden
    public void sendPeriodicMessage() {
        String currentTime = LocalDateTime.now().format(formatter);
        String message = String.format("Server-Zeit: %s - Aktive Verbindungen: %d", 
            currentTime, 
            webSocketHandler.getActiveConnections());
        
        logger.debug("Sende periodische Nachricht: {}", message);
        webSocketHandler.broadcast(message);
    }
} 