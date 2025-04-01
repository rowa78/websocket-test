package de.ronnywalter.websocket_test.controller;

import de.ronnywalter.websocket_test.config.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    private final WebSocketHandler webSocketHandler;

    public WebSocketController(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/broadcast")
    public void broadcast(@RequestBody String message) {
        logger.info("Broadcast-Anfrage empfangen: {}", message);
        webSocketHandler.broadcast(message);
        logger.debug("Broadcast-Anfrage verarbeitet");
    }
} 