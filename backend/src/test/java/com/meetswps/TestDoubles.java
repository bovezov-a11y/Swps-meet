package com.meetswps;

import com.meetswps.mail.EmailSender;
import com.meetswps.notification.PushMessage;
import com.meetswps.notification.PushSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

/** Stand-ins for services that would send real emails or push notifications. */
@TestConfiguration(proxyBeanMethods = false)
public class TestDoubles {

    @Bean
    @Primary
    RecordingEmailSender recordingEmailSender() {
        return new RecordingEmailSender();
    }

    @Bean
    @Primary
    RecordingPushSender recordingPushSender() {
        return new RecordingPushSender();
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

    /** Records notifications; tokens added to {@code unregistered} are reported back as uninstalled apps. */
    public static class RecordingPushSender implements PushSender {

        public final List<PushMessage> sent = new CopyOnWriteArrayList<>();
        public final Set<String> unregistered = ConcurrentHashMap.newKeySet();

        @Override
        public Set<String> send(List<PushMessage> messages) {
            sent.addAll(messages);
            return Set.copyOf(messages.stream().map(PushMessage::to).filter(unregistered::contains).toList());
        }

        public List<PushMessage> sentTo(String token) {
            return sent.stream().filter(message -> message.to().equals(token)).toList();
        }
    }
}
