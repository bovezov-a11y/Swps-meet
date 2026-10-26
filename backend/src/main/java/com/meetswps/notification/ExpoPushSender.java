package com.meetswps.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Sends through Expo's push service, which forwards to Apple and Google. */
@Component
class ExpoPushSender implements PushSender {

    private static final Logger log = LoggerFactory.getLogger(ExpoPushSender.class);
    // Expo accepts at most 100 messages per request.
    private static final int BATCH_SIZE = 100;

    private final RestClient client;

    ExpoPushSender(@Value("${app.push.expo-url}") String url, @Value("${app.push.access-token:}") String accessToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        RestClient.Builder builder = RestClient.builder().baseUrl(url).requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (!accessToken.isBlank()) {
            // Only needed when "enhanced push security" is turned on for the Expo project.
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        }
        this.client = builder.build();
    }

    @Override
    public Set<String> send(List<PushMessage> messages) {
        Set<String> unregistered = new HashSet<>();
        for (int start = 0; start < messages.size(); start += BATCH_SIZE) {
            List<PushMessage> batch = messages.subList(start, Math.min(start + BATCH_SIZE, messages.size()));
            try {
                ExpoResponse response = client.post().contentType(MediaType.APPLICATION_JSON)
                        .body(batch.stream().map(ExpoMessage::from).toList())
                        .retrieve().body(ExpoResponse.class);
                if (response == null || response.data() == null) {
                    continue;
                }
                // Tickets come back in the order the messages were sent.
                for (int i = 0; i < response.data().size() && i < batch.size(); i++) {
                    ExpoTicket ticket = response.data().get(i);
                    if ("error".equals(ticket.status())) {
                        String error = ticket.details() == null ? null : String.valueOf(ticket.details().get("error"));
                        if ("DeviceNotRegistered".equals(error)) {
                            unregistered.add(batch.get(i).to());
                        } else {
                            log.warn("Push notification rejected: {} ({})", ticket.message(), error);
                        }
                    }
                }
            } catch (RestClientException e) {
                // A notification is a courtesy: the match or message is already saved, so never fail because of it.
                log.warn("Could not send push notifications", e);
            }
        }
        return unregistered;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ExpoMessage(String to, String title, String body, Map<String, String> data, String sound,
                       String channelId) {

        static ExpoMessage from(PushMessage message) {
            return new ExpoMessage(message.to(), message.title(), message.body(), message.data(), "default",
                    "default");
        }
    }

    record ExpoResponse(List<ExpoTicket> data) {
    }

    record ExpoTicket(String status, String id, String message, Map<String, Object> details) {
    }
}
