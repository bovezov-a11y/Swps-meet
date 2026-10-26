import { Stack } from 'expo-router';

export default function AuthLayout() {
  return (
    <Stack screenOptions={{ headerTitle: '', headerTransparent: true, headerBackButtonDisplayMode: 'minimal' }}>
      <Stack.Screen name="index" options={{ headerShown: false }} />
      <Stack.Screen
        name="privacy"
        options={{ presentation: 'modal', headerTransparent: false, headerTitle: 'Privacy policy' }}
      />
    </Stack>
  );
}
