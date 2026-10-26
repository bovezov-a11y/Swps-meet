package com.meetswps.notification;

import java.util.List;
import java.util.Set;

/** Delivers notifications to phones. Kept behind an interface so tests can record messages instead of sending them. */
public interface PushSender {

    /** Sends the messages and returns the tokens the push service no longer accepts, so they can be forgotten. */
    Set<String> send(List<PushMessage> messages);
}
