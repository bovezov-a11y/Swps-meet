import { useMutation } from '@tanstack/react-query';
import { useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { View } from 'react-native';

import { Button, Field, Screen, T } from '@/components/ui';
import { api, ApiError } from '@/lib/api';
import { useSession } from '@/lib/auth';
import { space } from '@/lib/theme';
import type { AuthResponse } from '@/lib/types';

export default function VerifyScreen() {
  const { email } = useLocalSearchParams<{ email: string }>();
  const { signIn } = useSession();
  const [code, setCode] = useState('');
  const [resent, setResent] = useState(false);

  const verify = useMutation({
    mutationFn: () => api<AuthResponse>('/api/auth/verify-email', { method: 'POST', body: { email, code } }),
    onSuccess: signIn,
  });
  const resend = useMutation({
    mutationFn: () => api<void>('/api/auth/resend-verification', { method: 'POST', body: { email } }),
    onSuccess: () => setResent(true),
  });

  return (
    <Screen>
      <View style={{ gap: space.xs, marginTop: space.xl }}>
        <T variant="title">Check your inbox</T>
        <T variant="muted">We sent a 6-digit code to {email}. It may take a minute, and check spam too.</T>
      </View>
      <Field
        label="Code"
        value={code}
        onChangeText={(v) => setCode(v.replace(/\D/g, '').slice(0, 6))}
        keyboardType="number-pad"
        autoComplete="one-time-code"
        textContentType="oneTimeCode"
        placeholder="123456"
      />
      {verify.error instanceof ApiError ? <T variant="error">{verify.error.summary}</T> : null}
      <Button title="Confirm" onPress={() => verify.mutate()} loading={verify.isPending} disabled={code.length !== 6} />
      <Button
        title={resent ? 'New code sent' : 'Send a new code'}
        variant="ghost"
        onPress={() => resend.mutate()}
        loading={resend.isPending}
        disabled={resent}
      />
    </Screen>
  );
}
