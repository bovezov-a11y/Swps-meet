package com.meetswps.notification;

import java.util.Map;

/** One notification to one phone, in the shape of Expo's push API. */
public record PushMessage(String to, String title, String body, Map<String, String> data) {
}
