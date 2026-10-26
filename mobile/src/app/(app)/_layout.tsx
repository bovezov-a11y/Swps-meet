import { Stack } from 'expo-router';
import { useEffect } from 'react';

import { registerForPushNotifications, useOpenTappedNotification } from '@/lib/notifications';

export default function AppLayout() {
  useOpenTappedNotification();

  useEffect(() => {
    void registerForPushNotifications();
  }, []);

  return (
    <Stack screenOptions={{ headerBackButtonDisplayMode: 'minimal' }}>
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen name="person/[id]" options={{ title: '' }} />
      <Stack.Screen name="chat/[id]" options={{ title: '' }} />
      <Stack.Screen name="edit-profile" options={{ title: 'Edit profile' }} />
      <Stack.Screen name="blocked" options={{ title: 'Blocked students' }} />
      <Stack.Screen name="privacy" options={{ title: 'Privacy policy', presentation: 'modal' }} />
      <Stack.Screen name="report" options={{ title: 'Report', presentation: 'modal' }} />
    </Stack>
  );
}
