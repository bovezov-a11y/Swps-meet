import { isRunningInExpoGo } from 'expo';
import Constants from 'expo-constants';
import * as Notifications from 'expo-notifications';
import { type Href, router } from 'expo-router';
import { useEffect } from 'react';
import { Platform } from 'react-native';

import { api } from './api';
import { getItem, setItem } from './storage';

const TOKEN_KEY = 'meetswps.pushToken';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

/**
 * Asks for permission and sends this phone's push token to the backend. Does nothing where push cannot work: Expo
 * Go (push needs a development build) and builds without an EAS project id. The web preview uses notifications.web.ts.
 */
export async function registerForPushNotifications(): Promise<void> {
  if (isRunningInExpoGo()) return;
  const projectId = Constants.expoConfig?.extra?.eas?.projectId ?? Constants.easConfig?.projectId;
  if (!projectId) {
    console.warn('Push notifications are off: run `npx eas-cli init` to add an EAS project id (see README).');
    return;
  }
  try {
    if (Platform.OS === 'android') {
      // Android needs a channel before it can show notifications or hand out a token.
      await Notifications.setNotificationChannelAsync('default', {
        name: 'Matches and messages',
        // eslint-disable-next-line import/namespace -- exported through nested `export *`, which the rule cannot follow
        importance: Notifications.AndroidImportance.HIGH,
      });
    }
    let { status } = await Notifications.getPermissionsAsync();
    if (status !== 'granted') {
      ({ status } = await Notifications.requestPermissionsAsync());
    }
    if (status !== 'granted') return;

    const { data: token } = await Notifications.getExpoPushTokenAsync({ projectId });
    await api<void>('/api/push-tokens', { method: 'PUT', body: { token } });
    await setItem(TOKEN_KEY, token);
  } catch (error) {
    // Notifications are a convenience; the app works without them.
    console.warn('Could not register for push notifications', error);
  }
}

/** Called on logout, so the next person to log in on this phone does not get the previous student's messages. */
export async function unregisterPushNotifications(): Promise<void> {
  const token = await getItem(TOKEN_KEY);
  if (!token) return;
  await setItem(TOKEN_KEY, null);
  await api<void>('/api/push-tokens', { method: 'DELETE', body: { token } }).catch(() => {});
}

/** Tapping a "new match" or "new message" notification opens that chat. */
export function useOpenTappedNotification() {
  const response = Notifications.useLastNotificationResponse();
  useEffect(() => {
    const url = response?.notification.request.content.data?.url;
    if (typeof url === 'string' && url.startsWith('/')) {
      router.push(url as Href);
      Notifications.clearLastNotificationResponse();
    }
  }, [response]);
}
