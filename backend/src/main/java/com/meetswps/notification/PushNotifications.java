package com.meetswps.notification;

import com.meetswps.connection.MatchCreated;
import com.meetswps.connection.MessageSent;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tells students about new matches and messages. Runs after the change is committed and off the request thread, so
 * a slow or failing push service never delays or breaks the app.
 */
@Component
class PushNotifications {

    static final int PREVIEW_LENGTH = 100;

    private final PushTokenRepository tokens;
    private final UserRepository users;
    private final PushSender sender;

    PushNotifications(PushTokenRepository tokens, UserRepository users, PushSender sender) {
        this.tokens = tokens;
        this.users = users;
        this.sender = sender;
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMatch(MatchCreated match) {
        Map<String, String> data = chat(match.matchId());
        List<PushMessage> messages = new ArrayList<>();
        messages.addAll(toUser(match.firstUserId(), "It's a match! 🎉",
                "You and " + firstName(match.secondUserId()) + " want to meet. Say hi!", data));
        messages.addAll(toUser(match.secondUserId(), "It's a match! 🎉",
                "You and " + firstName(match.firstUserId()) + " want to meet. Say hi!", data));
        send(messages);
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMessage(MessageSent message) {
        send(toUser(message.recipientId(), firstName(message.senderId()), preview(message.body()),
                chat(message.matchId())));
    }

    static String preview(String body) {
        return body.length() <= PREVIEW_LENGTH ? body : body.substring(0, PREVIEW_LENGTH - 1).strip() + "…";
    }

    private List<PushMessage> toUser(UUID userId, String title, String body, Map<String, String> data) {
        return tokens.findAllByUserIdIn(List.of(userId)).stream()
                .map(token -> new PushMessage(token.getToken(), title, body, data)).toList();
    }

    private void send(List<PushMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }
        Set<String> unregistered = sender.send(messages);
        // The app was uninstalled or notifications were revoked; stop sending to that phone.
        tokens.deleteAllById(unregistered);
    }

    private String firstName(UUID userId) {
        return users.findById(userId).map(User::getFirstName).orElse("Someone");
    }

    /** Opening the notification takes the student to the chat. */
    private static Map<String, String> chat(UUID matchId) {
        return Map.of("url", "/chat/" + matchId);
    }
}
