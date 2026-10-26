import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { DarkTheme, DefaultTheme, Stack, ThemeProvider } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { useEffect, useState } from 'react';
import { useColorScheme } from 'react-native';

import { Button, EmptyState, Screen } from '@/components/ui';
import { SessionProvider, useSession } from '@/lib/auth';

void SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const scheme = useColorScheme();
  const [queryClient] = useState(
    () => new QueryClient({ defaultOptions: { queries: { retry: 1, staleTime: 10_000 } } }),
  );
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider value={scheme === 'dark' ? DarkTheme : DefaultTheme}>
        <SessionProvider>
          <RootNavigator />
        </SessionProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

/**
 * Three states: signed out (welcome, sign-up, login), signed in without a profile (onboarding), and the app itself.
 * The server enforces every rule again; these guards only decide which screens exist.
 */
function RootNavigator() {
  const { loading, token, account, signOut } = useSession();

  useEffect(() => {
    if (!loading) void SplashScreen.hideAsync();
  }, [loading]);

  if (loading) return null;

  if (token && !account) {
    return (
      <Screen>
        <EmptyState
          title="Can't reach Meet SWPS"
          text="Check your internet connection and try again."
          action={<Button title="Log out" variant="ghost" onPress={() => void signOut()} />}
        />
      </Screen>
    );
  }

  const signedIn = token !== null;
  const hasProfile = account?.hasProfile ?? false;
  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Protected guard={!signedIn}>
        <Stack.Screen name="(auth)" />
      </Stack.Protected>
      <Stack.Protected guard={signedIn && !hasProfile}>
        <Stack.Screen name="onboarding" />
      </Stack.Protected>
      <Stack.Protected guard={signedIn && hasProfile}>
        <Stack.Screen name="(app)" />
      </Stack.Protected>
    </Stack>
  );
}
