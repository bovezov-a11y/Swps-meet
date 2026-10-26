package com.meetswps.notification;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ExpoPushSenderTest {

    private HttpServer server;
    private final AtomicReference<String> request = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private volatile String response;

    @BeforeEach
    void startFakeExpo() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/send", exchange -> {
            request.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/send";
    }

    @Test
    void reportsTokensOfUninstalledApps() {
        response = """
                {"data": [
                  {"status": "ok", "id": "1"},
                  {"status": "error", "message": "not registered", "details": {"error": "DeviceNotRegistered"}}
                ]}
                """;
        ExpoPushSender sender = new ExpoPushSender(url(), "secret");

        var unregistered = sender.send(List.of(
                new PushMessage("ExponentPushToken[a]", "Ola", "Hi", Map.of("url", "/chat/1")),
                new PushMessage("ExponentPushToken[b]", "Ola", "Hi", Map.of())));

        assertThat(unregistered).containsExactly("ExponentPushToken[b]");
        assertThat(request.get()).contains("\"to\":\"ExponentPushToken[a]\"", "\"channelId\":\"default\"",
                "\"url\":\"/chat/1\"");
        assertThat(authorization.get()).isEqualTo("Bearer secret");
    }

    @Test
    void aFailingPushServiceDoesNotThrow() {
        server.stop(0);
        ExpoPushSender sender = new ExpoPushSender(url(), "");

        assertThat(sender.send(List.of(new PushMessage("ExponentPushToken[a]", "t", "b", Map.of())))).isEmpty();
    }
}
