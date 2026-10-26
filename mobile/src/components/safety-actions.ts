import { router } from 'expo-router';
import { Alert } from 'react-native';

/** Asks before blocking, since it also ends any match and cannot be noticed by the other student. */
export function confirmBlock(name: string, block: () => Promise<unknown>) {
  Alert.alert(`Block ${name}?`, 'You will not see each other anywhere in the app. They will not be told.', [
    { text: 'Cancel', style: 'cancel' },
    {
      text: 'Block',
      style: 'destructive',
      onPress: () => void block().then(() => router.dismissTo('/')),
    },
  ]);
}

export function openReport(userId: string, name: string) {
  router.push({ pathname: '/report', params: { userId, name } });
}
