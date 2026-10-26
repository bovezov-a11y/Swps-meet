package com.meetswps;

import com.meetswps.mail.EmailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Stand-ins for services that would send real emails. */
@TestConfiguration(proxyBeanMethods = false)
public class TestDoubles {

    @Bean
    @Primary
    RecordingEmailSender recordingEmailSender() {
        return new RecordingEmailSender();
    }

    public static class RecordingEmailSender implements EmailSender {

        private final Map<String, String> lastBodyByRecipient = new ConcurrentHashMap<>();

        @Override
        public void send(String to, String subject, String body) {
            lastBodyByRecipient.put(to, body);
        }

        public Optional<String> lastEmailTo(String address) {
            return Optional.ofNullable(lastBodyByRecipient.get(address));
        }
    }
}
