import { useMutation } from '@tanstack/react-query';
import { router } from 'expo-router';
import { useState } from 'react';
import { View } from 'react-native';

import { Button, Field, Screen, T } from '@/components/ui';
import { api, ApiError } from '@/lib/api';
import { useSession } from '@/lib/auth';
import { space } from '@/lib/theme';
import type { AuthResponse } from '@/lib/types';

export default function LoginScreen() {
  const { signIn } = useSession();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const login = useMutation({
    mutationFn: () => api<AuthResponse>('/api/auth/login', { method: 'POST', body: { email, password } }),
    onSuccess: signIn,
    onError: (error) => {
      if (error instanceof ApiError && error.code === 'EMAIL_NOT_VERIFIED') {
        void api('/api/auth/resend-verification', { method: 'POST', body: { email } });
        router.push({ pathname: '/verify', params: { email: email.trim().toLowerCase() } });
      }
    },
  });

  return (
    <Screen>
      <View style={{ gap: space.xs, marginTop: space.xl }}>
        <T variant="title">Welcome back</T>
      </View>
      <Field
        label="SWPS email"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
      />
      <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry autoComplete="password" />
      {login.error instanceof ApiError ? <T variant="error">{login.error.summary}</T> : null}
      <Button title="Log in" onPress={() => login.mutate()} loading={login.isPending} disabled={!email || !password} />
    </Screen>
  );
}
